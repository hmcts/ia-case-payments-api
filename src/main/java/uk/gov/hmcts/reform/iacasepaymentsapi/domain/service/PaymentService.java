package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Service.FPL;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.PaymentApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.PaymentConfiguration;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.RequestUserAccessTokenProvider;

@Service
@Slf4j
public class PaymentService {

    private final PaymentApi paymentApi;
    private final PaymentConfiguration paymentConfiguration;
    private final RequestUserAccessTokenProvider userAuthorizationProvider;
    private final AuthTokenGenerator serviceAuthorizationProvider;

    public PaymentService(PaymentApi paymentApi,
                          PaymentConfiguration paymentConfiguration,
                          RequestUserAccessTokenProvider userAuthorizationProvider,
                          AuthTokenGenerator serviceAuthorizationProvider) {

        this.paymentApi = paymentApi;
        this.paymentConfiguration = paymentConfiguration;
        this.userAuthorizationProvider = userAuthorizationProvider;
        this.serviceAuthorizationProvider = serviceAuthorizationProvider;
    }

    public PaymentResponse creditAccountPayment(CreditAccountPayment creditAccountPaymentRequest) {

        creditAccountPaymentRequest.setOrganisationName(paymentConfiguration.getOrganisationUrn());
        creditAccountPaymentRequest.setService(FPL);
        creditAccountPaymentRequest.setSiteId(paymentConfiguration.getSiteId());

        return paymentApi.creditAccountPaymentRequest(
            userAuthorizationProvider.getAccessToken(),
            serviceAuthorizationProvider.generate(),
            creditAccountPaymentRequest);
    }
}
