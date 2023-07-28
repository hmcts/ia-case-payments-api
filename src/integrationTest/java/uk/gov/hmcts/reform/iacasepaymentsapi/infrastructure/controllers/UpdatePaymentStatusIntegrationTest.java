package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.PaymentDtoForTest;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithCcdStub;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithFeeStub;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithPaymentStub;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithServiceAuthStub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State.PENDING_PAYMENT;

@Slf4j
public class UpdatePaymentStatusIntegrationTest extends SpringBootIntegrationTest
    implements WithServiceAuthStub, WithFeeStub, WithPaymentStub, WithIdamStub, WithCcdStub {

    public static final String ID = "1234";
    public static final String PAYMENT_STATUS_UPDATE_CASE_REFERENCE = "RC-1627-5070-9329-7815";
    public static final String CCD_CASE_NUMBER = "1627506765384547";
    public static final String JURISDICTION = "IA";

    private IaCasePaymentApiClient iaCasePaymentApiClient;

    @BeforeEach
    public void setup() {
        iaCasePaymentApiClient = new IaCasePaymentApiClient(mockMvc);
    }

    @Test
    public void updatePaymentStatusEndpoint() throws Exception {
        addServiceAuthStub(server);
        addFeesRegisterStub(server);
        addPaymentStub(server);
        addUserInfoStub(server);
        addIdamTokenStub(server);
        addCcdUpdatePaymentStatusGetTokenStub(server);
        addCcdUpdatePaymentStatusEventStub(server);
        addPaymentUpdateSubmitStub(server);

        SubmitEventDetails response = iaCasePaymentApiClient.updatePaymentStatus(PaymentDtoForTest.generateValid().build());

        assertNotNull(response);
        assertEquals("CALLBACK_COMPLETED", response.getCallbackResponseStatus());
        assertEquals(200, response.getCallbackResponseStatusCode());
        assertEquals(CCD_CASE_NUMBER, response.getData().get("paymentReference"));
        assertEquals("Success", response.getData().get("paymentStatus"));
        assertEquals(Long.parseLong(ID), response.getId());
        assertEquals(JURISDICTION, response.getJurisdiction());
        assertEquals(PENDING_PAYMENT, response.getState());
    }

}
