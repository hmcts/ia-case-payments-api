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

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class UpdatePaymentStatusIntegrationTest extends SpringBootIntegrationTest
    implements WithServiceAuthStub, WithFeeStub, WithPaymentStub, WithIdamStub, WithCcdStub {

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
    }

}
