package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;

import java.util.HashMap;

import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.CALLBACK_COMPLETED;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.ID;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.JURISDICTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.SUCCESS;

public class SubmitEventDetailsForTest extends SubmitEventDetails {

    public static SubmitEventDetails generateValidPaymentUpdateEvent() {
        return new SubmitEventDetails(
            Long.parseLong(ID),
            JURISDICTION,
            State.PENDING_PAYMENT,
            generateEventData(CCD_CASE_NUMBER),
            200,
            CALLBACK_COMPLETED
        );
    }

    private static HashMap<String, Object> generateEventData(String caseReference) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("paymentStatus", SUCCESS);
        data.put("paymentReference", caseReference);
        return data;
    }

}
