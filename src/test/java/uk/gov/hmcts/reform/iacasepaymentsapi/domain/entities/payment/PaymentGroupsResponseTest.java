package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class PaymentGroupsResponseTest {

    @Test
    void should_hold_onto_values() {
        PaymentGroupDto paymentGroup = PaymentGroupDto.builder()
            .paymentGroupReference("ref-123")
            .serviceRequestStatus("Not paid")
            .build();

        PaymentGroupsResponse response = PaymentGroupsResponse.builder()
            .paymentGroups(List.of(paymentGroup))
            .build();

        assertNotNull(response.getPaymentGroups());
        assertEquals(1, response.getPaymentGroups().size());
        assertEquals("ref-123", response.getPaymentGroups().get(0).getPaymentGroupReference());
    }

    @Test
    void should_allow_all_args_constructor() {
        PaymentGroupDto paymentGroup = PaymentGroupDto.builder()
            .paymentGroupReference("ref-456")
            .build();

        PaymentGroupsResponse response = new PaymentGroupsResponse(List.of(paymentGroup));

        assertNotNull(response.getPaymentGroups());
        assertEquals(1, response.getPaymentGroups().size());
    }

    @Test
    void should_allow_no_args_constructor() {
        PaymentGroupsResponse response = new PaymentGroupsResponse();

        assertNotNull(response);
        assertNull(response.getPaymentGroups());
    }
}