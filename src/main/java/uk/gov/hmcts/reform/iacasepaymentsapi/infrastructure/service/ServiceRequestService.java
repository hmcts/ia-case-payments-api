package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service;

import java.util.Optional;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CasePaymentRequestDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentGroupDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentGroupsResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestRequest;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.ServiceRequestApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SystemTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions.PaymentServiceRequestException;

@Service
@Slf4j
@EnableRetry
public class ServiceRequestService {

    private static final String PAYMENT_ACTION = "payment";
    private static final int VOLUME_1 = 1;

    private final SystemTokenGenerator systemTokenGenerator;
    private final AuthTokenGenerator serviceAuthorization;
    private final ServiceRequestApi serviceRequestApi;
    private String callBackUrl;
    private final RequestUserAccessTokenProvider userAuthorizationProvider;

    public ServiceRequestService(SystemTokenGenerator systemTokenGenerator,
                                 AuthTokenGenerator serviceAuthorization,
                                 RequestUserAccessTokenProvider userAuthorizationProvider,
                                 ServiceRequestApi serviceRequestApi,
                                 @Value("${payment.api.callback-url}") String callBackUrl) {
        this.systemTokenGenerator = systemTokenGenerator;
        this.serviceAuthorization = serviceAuthorization;
        this.userAuthorizationProvider = userAuthorizationProvider;
        this.serviceRequestApi = serviceRequestApi;
        this.callBackUrl = callBackUrl;
    }

    @Retryable(retryFor = { FeignException.class }, maxAttempts = 3, backoff = @Backoff(3000))
    public ServiceRequestResponse createServiceRequest(Callback<AsylumCase> callback, Fee fee) {

        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        AsylumCase asylumCase = caseDetails.getCaseData();
        String ccdCaseReferenceNumber = String.valueOf(caseDetails.getId());
        String appealReferenceNumber = asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)
            .orElse("");
        String appellantGivenNames = asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)
            .orElse("");
        String appellantFamilyName = asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)
            .orElse("");
        String responsibleParty = appellantGivenNames + " " + appellantFamilyName;

        String userAuth = systemTokenGenerator.generate();
        String serviceAuth = serviceAuthorization.generate();

        ServiceRequestResponse serviceRequestResponse;
        log.info("Calling Payment Service Request API for case reference {}", ccdCaseReferenceNumber);
        try {
            serviceRequestResponse = serviceRequestApi.createServiceRequest(
                userAuth,
                serviceAuth,
                ServiceRequestRequest.builder()
                    .callBackUrl(callBackUrl)
                    .casePaymentRequest(
                        CasePaymentRequestDto.builder()
                            .action(PAYMENT_ACTION)
                            .responsibleParty(responsibleParty)
                            .build())
                    .caseReference(appealReferenceNumber)
                    .ccdCaseNumber(ccdCaseReferenceNumber)
                    .fees(new FeeDto[]{
                        FeeDto.builder()
                            .calculatedAmount(fee.getCalculatedAmount())
                            .code(fee.getCode())
                            .version(fee.getVersion())
                            .volume(VOLUME_1).build()
                    })
                    .build()
            );
            log.info(
                "Payment Service Request API response for case reference {} - {}",
                ccdCaseReferenceNumber,
                serviceRequestResponse != null ? serviceRequestResponse.getServiceRequestReference() : ""
            );
        } catch (FeignException fe) {
            log.error(
                "Error in calling Payment Service Request API for case reference {} \n {}\n Retrying now...",
                ccdCaseReferenceNumber,
                fe.getMessage()
            );
            throw fe;
        }
        return serviceRequestResponse;
    }

    @Recover
    public ServiceRequestResponse recover(FeignException fe, Callback<AsylumCase> callback, Fee fee) {
        log.error("Error in calling Payment Service Request API for 3 retries \n {}", fe.getMessage());
        throw new PaymentServiceRequestException(fe.getMessage(), fe.getCause());
    }

    public Optional<ServiceRequestResponse> getExistingUnpaidServiceRequest(
        Callback<AsylumCase> callback,
        Fee fee
    ) {
        String ccdCaseReferenceNumber = String.valueOf(callback.getCaseDetails().getId());

        PaymentGroupsResponse existingPaymentGroups;
        try {
            log.info("Calling Payment Service Request API - Get Payment Groups for case reference {}", ccdCaseReferenceNumber);
            existingPaymentGroups = serviceRequestApi.getServiceRequests(
                userAuthorizationProvider.getAccessToken(),
                serviceAuthorization.generate(),
                ccdCaseReferenceNumber
            );
        } catch (FeignException e) {
            log.warn(
                "Failed to get existing service requests for case reference {}. Proceeding to create new service request. Error: {}",
                ccdCaseReferenceNumber,
                e.getMessage()
            );
            return Optional.empty();
        }

        if (existingPaymentGroups == null || existingPaymentGroups.getPaymentGroups() == null) {
            log.info("Existing Payment Groups for case reference {} is null", ccdCaseReferenceNumber);
            return Optional.empty();
        }

        for (PaymentGroupDto paymentGroup : existingPaymentGroups.getPaymentGroups()) {
            if ("Not paid".equals(paymentGroup.getServiceRequestStatus())
                && hasMatchingFee(paymentGroup, fee)) {
                log.info(
                    "Found existing unpaid service request for case reference {} with fee code {} - {}",
                    ccdCaseReferenceNumber,
                    fee.getCode(),
                    paymentGroup.getPaymentGroupReference()
                );
                return Optional.of(ServiceRequestResponse.builder()
                    .serviceRequestReference(paymentGroup.getPaymentGroupReference())
                    .build());
            }
        }

        log.info("Payment Groups for case reference {} is empty", ccdCaseReferenceNumber);
        return Optional.empty();
    }

    private boolean hasMatchingFee(PaymentGroupDto paymentGroup, Fee fee) {
        if (paymentGroup.getFees() == null || paymentGroup.getFees().isEmpty()) {
            return false;
        }
        return paymentGroup.getFees().stream()
            .anyMatch(feeDto -> fee.getCode().equals(feeDto.getCode()));
    }
}
