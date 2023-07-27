package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers.UpdatePaymentStatusIntegrationTest.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers.UpdatePaymentStatusIntegrationTest.PAYMENT_STATUS_UPDATE_CASE_REFERENCE;

public interface WithCcdStub {

    ObjectMapper mapper = new ObjectMapper();

    default void addCcdUpdatePaymentStatusGetTokenStub(WireMockServer server) throws JsonProcessingException {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(GET, urlEqualTo("/ccd/caseworkers//jurisdictions/IA/case-types"
                        + "/Asylum/cases/" + CCD_CASE_NUMBER + "/event-triggers/updatePaymentStatus"
                        + "/token?ignore-warning=true"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mapper.writeValueAsString(
                        StartEventDetailsForTest.generateValidUpdatePaymentStatusDetail()
                    ))
                    .build()
            )
        );
    }

    default void addCcdUpdatePaymentStatusEventStub(WireMockServer server) throws JsonProcessingException {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(POST, urlEqualTo("/ccd/cases/" + CCD_CASE_NUMBER + "/events"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mapper.writeValueAsString(
                        CaseDataContentForTest.generateValidUpdatePaymentStatus(
                            "Success",
                            PAYMENT_STATUS_UPDATE_CASE_REFERENCE,
                            "integrationCcdEventToken"
                        )))
                    .build()
            )
        );
    }

}
