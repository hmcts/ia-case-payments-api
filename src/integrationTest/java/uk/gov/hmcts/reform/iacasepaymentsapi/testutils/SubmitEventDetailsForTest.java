package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;

import java.util.HashMap;

public class SubmitEventDetailsForTest extends SubmitEventDetails {

    public static SubmitEventDetails generateValidPaymentUpdateEvent() {
        return new SubmitEventDetails(
            1234L,
            "IA",
            State.PENDING_PAYMENT,
            generateEventData("RC-1627-5070-9329-7815"),
            200,
            "CALLBACK_COMPLETED"
        );
    }

    private static HashMap<String, Object> generateEventData(String caseReference) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("paymentStatus", "Success");
        data.put("paymentReference", caseReference);
        return data;
    }

}
