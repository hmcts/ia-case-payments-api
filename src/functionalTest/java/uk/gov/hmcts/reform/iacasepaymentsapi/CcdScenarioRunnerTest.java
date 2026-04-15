package uk.gov.hmcts.reform.iacasepaymentsapi;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.util.AuthorizationHeadersProvider;
import uk.gov.hmcts.reform.iacasepaymentsapi.util.MapMerger;
import uk.gov.hmcts.reform.iacasepaymentsapi.util.MapSerializer;
import uk.gov.hmcts.reform.iacasepaymentsapi.util.MapValueExpander;
import uk.gov.hmcts.reform.iacasepaymentsapi.util.MapValueExtractor;
import uk.gov.hmcts.reform.iacasepaymentsapi.util.StringResourceLoader;
import uk.gov.hmcts.reform.iacasepaymentsapi.verifiers.Verifier;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CcdScenarioRunnerTest {

    @Value("${targetInstance}")
    private String targetInstance;

    @Autowired
    private Environment environment;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private List<Verifier> verifiers;

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    private Map<String, Object> actualResponse = null;

    @BeforeAll
    public void beforeAll() {
        MapSerializer.setObjectMapper(objectMapper);
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
        loadPropertiesIntoMapValueExpander();
        assertFalse(
            "Verifiers are configured",
            verifiers.isEmpty()
        );
    }

    private Stream<Arguments> scenarioSources() throws IOException {
        String scenarioPattern = System.getProperty("scenario");
        if (scenarioPattern == null) {
            scenarioPattern = "*.json";
        } else {
            scenarioPattern = "*" + scenarioPattern + "*.json";
        }

        Collection<String> scenarioSources =
            StringResourceLoader
                .load("/scenarios/" + scenarioPattern)
                .values();
        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        System.out.println((char) 27 + "[33m" + "RUNNING " + scenarioSources.size() + " SCENARIOS");
        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        return scenarioSources.stream().map(scenarioSource -> {
            try {
                Map<String, Object> scenario = MapSerializer.deserialize(scenarioSource);

                String description = MapValueExtractor.extract(scenario, "description");

                Object scenarioDisabled = MapValueExtractor.extractOrDefault(scenario, "disabled", false);
                if (Boolean.parseBoolean(scenarioDisabled.toString())) {
                    return Arguments.of("Disabled: " + description, null, null, null, null, 0, 0, null);
                }

                System.out.println((char) 27 + "[33m" + "SCENARIO: " + description);

                Map<String, String> templatesByFilename = StringResourceLoader.load("/templates/*.json");

                final long scenarioTestCaseId = MapValueExtractor.extractOrDefault(
                    scenario,
                    "request.input.id",
                    -1
                );

                final long testCaseId = (scenarioTestCaseId == -1)
                    ? ThreadLocalRandom.current().nextLong(1111111111111111L, 1999999999999999L)
                    : scenarioTestCaseId;

                final String requestBody = buildCallbackBody(
                    testCaseId,
                    MapValueExtractor.extract(scenario, "request.input"),
                    templatesByFilename
                );

                final String requestUri = MapValueExtractor.extract(scenario, "request.uri");
                final int expectedStatus = MapValueExtractor.extractOrDefault(scenario, "expectation.status", 200);
                final String credentials = MapValueExtractor.extractOrDefault(scenario, "request.credentials", "none");
                final Headers authorizationHeaders = getAuthorizationHeaders(credentials);
                String expectedResponseBody = buildCallbackResponseBody(
                    MapValueExtractor.extract(scenario, "expectation"),
                    templatesByFilename
                );
                Map<String, Object> expectedResponse = MapSerializer.deserialize(expectedResponseBody);
                return Arguments.of(
                    description,
                    scenario,
                    authorizationHeaders,
                    requestBody,
                    requestUri,
                    expectedStatus,
                    testCaseId,
                    expectedResponse
                );

            } catch (IOException e) {
                System.out.println("Failed to load scenario" + e);
                return null;
            }
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarioSources")
    public void scenarios_should_behave_as_specified(String description,
                                                     Map<String, Object> scenario,
                                                     Headers authorizationHeaders,
                                                     String requestBody,
                                                     String requestUri,
                                                     int expectedStatus,
                                                     long testCaseId,
                                                     Map<String, Object> expectedResponse) throws IOException {
        int maxRetries = 3;
        assumeFalse(description.startsWith("Disabled:"),
                    "Test skipped because description starts with 'Disabled:'");
        for (int i = 0; i < maxRetries; i++) {
            try {
                actualResponse = null;
                String actualResponseBody =
                    SerenityRest
                        .given()
                        .headers(authorizationHeaders)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .body(requestBody)
                        .when()
                        .post(requestUri)
                        .then()
                        .statusCode(expectedStatus)
                        .and()
                        .extract()
                        .body()
                        .asString();

                actualResponse = MapSerializer.deserialize(actualResponseBody);

                verifiers.forEach(verifier ->
                                      verifier.verify(
                                          testCaseId,
                                          scenario,
                                          expectedResponse,
                                          actualResponse
                                      )
                );
                break;
            } catch (Error | RetryableException | NullPointerException e) {
                System.out.println("Scenario failed with error " + e.getMessage());
                if (actualResponse != null) {
                    System.out.println("actualResponse: " + objectMapper.writeValueAsString(actualResponse));
                    System.out.println("expectedResponse: " + objectMapper.writeValueAsString(expectedResponse));
                }
                if (i == maxRetries - 1) {
                    throw e;
                }
            }
        }
    }

    private void loadPropertiesIntoMapValueExpander() {

        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
        StreamSupport
            .stream(propertySources.spliterator(), false)
            .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
            .map(propertySource -> ((EnumerablePropertySource) propertySource).getPropertyNames())
            .flatMap(Arrays::stream)
            .filter(name -> environment.getProperty(name) != null)
            .forEach(name -> MapValueExpander.ENVIRONMENT_PROPERTIES.setProperty(name, environment.getProperty(name)));
    }

    private Map<String, Object> buildCaseData(
        Map<String, Object> caseDataInput,
        Map<String, String> templatesByFilename
    ) throws IOException {

        String templateFilename = MapValueExtractor.extract(caseDataInput, "template");

        Map<String, Object> caseData = MapSerializer.deserialize(templatesByFilename.get(templateFilename));
        Map<String, Object> caseDataReplacements = MapValueExtractor.extract(caseDataInput, "replacements");
        if (caseDataReplacements != null) {
            MapMerger.merge(caseData, caseDataReplacements);
        }

        return caseData;
    }

    private String buildCallbackBody(
        long testCaseId,
        Map<String, Object> input,
        Map<String, String> templatesByFilename
    ) throws IOException {

        Map<String, Object> caseData = buildCaseData(
            MapValueExtractor.extract(input, "caseData"),
            templatesByFilename
        );

        LocalDateTime createdDate =
            LocalDateTime.parse(
                MapValueExtractor.extractOrDefault(input, "createdDate", LocalDateTime.now().toString())
            );

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("id", testCaseId);
        caseDetails.put("jurisdiction", MapValueExtractor.extractOrDefault(input, "jurisdiction", "IA"));
        caseDetails.put("state", MapValueExtractor.extractOrThrow(input, "state"));
        caseDetails.put("created_date", createdDate);
        caseDetails.put("case_data", caseData);

        Map<String, Object> callback = new HashMap<>();
        callback.put("event_id", MapValueExtractor.extractOrThrow(input, "eventId"));
        callback.put("case_details", caseDetails);

        if (input.containsKey("caseDataBefore")) {
            Map<String, Object> caseDataBefore = buildCaseData(
                MapValueExtractor.extract(input, "caseDataBefore"),
                templatesByFilename
            );

            Map<String, Object> caseDetailsBefore = new HashMap<>();
            caseDetailsBefore.put("id", testCaseId);
            caseDetailsBefore.put("jurisdiction", MapValueExtractor.extractOrDefault(input, "jurisdiction", "IA"));
            caseDetailsBefore.put("state", MapValueExtractor.extractOrThrow(input, "state"));
            caseDetailsBefore.put("created_date", createdDate);
            caseDetailsBefore.put("case_data", caseDataBefore);
            callback.put("case_details_before", caseDetailsBefore);
        }

        return MapSerializer.serialize(callback);
    }

    private String buildCallbackResponseBody(
        Map<String, Object> expectation,
        Map<String, String> templatesByFilename
    ) throws IOException {

        if (MapValueExtractor.extract(expectation, "confirmation") != null) {

            final Map<String, Object> callbackResponse = new HashMap<>();

            callbackResponse.put("confirmation_header", MapValueExtractor.extract(expectation, "confirmation.header"));
            callbackResponse.put("confirmation_body", MapValueExtractor.extract(expectation, "confirmation.body"));

            return MapSerializer.serialize(callbackResponse);

        } else {

            Map<String, Object> caseData = buildCaseData(
                MapValueExtractor.extract(expectation, "caseData"),
                templatesByFilename
            );

            PreSubmitCallbackResponse<AsylumCase> preSubmitCallbackResponse =
                new PreSubmitCallbackResponse<>(
                    objectMapper.readValue(
                        MapSerializer.serialize(caseData),
                        new TypeReference<AsylumCase>() {
                        }
                    )
                );

            preSubmitCallbackResponse.addErrors(MapValueExtractor.extract(expectation, "errors"));

            return objectMapper.writeValueAsString(preSubmitCallbackResponse);
        }
    }

    private Headers getAuthorizationHeaders(String credentials) {
        if (credentials.equalsIgnoreCase("LegalRepresentative")) {

            return authorizationHeadersProvider
                .getLegalRepresentativeAuthorization();
        }

        if (credentials.equalsIgnoreCase("LegalRepresentativeOrgSuccess")) {

            return authorizationHeadersProvider
                .getLegalRepresentativeOrgSuccessAuthorization();
        }

        if (credentials.equalsIgnoreCase("LegalRepresentativeOrgDeleted")) {

            return authorizationHeadersProvider
                .getLegalRepresentativeOrgDeletedAuthorization();
        }

        if (credentials.equalsIgnoreCase("Citizen")) {
            return authorizationHeadersProvider
                .getCitizenAuthorization();
        }

        return new Headers();
    }
}
