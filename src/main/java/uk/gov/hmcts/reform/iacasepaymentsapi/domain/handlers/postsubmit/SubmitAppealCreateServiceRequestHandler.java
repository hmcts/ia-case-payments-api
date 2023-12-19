package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HAS_SERVICE_REQUEST_ALREADY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.SERVICE_REQUEST_REFERENCE;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit.ErrorHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit.FeesHelper;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.ServiceRequestService;

@Slf4j
@Component
public class SubmitAppealCreateServiceRequestHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final ServiceRequestService serviceRequestService;
    private final FeeService feeService;

    private final Optional<ErrorHandler<AsylumCase>> errorHandling;

    public SubmitAppealCreateServiceRequestHandler(
        ServiceRequestService serviceRequestService,
        FeeService feeService,
        Optional<ErrorHandler<AsylumCase>> errorHandling) {
        this.serviceRequestService = serviceRequestService;
        this.feeService = feeService;
        this.errorHandling = errorHandling;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.CREATE_SERVICE_REQUEST;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Fee fee = FeesHelper.findFeeByHearingType(feeService, asylumCase);

        YesOrNo requestFeeRemissionFlagForServiceRequest =
            asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)
            .orElse(YesOrNo.NO);

        PaymentStatus paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)
            .orElse(PaymentStatus.PAYMENT_PENDING);

        if (isWaysToPay(callback, isLegalRepJourney(asylumCase))
            && hasNoRemission(asylumCase)
            && requestFeeRemissionFlagForServiceRequest != YesOrNo.YES
            && paymentStatus != PaymentStatus.PAID) {
            try {
                Optional<YesOrNo> test = asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class);
                log.info("result is : " + test.toString());
                ServiceRequestResponse serviceRequestResponse = serviceRequestService.createServiceRequest(callback, fee);
                log.info("Generated service request successfully {}", serviceRequestResponse.toString());
                String serviceRequestReference = serviceRequestResponse.getServiceRequestReference();
                log.info("Service request reference {} being saved as {}", serviceRequestReference, SERVICE_REQUEST_REFERENCE);
                asylumCase.write(SERVICE_REQUEST_REFERENCE, serviceRequestReference);
                Optional<String> savedServiceRequestReference = asylumCase.read(SERVICE_REQUEST_REFERENCE, String.class);
                log.info("Successfully written serviceRequestReference to case_data {}", asylumCase.read(SERVICE_REQUEST_REFERENCE, String.class));
            } catch (Exception e) {
                log.error("something failed: {}", e.getMessage());
                errorHandling.ifPresent(asylumCaseErrorHandler -> asylumCaseErrorHandler.accept(callback, e));
            }
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isWaysToPay(Callback<AsylumCase> callback,
                                boolean isLegalRepJourney) {

        List<Event> waysToPayEvents = List.of(Event.SUBMIT_APPEAL,
                                              Event.GENERATE_SERVICE_REQUEST,
                                              Event.CREATE_SERVICE_REQUEST,
                                              Event.RECORD_REMISSION_DECISION);

        return waysToPayEvents.contains(callback.getEvent())
               && isLegalRepJourney
               && isHuEaEuPa(callback.getCaseDetails().getCaseData());
    }

    private boolean isHuEaEuPa(AsylumCase asylumCase) {
        Optional<AppealType> optionalAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);
        if (optionalAppealType.isPresent()) {
            AppealType appealType = optionalAppealType.get();
            return List.of(HU, EA, EU, PA).contains(appealType);
        }
        return false;
    }

    private boolean isLegalRepJourney(AsylumCase asylumCase) {
        return asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(journey -> journey == JourneyType.REP)
            .orElse(true);
    }

    private boolean hasNoRemission(AsylumCase asylumCase) {
        Optional<RemissionType> optRemissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        Optional<RemissionDecision> optionalRemissionDecision =
            asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        return (optRemissionType.isPresent() && optRemissionType.get() == RemissionType.NO_REMISSION)
               || optRemissionType.isEmpty()
               || (optionalRemissionDecision.isPresent()
                   && optionalRemissionDecision.get() == RemissionDecision.REJECTED);
    }
}
