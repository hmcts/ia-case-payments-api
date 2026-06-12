package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.LegRepAddressUk;

class OrganisationEntityResponseTest {

    @Test
    void should_hold_onto_values() {
        List<String> paymentAccounts = Arrays.asList("PBA0001", "PBA0002");
        List<LegRepAddressUk> contactInfo = Arrays.asList(
            new LegRepAddressUk("Line1", "Line2", "Line3", "City", "County", "AB1 2CD", "UK", null)
        );
        SuperUser superUser = new SuperUser("John", "Doe", "john.doe@test.com");

        OrganisationEntityResponse response = new OrganisationEntityResponse(
            "ORG123",
            "Test Organisation",
            "ACTIVE",
            "SRA123",
            "true",
            "12345678",
            "https://test.org",
            superUser,
            paymentAccounts,
            contactInfo
        );

        assertEquals("ORG123", response.getOrganisationIdentifier());
        assertEquals("Test Organisation", response.getName());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("SRA123", response.getSraId());
        assertEquals("true", response.getSraRegulated());
        assertEquals("12345678", response.getCompanyNumber());
        assertEquals("https://test.org", response.getCompanyUrl());
        assertEquals(superUser, response.getSuperUser());
        assertEquals(paymentAccounts, response.getPaymentAccount());
        assertEquals(contactInfo, response.getContactInformation());
    }

    @Test
    void should_create_with_name_only_constructor() {
        OrganisationEntityResponse response = new OrganisationEntityResponse("Test Organisation");

        assertEquals("Test Organisation", response.getName());
        assertNull(response.getOrganisationIdentifier());
        assertNull(response.getStatus());
    }

    @Test
    void should_return_empty_list_when_payment_account_is_null() {
        OrganisationEntityResponse response = new OrganisationEntityResponse(
            "ORG123",
            "Test Organisation",
            "ACTIVE",
            "SRA123",
            "true",
            "12345678",
            "https://test.org",
            null,
            null,
            null
        );

        assertTrue(response.getPaymentAccount().isEmpty());
    }

    @Test
    void should_return_empty_list_when_contact_information_is_null() {
        OrganisationEntityResponse response = new OrganisationEntityResponse(
            "ORG123",
            "Test Organisation",
            "ACTIVE",
            "SRA123",
            "true",
            "12345678",
            "https://test.org",
            null,
            null,
            null
        );

        assertTrue(response.getContactInformation().isEmpty());
    }
}
