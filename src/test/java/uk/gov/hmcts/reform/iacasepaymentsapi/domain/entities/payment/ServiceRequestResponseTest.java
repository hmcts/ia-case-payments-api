package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ServiceRequestResponseTest {

    @Test
    void should_hold_onto_values() {
        String serviceRequestReference = "some-service-request-reference";
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder()
            .serviceRequestReference(serviceRequestReference)
            .build();

        assertEquals("some-service-request-reference", serviceRequestResponse.getServiceRequestReference());
    }
}
