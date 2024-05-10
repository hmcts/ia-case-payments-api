package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.IdentityManagerResponseException;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.S2STokenValidator;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SystemTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SystemUserProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State.*;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State.PENDING_PAYMENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class CcdDataServiceTest {
    @Mock private CcdDataApi ccdDataApi;
    @Mock private SystemTokenGenerator systemTokenGenerator;
    @Mock private SystemUserProvider systemUserProvider;
    @Mock private AuthTokenGenerator serviceAuthorization;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private S2STokenValidator s2STokenValidator;

    private String token = "token";
    private String serviceToken = "Bearer serviceToken";
    private String userId = "userId";
    private String eventToken = "eventToken";
    private long caseId = 1234;
    private String jurisdiction = "IA";
    private String caseType = "Asylum";

    private String eventId = "updatePaymentStatus";
    private static final String VALID_S2S_TOKEN = "VALID_S2S_TOKEN";
    private static final String INVALID_S2S_TOKEN = "INVALID_S2S_TOKEN";

    private CcdDataService ccdDataService;

    @BeforeEach
    void setUp() {
        ccdDataService =
            new CcdDataService(
                ccdDataApi,
                systemTokenGenerator,
                systemUserProvider,
                serviceAuthorization,
                s2STokenValidator);
    }

    @Test
    void service_should_throw_on_unable_to_generate_system_user_token() {
        when(systemTokenGenerator.generate()).thenThrow(IdentityManagerResponseException.class);

        assertThrows(IdentityManagerResponseException.class, () -> systemTokenGenerator.generate());
    }

    @Test
    void service_should_throw_on_unable_to_generate_s2s_token() {
        when(systemTokenGenerator.generate()).thenReturn("aSystemUserToken");
        when(serviceAuthorization.generate()).thenThrow(IdentityManagerResponseException.class);

        assertThrows(IdentityManagerResponseException.class, () -> serviceAuthorization.generate());
    }

    @Test
    void service_should_update_the_payment_status_for_the_case_id() {
        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(systemUserProvider.getSystemUserId("Bearer " + token)).thenReturn(userId);

        StartEventDetails startEventResponse = getStartEventResponse("RC-1627-5070-9329-7815");
        when(ccdDataApi.startEvent(
            "Bearer " + token, serviceToken, userId, jurisdiction,  caseType,
                                   String.valueOf(caseId), eventId)).thenReturn(startEventResponse);

        CaseDataContent caseDataContent = getCaseDataContent("Success");
        when(ccdDataApi.submitEvent("Bearer " + token, serviceToken, String.valueOf(caseId),
                                    caseDataContent)).thenReturn(getSubmitEventResponse());

        SubmitEventDetails submitEventDetails =
            ccdDataService.updatePaymentStatus(
                getCaseMetaData("Success", "RC-1627-5070-9329-7815"), false, VALID_S2S_TOKEN);

        assertNotNull(submitEventDetails);
        assertEquals(caseId, submitEventDetails.getId());
        assertEquals(jurisdiction, submitEventDetails.getJurisdiction());
        assertEquals("Success", submitEventDetails.getData().get("paymentStatus"));
        assertEquals("RC-1627-5070-9329-7815", submitEventDetails.getData().get("paymentReference"));
        assertEquals(200, submitEventDetails.getCallbackResponseStatusCode());
        assertEquals("CALLBACK_COMPLETED", submitEventDetails.getCallbackResponseStatus());

        verify(ccdDataApi, times(1))
            .startEvent("Bearer " + token, serviceToken, userId,
                         jurisdiction,  caseType, String.valueOf(caseId), eventId);
        verify(ccdDataApi, times(1))
            .submitEvent("Bearer " + token, serviceToken, String.valueOf(caseId), caseDataContent);
        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);
    }

    @Test
    void service_should_error_on_incorrect_payment_reference() {

        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(systemUserProvider.getSystemUserId("Bearer " + token)).thenReturn(userId);
        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.of("RC-1627-5070-9329-7815"));

        StartEventDetails startEventResponse = getStartEventResponse("RC-1627-5070-9329-1234");
        when(
            ccdDataApi.startEvent(
                "Bearer " + token, serviceToken, userId,
                jurisdiction,  caseType, String.valueOf(caseId), eventId)).thenReturn(startEventResponse);

        ResponseStatusException rse = assertThrows(
            ResponseStatusException.class, () ->
                ccdDataService.updatePaymentStatus(
                    getCaseMetaData("Success", "RC-1627-5070-9329-7815"), false, VALID_S2S_TOKEN));

        assertThat(rse.getMessage()).contains("400 BAD_REQUEST \"Payment reference not found for the caseId: 1234\"");

        verify(ccdDataApi, times(1))
            .startEvent("Bearer " + token, serviceToken, userId,
                         jurisdiction,  caseType, String.valueOf(caseId), eventId);
        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);
    }

    @Test
    void service_should_error_on_invalid_ccd_case_reference() {

        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(systemUserProvider.getSystemUserId("Bearer " + token)).thenReturn(userId);

        when(
            ccdDataApi.startEvent(
                "Bearer " + token, serviceToken, userId,
                jurisdiction,  caseType, String.valueOf(caseId), eventId)).thenThrow(FeignException.class);

        FeignException fe = assertThrows(FeignException.class, () -> ccdDataService.updatePaymentStatus(
            getCaseMetaData("Success", "RC-1627-5070-9329-7815"), false, VALID_S2S_TOKEN));
        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);
    }

    @Test
    void service_should_not_seek_payment_reference_if_is_waysToPay() {
        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(systemUserProvider.getSystemUserId("Bearer " + token)).thenReturn(userId);

        StartEventDetails startEventResponse = getStartEventResponse("RC-1627-5070-9329-7815");
        when(ccdDataApi.startEvent(
            "Bearer " + token, serviceToken, userId, jurisdiction,  caseType,
            String.valueOf(caseId), eventId)).thenReturn(startEventResponse);

        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.empty());

        CaseDataContent caseDataContent = getCaseDataContent("Paid");
        when(ccdDataApi.submitEvent("Bearer " + token, serviceToken, String.valueOf(caseId),
                                    caseDataContent)).thenReturn(getSubmitEventResponse());

        SubmitEventDetails submitEventDetails =
            assertDoesNotThrow(() -> ccdDataService.updatePaymentStatus(
                getCaseMetaData("Paid", "RC-1627-5070-9329-7815"), true, VALID_S2S_TOKEN));

        assertNotNull(submitEventDetails);
        assertEquals(caseId, submitEventDetails.getId());
        assertEquals(jurisdiction, submitEventDetails.getJurisdiction());
        assertEquals("Success", submitEventDetails.getData().get("paymentStatus"));
        assertEquals("RC-1627-5070-9329-7815", submitEventDetails.getData().get("paymentReference"));
        assertEquals(200, submitEventDetails.getCallbackResponseStatusCode());
        assertEquals("CALLBACK_COMPLETED", submitEventDetails.getCallbackResponseStatus());

        verify(ccdDataApi, times(1))
            .startEvent("Bearer " + token, serviceToken, userId,
                        jurisdiction,  caseType, String.valueOf(caseId), eventId);
        verify(ccdDataApi, times(1))
            .submitEvent("Bearer " + token, serviceToken, String.valueOf(caseId), caseDataContent);
        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);
    }

    @Test
    void service_should_update_payment_if_is_waysToPay() {
        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(systemUserProvider.getSystemUserId("Bearer " + token)).thenReturn(userId);

        StartEventDetails startEventResponse = getStartEventResponse("RC-1627-5070-9329-7815");
        when(ccdDataApi.startEvent(
            "Bearer " + token, serviceToken, userId, jurisdiction,  caseType,
            String.valueOf(caseId), eventId)).thenReturn(startEventResponse);

        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.empty());

        CaseDataContent caseDataContent = getCaseDataContent("Paid");
        when(ccdDataApi.submitEvent("Bearer " + token, serviceToken, String.valueOf(caseId),
                                    caseDataContent)).thenReturn(getSubmitEventResponse());

        SubmitEventDetails submitEventDetails =
            assertDoesNotThrow(() -> ccdDataService.updatePaymentStatus(
                getCaseMetaData("Paid", "RC-1627-5070-9329-7815"), true, VALID_S2S_TOKEN));

        assertNotNull(submitEventDetails);
        assertEquals(caseId, submitEventDetails.getId());
        assertEquals(jurisdiction, submitEventDetails.getJurisdiction());
        assertEquals("Success", submitEventDetails.getData().get("paymentStatus"));
        assertEquals("RC-1627-5070-9329-7815", submitEventDetails.getData().get("paymentReference"));
        assertEquals(200, submitEventDetails.getCallbackResponseStatusCode());
        assertEquals("CALLBACK_COMPLETED", submitEventDetails.getCallbackResponseStatus());

        verify(ccdDataApi, times(1))
            .startEvent("Bearer " + token, serviceToken, userId,
                        jurisdiction,  caseType, String.valueOf(caseId), eventId);
        verify(ccdDataApi, times(1))
            .submitEvent("Bearer " + token, serviceToken, String.valueOf(caseId), caseDataContent);
        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);
    }

    @Test
    void service_should_throw_exception_from_invalid_s2s_token() {
        doThrow(AccessDeniedException.class).when(s2STokenValidator).checkIfServiceIsAllowed(INVALID_S2S_TOKEN);

        assertThrows(AccessDeniedException.class, () -> ccdDataService.updatePaymentStatus(
            getCaseMetaData("Paid", "RC-1627-5070-9329-7815"), false, INVALID_S2S_TOKEN));

        verify(s2STokenValidator).checkIfServiceIsAllowed(INVALID_S2S_TOKEN);
    }

    @Test
    void service_should_throw_exception_if_no_appeal_type() {
        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(systemUserProvider.getSystemUserId("Bearer " + token)).thenReturn(userId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        StartEventDetails startEventResponse = new StartEventDetails(Event.UPDATE_PAYMENT_STATUS, eventToken, caseDetails);
        when(ccdDataApi.startEvent(
            "Bearer " + token, serviceToken, userId, jurisdiction,  caseType,
            String.valueOf(caseId), eventId)).thenReturn(startEventResponse);
        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.empty());
        CaseDataContent caseDataContent = getCaseDataContent("Paid");
        when(ccdDataApi.submitEvent("Bearer " + token, serviceToken, String.valueOf(caseId),
                                    caseDataContent)).thenReturn(getSubmitEventResponse());

        assertThrows(IllegalStateException.class, () -> ccdDataService.updatePaymentStatus(
            getCaseMetaData("Paid", "RC-1627-5070-9329-7815"), true, VALID_S2S_TOKEN), "No appeal type in case data for case: " + caseId);
    }

    @ParameterizedTest
    @EnumSource(State.class)
    void service_should_not_throw_exception_if_validation_passes_PA_pay_later(State state) {
        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(systemUserProvider.getSystemUserId("Bearer " + token)).thenReturn(userId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        StartEventDetails startEventResponse = new StartEventDetails(Event.UPDATE_PAYMENT_STATUS, eventToken, caseDetails);
        when(ccdDataApi.startEvent(
            "Bearer " + token, serviceToken, userId, jurisdiction,  caseType,
            String.valueOf(caseId), eventId)).thenReturn(startEventResponse);
        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.empty());
        CaseDataContent caseDataContent = getCaseDataContent("Paid");
        when(ccdDataApi.submitEvent("Bearer " + token, serviceToken, String.valueOf(caseId),
                                    caseDataContent)).thenReturn(getSubmitEventResponse());
        when(caseDetails.getState()).thenReturn(state);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("none"));
        assertDoesNotThrow(() -> ccdDataService.updatePaymentStatus(
            getCaseMetaData("Paid", "RC-1627-5070-9329-7815"), true, VALID_S2S_TOKEN));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("none"));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        assertDoesNotThrow(() -> ccdDataService.updatePaymentStatus(
            getCaseMetaData("Paid", "RC-1627-5070-9329-7815"), true, VALID_S2S_TOKEN));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = { "APPEAL_STARTED", "APPEAL_SUBMITTED", "APPEAL_STARTED_BY_ADMIN", "PENDING_PAYMENT" })
    void service_should_not_throw_exception_if_validation_passes_non_PA_pay_later(State state) {
        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(systemUserProvider.getSystemUserId("Bearer " + token)).thenReturn(userId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        StartEventDetails startEventResponse = new StartEventDetails(Event.UPDATE_PAYMENT_STATUS, eventToken, caseDetails);
        when(ccdDataApi.startEvent(
            "Bearer " + token, serviceToken, userId, jurisdiction,  caseType,
            String.valueOf(caseId), eventId)).thenReturn(startEventResponse);
        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.empty());
        CaseDataContent caseDataContent = getCaseDataContent("Paid");
        when(ccdDataApi.submitEvent("Bearer " + token, serviceToken, String.valueOf(caseId),
                                    caseDataContent)).thenReturn(getSubmitEventResponse());
        for (AppealType appealType : AppealType.values() ) {
            when(caseDetails.getState()).thenReturn(state);
            when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
            when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("none"));
            when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("none"));
            assertDoesNotThrow(() -> ccdDataService.updatePaymentStatus(
                getCaseMetaData("Paid", "RC-1627-5070-9329-7815"), true, VALID_S2S_TOKEN));
        }
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = { "APPEAL_STARTED", "APPEAL_SUBMITTED", "APPEAL_STARTED_BY_ADMIN", "PENDING_PAYMENT" }, mode = EnumSource.Mode.EXCLUDE)
    void service_should_throw_exception_if_validation_fails(State state) {
        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(systemUserProvider.getSystemUserId("Bearer " + token)).thenReturn(userId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        StartEventDetails startEventResponse = new StartEventDetails(Event.UPDATE_PAYMENT_STATUS, eventToken, caseDetails);
        when(ccdDataApi.startEvent(
            "Bearer " + token, serviceToken, userId, jurisdiction,  caseType,
            String.valueOf(caseId), eventId)).thenReturn(startEventResponse);
        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.empty());
        CaseDataContent caseDataContent = getCaseDataContent("Paid");
        when(ccdDataApi.submitEvent("Bearer " + token, serviceToken, String.valueOf(caseId),
                                    caseDataContent)).thenReturn(getSubmitEventResponse());
        for (AppealType appealType : AppealType.values() ) {
            when(caseDetails.getState()).thenReturn(state);
            when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
            when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("none"));
            when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("none"));
            assertThrows(IllegalStateException.class, () -> ccdDataService.updatePaymentStatus(
                 getCaseMetaData("Paid", "RC-1627-5070-9329-7815"), true, VALID_S2S_TOKEN),
             appealType.getValue() + " appeal payment should not be made at " + state.toString() + " state for case: " + caseId);
        }
    }

    private StartEventDetails getStartEventResponse(String paymentReference) {

        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(caseDetails.getJurisdiction()).thenReturn(jurisdiction);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HU/50004/2021"));
        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.of(paymentReference));
        when(asylumCase.read(PAYMENT_STATUS, String.class)).thenReturn(Optional.of("Failed"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));

        return new StartEventDetails(Event.UPDATE_PAYMENT_STATUS, eventToken, caseDetails);
    }

    private SubmitEventDetails getSubmitEventResponse() {

        Map<String, Object> data = new HashMap<>();
        data.put("appealReferenceNumber", "HU/50004/2021");
        data.put("paymentReference", "RC-1627-5070-9329-7815");
        data.put(PAYMENT_STATUS.value(), "Success");

        return new SubmitEventDetails(caseId, jurisdiction, State.APPEAL_SUBMITTED, data,
                                      200, "CALLBACK_COMPLETED");
    }

    private CaseDataContent getCaseDataContent(String paymentStatus) {

        Map<String, Object> data = new HashMap<>();
        data.put(PAYMENT_STATUS.value(), paymentStatus);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", Event.UPDATE_PAYMENT_STATUS.toString());

        return new CaseDataContent(String.valueOf(caseId), data, eventData, eventToken, true);
    }

    private CaseMetaData getCaseMetaData(String paymentStatus, String paymentReference) {

        return new CaseMetaData(Event.UPDATE_PAYMENT_STATUS, jurisdiction, caseType, caseId,
                                paymentStatus, paymentReference);
    }

}
