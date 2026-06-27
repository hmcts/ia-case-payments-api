package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class LegRepAddressUkTest {

    @Test
    void should_hold_onto_values() {
        List<String> dxAddress = Arrays.asList("DX 123", "DX 456");

        LegRepAddressUk address = new LegRepAddressUk(
            "123 Test Street",
            "Suite 100",
            "Building A",
            "London",
            "Greater London",
            "SW1A 1AA",
            "United Kingdom",
            dxAddress
        );

        assertEquals("123 Test Street", address.getAddressLine1());
        assertEquals("Suite 100", address.getAddressLine2());
        assertEquals("Building A", address.getAddressLine3());
        assertEquals("London", address.getTownCity());
        assertEquals("Greater London", address.getCounty());
        assertEquals("SW1A 1AA", address.getPostCode());
        assertEquals("United Kingdom", address.getCountry());
        assertEquals(dxAddress, address.getDxAddress());
    }

    @Test
    void should_return_empty_list_when_dx_address_is_null() {
        LegRepAddressUk address = new LegRepAddressUk(
            "123 Test Street",
            "Suite 100",
            "Building A",
            "London",
            "Greater London",
            "SW1A 1AA",
            "United Kingdom",
            null
        );

        assertTrue(address.getDxAddress().isEmpty());
    }
}
