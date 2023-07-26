package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.S2STokenValidator;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.CcdDataService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions.BadRequestException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentStatusControllerTest {

    @Mock
    private CcdDataService ccdDataService;

    @Mock
    private S2STokenValidator s2STokenValidator;

    @Mock
    private PaymentDto paymentDto;

    private final String jurisdiction = "IA";
    private final String caseType = "Asylum";
    private final long caseId = 1234;
    private static final String VALID_S2S_TOKEN = "VALID_S2S_TOKEN";
    private static final String INVALID_S2S_TOKEN = "INVALID_S2S_TOKEN";

    private UpdatePaymentStatusController updatePaymentStatusController;

    @BeforeEach
    void setUp() {
        updatePaymentStatusController = new UpdatePaymentStatusController(ccdDataService, s2STokenValidator);
    }

    @Test
    void should_update_the_payment_status_successfully() {
        when(paymentDto.getCcdCaseNumber()).thenReturn("1234");
        when(ccdDataService.updatePaymentStatus(any(CaseMetaData.class), eq(false)))
            .thenReturn(getSubmitEventResponse());

        ResponseEntity<SubmitEventDetails> responseEntity = updatePaymentStatusController.updatePaymentStatus(VALID_S2S_TOKEN, paymentDto);

        assertNotNull(responseEntity);
        SubmitEventDetails response = responseEntity.getBody();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(State.APPEAL_SUBMITTED, response.getState());
        assertEquals("RC-1234", response.getData().get("paymentReference"));
        assertEquals("Success", response.getData().get("paymentStatus"));
        assertEquals(200, response.getCallbackResponseStatusCode());
        assertEquals("CALLBACK_COMPLETED", response.getCallbackResponseStatus());

        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);
    }

    @Test
    void should_error_when_the_s2s_token_is_invalid() {
        doThrow(AccessDeniedException.class).when(s2STokenValidator).checkIfServiceIsAllowed(INVALID_S2S_TOKEN);
        assertThrows(AccessDeniedException.class, () -> updatePaymentStatusController.updatePaymentStatus(INVALID_S2S_TOKEN, paymentDto));
        verify(s2STokenValidator).checkIfServiceIsAllowed(INVALID_S2S_TOKEN);
    }

    @Test
    void should_error_when_the_s2s_token_is_null() {
        doThrow(AccessDeniedException.class).when(s2STokenValidator).checkIfServiceIsAllowed(null);
        assertThrows(AccessDeniedException.class, () -> updatePaymentStatusController.updatePaymentStatus(null, paymentDto));
        verify(s2STokenValidator).checkIfServiceIsAllowed(null);
    }

    @Test
    void should_error_when_service_is_unavailable() {

        when(paymentDto.getCcdCaseNumber()).thenReturn("1234");
        when(ccdDataService.updatePaymentStatus(any(CaseMetaData.class), eq(false)))
            .thenThrow(ResponseStatusException.class);

        assertThatThrownBy(() -> updatePaymentStatusController.updatePaymentStatus(VALID_S2S_TOKEN, paymentDto))
            .isExactlyInstanceOf(ResponseStatusException.class);

        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);
    }

    @Test
    void should_error_on_invalid_ccd_case_number() {

        when(paymentDto.getCcdCaseNumber()).thenReturn("1001");
        when(ccdDataService.updatePaymentStatus(any(CaseMetaData.class), eq(false)))
            .thenThrow(BadRequestException.class);

        assertThatThrownBy(() -> updatePaymentStatusController.updatePaymentStatus(VALID_S2S_TOKEN, paymentDto))
            .isExactlyInstanceOf(BadRequestException.class);

        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);

    }

    private SubmitEventDetails getSubmitEventResponse() {

        Map<String, Object> data = new HashMap<>();
        data.put("appealReferenceNumber", "HU/50004/2021");
        data.put("paymentReference", "RC-1234");
        data.put("paymentStatus", "Success");

        return new SubmitEventDetails(1234, jurisdiction, State.APPEAL_SUBMITTED, data,
                                      200, "CALLBACK_COMPLETED");
    }
}
