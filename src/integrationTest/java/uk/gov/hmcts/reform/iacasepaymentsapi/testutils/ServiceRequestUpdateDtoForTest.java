package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestUpdateDto;

import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.PAYMENT_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.PAYMENT_STATUS_UPDATE_CASE_REFERENCE;

@SuperBuilder
public class ServiceRequestUpdateDtoForTest extends ServiceRequestUpdateDto {

    public static ServiceRequestUpdateDtoBuilder generateValid() {
        return builder()
            .serviceRequestReference(PAYMENT_STATUS_UPDATE_CASE_REFERENCE)
            .ccdCaseNumber(CCD_CASE_NUMBER)
            .serviceRequestAmount(PAYMENT_AMOUNT.toString())
            .serviceRequestStatus("success")
            .payment(PaymentDtoForTest.generateValid().build());
    }

}
