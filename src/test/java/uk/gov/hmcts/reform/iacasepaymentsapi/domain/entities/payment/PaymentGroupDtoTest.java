package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeDto;

class PaymentGroupDtoTest {

    @Test
    void should_hold_onto_values() {
        FeeDto fee = FeeDto.builder()
            .code("FEE0001")
            .version("1")
            .build();

        PaymentGroupDto paymentGroup = PaymentGroupDto.builder()
            .paymentGroupReference("ref-123")
            .serviceRequestStatus("Not paid")
            .fees(List.of(fee))
            .payments(null)
            .build();

        assertEquals("ref-123", paymentGroup.getPaymentGroupReference());
        assertEquals("Not paid", paymentGroup.getServiceRequestStatus());
        assertNotNull(paymentGroup.getFees());
        assertEquals(1, paymentGroup.getFees().size());
        assertEquals("FEE0001", paymentGroup.getFees().get(0).getCode());
    }

    @Test
    void should_allow_all_args_constructor() {
        FeeDto fee = FeeDto.builder().code("FEE0002").build();
        PaymentDto payment = PaymentDto.builder().id("pay-1").build();

        PaymentGroupDto paymentGroup = new PaymentGroupDto(
            "ref-456",
            "Paid",
            List.of(fee),
            List.of(payment)
        );

        assertEquals("ref-456", paymentGroup.getPaymentGroupReference());
        assertEquals("Paid", paymentGroup.getServiceRequestStatus());
        assertNotNull(paymentGroup.getFees());
        assertNotNull(paymentGroup.getPayments());
    }

    @Test
    void should_allow_no_args_constructor() {
        PaymentGroupDto paymentGroup = new PaymentGroupDto();

        assertNotNull(paymentGroup);
        assertNull(paymentGroup.getPaymentGroupReference());
        assertNull(paymentGroup.getServiceRequestStatus());
        assertNull(paymentGroup.getFees());
        assertNull(paymentGroup.getPayments());
    }
}