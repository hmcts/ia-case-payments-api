package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions.BadRequestException;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions.PaymentServiceRequestException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallbackControllerAdviceTest {

    private static final String TEST_CORRELATION_ID = "test-correlation-id";
    private static final String TEST_REQUEST_URI = "/asylum/ccdAboutToSubmit";

    @Mock
    private HttpServletRequest request;

    @Mock
    private ErrorResponseBuilder errorResponseBuilder;

    private CallbackControllerAdvice callbackControllerAdvice;

    @BeforeEach
    void setUp() {
        callbackControllerAdvice = new CallbackControllerAdvice(errorResponseBuilder);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, TEST_CORRELATION_ID);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void should_handle_bad_request_exception() {
        BadRequestException ex = new BadRequestException("Invalid data");
        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.BAD_REQUEST);
        when(errorResponseBuilder.build(eq(ErrorCode.BAD_REQUEST), eq(request), eq("Invalid data")))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleBadRequestException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.BAD_REQUEST), eq(request));
    }

    @Test
    void should_handle_response_status_exception_with_404() {
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Error in calling the client method:someMethod");
        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.BAD_REQUEST);
        when(errorResponseBuilder.build(eq(ErrorCode.BAD_REQUEST), eq(request), any()))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleResponseStatusException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.BAD_REQUEST), eq(request));
    }

    @Test
    void should_handle_response_status_exception_with_400() {
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Error in calling the client method:someMethod");
        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.BAD_REQUEST);
        when(errorResponseBuilder.build(eq(ErrorCode.BAD_REQUEST), eq(request), any()))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleResponseStatusException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.BAD_REQUEST), eq(request));
    }

    @Test
    void should_handle_access_denied_exception() {
        AccessDeniedException ex = new AccessDeniedException("Invalid S2S Token...");
        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.ACCESS_DENIED);
        when(errorResponseBuilder.build(eq(ErrorCode.ACCESS_DENIED), eq(request), any()))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleAccessDeniedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.ACCESS_DENIED), eq(request));
    }

    @Test
    void should_handle_authentication_exception() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.UNAUTHORIZED);
        when(errorResponseBuilder.build(eq(ErrorCode.UNAUTHORIZED), eq(request), eq(null)))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleAuthenticationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.UNAUTHORIZED), eq(request));
    }

    @Test
    void should_handle_method_argument_not_valid_exception() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.VALIDATION_ERROR);
        when(errorResponseBuilder.buildWithFieldErrors(eq(ErrorCode.VALIDATION_ERROR), eq(request), any()))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleMethodArgumentNotValidException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.VALIDATION_ERROR), eq(request));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_handle_constraint_violation_exception() {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("field");
        when(violation.getMessage()).thenReturn("must not be null");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.VALIDATION_ERROR);
        when(errorResponseBuilder.buildWithFieldErrors(eq(ErrorCode.VALIDATION_ERROR), eq(request), any()))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleConstraintViolationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.VALIDATION_ERROR), eq(request));
    }

    @Test
    void should_handle_method_argument_type_mismatch_exception() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("caseId");

        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.BAD_REQUEST);
        when(errorResponseBuilder.build(eq(ErrorCode.BAD_REQUEST), eq(request), any()))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleMethodArgumentTypeMismatchException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.BAD_REQUEST), eq(request));
    }

    @Test
    void should_handle_http_message_not_readable_exception() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.INVALID_REQUEST_BODY);
        when(errorResponseBuilder.build(eq(ErrorCode.INVALID_REQUEST_BODY), eq(request), eq(null)))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleHttpMessageNotReadableException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.INVALID_REQUEST_BODY), eq(request));
    }

    @Test
    void should_handle_http_media_type_not_supported_exception() {
        HttpMediaTypeNotSupportedException ex = mock(HttpMediaTypeNotSupportedException.class);

        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        when(errorResponseBuilder.build(eq(ErrorCode.UNSUPPORTED_MEDIA_TYPE), eq(request), eq(null)))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleHttpMediaTypeNotSupportedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.UNSUPPORTED_MEDIA_TYPE), eq(request));
    }

    @Test
    void should_handle_no_resource_found_exception() {
        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.NOT_FOUND);
        when(errorResponseBuilder.build(eq(ErrorCode.NOT_FOUND), eq(request), eq(null)))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleNoResourceFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.NOT_FOUND), eq(request));
    }

    @Test
    void should_handle_payment_service_request_exception() {
        PaymentServiceRequestException ex = new PaymentServiceRequestException("Payment failed", null);

        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.PAYMENT_SERVICE_ERROR);
        when(errorResponseBuilder.build(eq(ErrorCode.PAYMENT_SERVICE_ERROR), eq(request), eq(null)))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handlePaymentServiceRequestException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.PAYMENT_SERVICE_ERROR), eq(request));
    }

    @Test
    void should_handle_generic_exception() {
        RuntimeException ex = new RuntimeException("Unexpected error");

        ErrorResponse mockResponse = createMockErrorResponse(ErrorCode.INTERNAL_ERROR);
        when(errorResponseBuilder.build(eq(ErrorCode.INTERNAL_ERROR), eq(request), eq(null)))
            .thenReturn(mockResponse);

        ResponseEntity<ErrorResponse> response = callbackControllerAdvice
            .handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(errorResponseBuilder).logError(eq(ex), eq(ErrorCode.INTERNAL_ERROR), eq(request));
    }

    private ErrorResponse createMockErrorResponse(ErrorCode errorCode) {
        return ErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(errorCode.getDefaultMessage())
            .requestId(TEST_CORRELATION_ID)
            .path(TEST_REQUEST_URI)
            .build();
    }
}
