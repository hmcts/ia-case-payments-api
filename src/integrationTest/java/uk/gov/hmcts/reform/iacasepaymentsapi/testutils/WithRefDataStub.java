package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface WithRefDataStub {

    default void addRefDataStub(WireMockServer server) {

        String refDataUrl = "/refdata/external/v1/organisations/pbas";

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo(refDataUrl))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"name\": \"ia-legal-rep-org\",\n"
                                  + "  \"paymentAccount\": \"[PBA1234567, PBA7654321]\" }")
                    .build()
            )
        );
    }

}
