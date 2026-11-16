package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.Application;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentGroupDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentGroupsResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.ServiceRequestApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.RequestUserAccessTokenProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("integration")
public class GetExistingServiceRequestIntegrationTest {

    private static final long CASE_ID = 1111222233334444L;
    private static final String FEE_CODE = "FEE0001";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ServiceRequestService serviceRequestService;

    @MockitoBean
    private ServiceRequestApi serviceRequestApi;

    @MockitoBean
    private RequestUserAccessTokenProvider userAuthorizationProvider;

    @MockitoBean
    private AuthTokenGenerator serviceAuthorization;

    @MockitoBean
    private Callback<AsylumCase> callback;

    @MockitoBean
    private CaseDetails<AsylumCase> caseDetails;

    private final String token = "token";
    private final String serviceToken = "Bearer serviceToken";
    private final Fee fee = mock(Fee.class);

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(userAuthorizationProvider.getAccessToken()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(fee.getCode()).thenReturn(FEE_CODE);
    }

    @Test
    public void shouldReturnExistingServiceRequestWhenUnpaidExists() {
        FeeDto feeDto = FeeDto.builder().code(FEE_CODE).build();
        PaymentGroupDto paymentGroup = PaymentGroupDto.builder()
            .paymentGroupReference("existing-ref-123")
            .serviceRequestStatus("Not paid")
            .fees(List.of(feeDto))
            .build();
        PaymentGroupsResponse response = PaymentGroupsResponse.builder()
            .paymentGroups(List.of(paymentGroup))
            .build();

        when(serviceRequestApi.getServiceRequests(token, serviceToken, String.valueOf(CASE_ID)))
            .thenReturn(response);

        Optional<ServiceRequestResponse> result = serviceRequestService.getExistingUnpaidServiceRequest(callback, fee);

        assertTrue(result.isPresent());
        assertEquals("existing-ref-123", result.get().getServiceRequestReference());
    }

    @Test
    public void shouldReturnEmptyWhenNoPaymentGroupsExist() {
        when(serviceRequestApi.getServiceRequests(token, serviceToken, String.valueOf(CASE_ID)))
            .thenReturn(null);

        Optional<ServiceRequestResponse> result = serviceRequestService.getExistingUnpaidServiceRequest(callback, fee);

        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyWhenAllPaymentGroupsArePaid() {
        FeeDto feeDto = FeeDto.builder().code(FEE_CODE).build();
        PaymentGroupDto paymentGroup = PaymentGroupDto.builder()
            .paymentGroupReference("paid-ref-123")
            .serviceRequestStatus("Paid")
            .fees(List.of(feeDto))
            .build();
        PaymentGroupsResponse response = PaymentGroupsResponse.builder()
            .paymentGroups(List.of(paymentGroup))
            .build();

        when(serviceRequestApi.getServiceRequests(token, serviceToken, String.valueOf(CASE_ID)))
            .thenReturn(response);

        Optional<ServiceRequestResponse> result = serviceRequestService.getExistingUnpaidServiceRequest(callback, fee);

        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyWhenApiReturns404NotFound() {
        Request request = Request.create(
            Request.HttpMethod.GET,
            "/cases/1111222233334444/paymentgroups",
            Collections.emptyMap(),
            null,
            new RequestTemplate()
        );

        when(serviceRequestApi.getServiceRequests(anyString(), anyString(), anyString()))
            .thenThrow(new FeignException.NotFound("Not Found", request, null, null));

        Optional<ServiceRequestResponse> result = serviceRequestService.getExistingUnpaidServiceRequest(callback, fee);

        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyWhenApiReturns500InternalServerError() {
        Request request = Request.create(
            Request.HttpMethod.GET,
            "/cases/1111222233334444/paymentgroups",
            Collections.emptyMap(),
            null,
            new RequestTemplate()
        );

        when(serviceRequestApi.getServiceRequests(anyString(), anyString(), anyString()))
            .thenThrow(new FeignException.InternalServerError("Internal Server Error", request, null, null));

        Optional<ServiceRequestResponse> result = serviceRequestService.getExistingUnpaidServiceRequest(callback, fee);

        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyWhenApiReturns503ServiceUnavailable() {
        Request request = Request.create(
            Request.HttpMethod.GET,
            "/cases/1111222233334444/paymentgroups",
            Collections.emptyMap(),
            null,
            new RequestTemplate()
        );

        when(serviceRequestApi.getServiceRequests(anyString(), anyString(), anyString()))
            .thenThrow(new FeignException.ServiceUnavailable("Service Unavailable", request, null, null));

        Optional<ServiceRequestResponse> result = serviceRequestService.getExistingUnpaidServiceRequest(callback, fee);

        assertTrue(result.isEmpty());
    }
}
