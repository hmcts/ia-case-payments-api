package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.refdata;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertEquals;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.RefDataApi;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "referenceData_organisationExternalUsers", port = "8991")
@ContextConfiguration(
    classes = {RefDataConsumerApplication.class}
)
@TestPropertySource(
    properties = {"rd-professional.api.url=localhost:8991"}
)
@PactFolder("pacts")
public class RefDataConsumerTest {

    @Autowired
    RefDataApi refDataApi;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";


    @Pact(provider = "referenceData_organisationExternalUsers", consumer = "ia_case_payments")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) {
        return builder
            .given("Organisation with Id exists")
            .uponReceiving("A request to get organisation")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/refdata/external/v1/organisations/pbas")
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildOrganisationResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private DslPart buildOrganisationResponseDsl() {
        return newJsonBody(o -> {
            o.object("organisationEntityResponse", or ->
                or.stringType("organisationIdentifier", "BJMSDFDS80808")
                .stringType("name", "name")
                .stringType("status", "ACTIVE")
                .stringType("sraId", "sraId")
                .stringType("sraRegulated", "TRUE")
                .stringType("companyNumber", "12345")
                .stringType("companyUrl", "www.test.com")
                .object("superUser", su -> su
                    .stringType("firstName", "firstName")
                    .stringType("lastName", "lastName")
                    .stringType("email", "emailAddress"))
                .array("paymentAccount", pa ->
                    pa.stringType("paymentAccountA1"))
                .minArrayLike("contactInformation", 1, 1,
                    sh -> {
                        sh.stringType("addressLine1", "addressLine1")
                            .stringType("addressLine2", "addressLine2")
                            .stringType("country", "UK")
                            .stringType("postCode", "SM12SX");
                    })
            );
        }).build();

    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyPactResponse() {
        OrganisationResponse response = refDataApi.findOrganisation(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN,
                                                                    ORGANISATION_EMAIL);
        assertEquals(response.getOrganisationEntityResponse().getOrganisationIdentifier(), "BJMSDFDS80808");

    }
}
