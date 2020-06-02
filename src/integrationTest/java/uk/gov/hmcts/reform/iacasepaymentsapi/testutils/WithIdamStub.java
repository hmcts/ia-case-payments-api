package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface WithIdamStub {

    String SYSTEM_USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQw"
        + "PSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJjY2QtaW1wb3J0QGZha2UuaG1jdHMubmV0IiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja"
        + "2luZ0lkIjoiNjk0MjgxZmQtNzk5NS00N2FlLTlhM2QtMzk0M2IwYmM1NjY3IiwiaXNzIjoiaHR0cDovL2ZyLWFtOjgwODAvb3BlbmFtL"
        + "29hdXRoMi9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI"
        + "6ImE3M2I5YmNkLWFmYmItNDI0Mi1iNDZkLTg2NGM4MTE2ODZkYSIsImF1ZCI6ImNjZF9nYXRld2F5IiwibmJmIjoxNTg4NjY5MTYzLC"
        + "JncmFudF90eXBlIjoicGFzc3dvcmQiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiXSwiYXV0aF90aW1lIjoxNTg4NjY"
        + "5MTYzLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU4ODY5Nzk2MywiaWF0IjoxNTg4NjY5MTYzLCJleHBpcmVzX2luIjoyODgwMCwian"
        + "RpIjoiNjc1YzY2OWYtYzQ3YS00YjczLWI0NmItMDc1YzM5M2RjMmFkIn0.YZabJX_gnhnWB2k8bmOfB_xPLEc-OLgJ9Dj9J-BWvgW"
        + "QAlWQNODfnItVDpTbFsX_FeNu7ivSd0IUtmL_2H-iaMEo0taGGUBl7Pewf28KXc3m3rb1lfm2083moKuYmtC23nH8XoUPyfdo2EwPg"
        + "Qa31nvwQMlCPNvEzKMAcpAZPWNDk4mPE-VMOrcvJwUan-mOEia4O6VotHU0VbPiPrubZG_PsyJVGJUVVWgmBqGk7WK_jfYfza4cbTGE"
        + "L4eflYlqNUMmyM-wGd4ldtzaMdeuZvwByERymihnGu-yF7ZG5u1zr2pCvqTjH0Wgia7ToPhGH_vudoj-cKui-U5JGl6_Uw";

    String USER_ID = "49154ae9-47be-4469-9edd-d43f68d245f0";

    default void addIdamTokenStub(WireMockServer server) {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/idam/o/token"))
                    .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
                    .withRequestBody(
                        equalTo("grant_type=password"
                                + "&redirect_uri=http%3A%2F%2Flocalhost%3A3002%2Foauth2%2Fcallback"
                                + "&client_id=ia"
                                + "&client_secret=something"
                                + "&username=ia-system-user%40fake.hmcts.net"
                                + "&password=London05"
                                + "&scope=openid+profile+roles"
                        )
                    )
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"access_token\": \"" + SYSTEM_USER_TOKEN + "\"}")
                    .build()
            )
        );
    }

    default void addUserInfoStub(WireMockServer server) {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo("/idam/o/userinfo"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Authorization", equalTo("Bearer " + SYSTEM_USER_TOKEN))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"uid\": \"" + USER_ID + "\"}")
                    .build()
            )
        );
    }
}
