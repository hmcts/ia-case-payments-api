package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers.UpdatePaymentStatusIntegrationTest.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.SubmitEventDetailsForTest.generateValidPaymentUpdateEvent;

public interface WithPaymentStub {

    ObjectMapper mapper = new ObjectMapper();

    default void addPaymentStub(WireMockServer server) {

        String paymentUrl = "/payment/credit-account-payments";

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(POST, urlEqualTo(paymentUrl))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\": \"Success\",\n"
                                  + "  \"reference\": \"RC-1590-6786-1063-9996\" ,"
                                  + "  \"date_created\": \"2020-05-29T15:10:10.694+0000\"  }")
                    .build()
            )
        );
    }

    default void addPaymentUpdateSubmitStub(WireMockServer server) throws JsonProcessingException {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(POST, urlEqualTo("/ccd/cases/" + CCD_CASE_NUMBER + "/events"))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                    .withHeader(AUTHORIZATION, equalTo("Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre"))
                    .withHeader(SERVICE_AUTHORIZATION,
                                equalTo("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
                                                        + "eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ."
                                                        + "L8i6g3PfcHlioHCCPURC9pmXT7gdJpx3kOoyAfNUwCc"))
                    .withHeader("experimental", equalTo("true"))
                    .withHeader(CONTENT_LENGTH, equalTo("172"))
                    .withRequestBody(equalTo(
                        "{\"case_reference\":\"" + CCD_CASE_NUMBER + "\"," +
                            "\"data\":{\"paymentStatus\":\"Success\"}," +
                            "\"event\":{\"id\":\"updatePaymentStatus\"}," +
                            "\"event_token\":\"ccdIntegrationEventToken\"," +
                            "\"ignore_warning\":true}"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mapper.writeValueAsString(generateValidPaymentUpdateEvent()))
                    .build()
            )
        );
    }

}
