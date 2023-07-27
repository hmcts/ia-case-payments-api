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

public interface WithCcdStub {

    ObjectMapper mapper = new ObjectMapper();

    default void addCcdUpdatePaymentStatusGetTokenStub(WireMockServer server) throws JsonProcessingException {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(GET, urlEqualTo("/ccd/caseworkers//jurisdictions/IA/case-types"
                        + "/Asylum/cases/1627506765384547/event-triggers/updatePaymentStatus"
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
                newRequestPattern(POST, urlEqualTo("/ccd/cases/1627506765384547/events"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mapper.writeValueAsString(
                        CaseDataContentForTest.generateValidUpdatePaymentStatus(
                            "Success",
                            "RC-1627-5070-9329-7815",
                            "integrationCcdEventToken"
                        )))
                    .build()
            )
        );
    }

}
