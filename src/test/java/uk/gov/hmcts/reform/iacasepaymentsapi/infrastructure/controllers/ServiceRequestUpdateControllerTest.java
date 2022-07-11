package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.CcdDataService;

@ExtendWith(MockitoExtension.class)
public class ServiceRequestUpdateControllerTest {

    @Mock private CcdDataService ccdDataService;
    @Mock private PaymentDto paymentDto;
    @Mock private ServiceRequestUpdateDto serviceRequestUpdateDto;
    private final String JURISDICTION = "IA";
    private final String CASE_TYPE = "Asylum";
    private final String CCD_CASE_NUMBER = "1111222233334444";
    private final String SERVICE_REQUEST_REFERENCE = "2020-0000000000000";
    private final String SERVICE_REQUEST_AMOUNT = "80.00";
    private final String SERVICE_REQUEST_STATUS = "paid";
    private final long CASE_ID = 1234;

    private ServiceRequestUpdateController serviceRequestUpdateController;

    @BeforeEach
    void setup() {
        serviceRequestUpdateController = new ServiceRequestUpdateController(ccdDataService);
    }

    @Test
    void should_update_the_payment_status_successfully() {

        when(serviceRequestUpdateDto.getServiceRequestStatus()).thenReturn(SERVICE_REQUEST_STATUS);
        when(serviceRequestUpdateDto.getServiceRequestReference()).thenReturn(SERVICE_REQUEST_REFERENCE);
        when(serviceRequestUpdateDto.getCcdCaseNumber()).thenReturn(CCD_CASE_NUMBER);
        when(ccdDataService.updatePaymentStatus(any(CaseMetaData.class))).thenReturn(getSubmitEventResponse());

        ResponseEntity<SubmitEventDetails> responseEntity = serviceRequestUpdateController
            .serviceRequestUpdate(serviceRequestUpdateDto);

        SubmitEventDetails response = responseEntity.getBody();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(State.APPEAL_SUBMITTED, response.getState());
        assertEquals("2020-0000000000000", response.getData().get("service_request_reference"));
        assertEquals("paid", response.getData().get("service_request_status"));
        assertEquals(200, response.getCallbackResponseStatusCode());
        assertEquals("CALLBACK_COMPLETED", response.getCallbackResponseStatus());
    }

    @Test
    void should_error_when_service_is_unavailable() {

        when(serviceRequestUpdateDto.getCcdCaseNumber()).thenReturn(CCD_CASE_NUMBER);
        when(ccdDataService.updatePaymentStatus(any(CaseMetaData.class))).thenThrow(ResponseStatusException.class);

        assertThatThrownBy(() -> serviceRequestUpdateController.serviceRequestUpdate(serviceRequestUpdateDto))
            .isExactlyInstanceOf(ResponseStatusException.class);
    }

    private SubmitEventDetails getSubmitEventResponse() {

        Map<String, Object> data = new HashMap<>();
        data.put("ccd_case_number", CCD_CASE_NUMBER);
        data.put("service_request_reference", SERVICE_REQUEST_REFERENCE);
        data.put("service_request_status", SERVICE_REQUEST_STATUS);
        data.put("service_request_amount", SERVICE_REQUEST_AMOUNT);

        return new SubmitEventDetails(CASE_ID, JURISDICTION, State.APPEAL_SUBMITTED, data,
                                      200, "CALLBACK_COMPLETED");
    }

}
