package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PBA_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.StaticPortWiremockFactory;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithFeeStub;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithPaymentStub;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithServiceAuthStub;

public class MakePaymentIntegrationTest extends SpringBootIntegrationTest
                                        implements WithIdamStub, WithServiceAuthStub, WithFeeStub, WithPaymentStub {

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void executionEndpoint(@WiremockResolver
                    .Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) throws Exception {

        addIdamTokenStub(server);
        addUserInfoStub(server);
        addServiceAuthStub(server);
        addFeesRegisterStub(server);
        addPaymentStub(server);

        IaCasePaymentApiClient iaCasePaymentApiClient = new IaCasePaymentApiClient(mockMvc);

        PreSubmitCallbackResponseForTest response = iaCasePaymentApiClient.aboutToSubmit(callback()
            .event(Event.PAYMENT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_STARTED)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, "refusalOfEu")
                    .with(DECISION_HEARING_FEE_OPTION, "decisionWithHearing")
                    .with(PBA_NUMBER, "PBA1234567")
                    .with(HOME_OFFICE_REFERENCE_NUMBER, "A123456/003"))));

        assertEquals("Paid", response.getAsylumCase().read(PAYMENT_STATUS, String.class).orElse(""));
        assertEquals("PBA1234567", response.getAsylumCase().read(PBA_NUMBER, String.class).orElse(""));
        assertEquals("RC-1590-6786-1063-9996", response.getAsylumCase()
                            .read(PAYMENT_REFERENCE, String.class).orElse(""));
        assertEquals("140.00", response.getAsylumCase().read(FEE_AMOUNT, BigDecimal.class)
                            .orElse(BigDecimal.valueOf(140.00)).toString());
        assertEquals("2", response.getAsylumCase().read(FEE_VERSION, String.class).orElse(""));


        PreSubmitCallbackResponseForTest responseNoHearing = iaCasePaymentApiClient.aboutToSubmit(callback()
            .event(Event.PAYMENT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_STARTED)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, "refusalOfEu")
                    .with(DECISION_HEARING_FEE_OPTION, "decisionWithoutHearing")
                    .with(PBA_NUMBER, "PBA1234567")
                    .with(HOME_OFFICE_REFERENCE_NUMBER, "A123456/003"))));

        assertEquals("Paid", responseNoHearing.getAsylumCase()
                        .read(PAYMENT_STATUS, String.class).orElse(""));
        assertEquals("PBA1234567", responseNoHearing.getAsylumCase()
                        .read(PBA_NUMBER, String.class).orElse(""));
        assertEquals("RC-1590-6786-1063-9996", responseNoHearing.getAsylumCase()
                        .read(PAYMENT_REFERENCE, String.class).orElse(""));
        assertEquals("80.00", responseNoHearing.getAsylumCase()
                        .read(FEE_AMOUNT, BigDecimal.class).orElse(BigDecimal.valueOf(80.00)).toString());
        assertEquals("2", responseNoHearing.getAsylumCase().read(FEE_VERSION, String.class).orElse(""));
    }

}
