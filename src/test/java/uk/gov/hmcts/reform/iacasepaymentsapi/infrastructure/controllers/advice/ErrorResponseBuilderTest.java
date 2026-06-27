package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorResponseBuilderTest {

    private static final String TEST_CORRELATION_ID = "test-correlation-id-123";
    private static final String TEST_REQUEST_URI = "/asylum/ccdAboutToSubmit";

    @Mock
    private HttpServletRequest request;

    private ErrorResponseBuilder errorResponseBuilder;

    @BeforeEach
    void setUp() {
        errorResponseBuilder = new ErrorResponseBuilder();
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, TEST_CORRELATION_ID);
        when(request.getRequestURI()).thenReturn(TEST_REQUEST_URI);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void should_build_error_response_with_custom_message() {
        String customMessage = "Custom error message";

        ErrorResponse response = errorResponseBuilder.build(ErrorCode.BAD_REQUEST, request, customMessage);

        assertThat(response.getErrorCode()).isEqualTo("BAD_REQUEST");
        assertThat(response.getMessage()).isEqualTo(customMessage);
        assertThat(response.getRequestId()).isEqualTo(TEST_CORRELATION_ID);
        assertThat(response.getPath()).isEqualTo(TEST_REQUEST_URI);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void should_build_error_response_with_default_message_when_custom_message_is_null() {
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.INTERNAL_ERROR, request, null);

        assertThat(response.getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getRequestId()).isEqualTo(TEST_CORRELATION_ID);
        assertThat(response.getPath()).isEqualTo(TEST_REQUEST_URI);
    }

    @Test
    void should_build_error_response_with_field_errors() {
        List<ErrorResponse.FieldError> fieldErrors = List.of(
            ErrorResponse.FieldError.builder().field("name").message("must not be blank").build(),
            ErrorResponse.FieldError.builder().field("email").message("must be valid").build()
        );

        ErrorResponse response = errorResponseBuilder.buildWithFieldErrors(
            ErrorCode.VALIDATION_ERROR, request, fieldErrors);

        assertThat(response.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getFieldErrors()).hasSize(2);
        assertThat(response.getFieldErrors().get(0).getField()).isEqualTo("name");
        assertThat(response.getFieldErrors().get(0).getMessage()).isEqualTo("must not be blank");
        assertThat(response.getFieldErrors().get(1).getField()).isEqualTo("email");
        assertThat(response.getFieldErrors().get(1).getMessage()).isEqualTo("must be valid");
    }

    @Test
    void should_log_error_with_ccd_case_id_from_request_attributes() {
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        attrs.setAttribute("CCDCaseId", "12345", RequestAttributes.SCOPE_REQUEST);
        RequestContextHolder.setRequestAttributes(attrs);

        Exception ex = new RuntimeException("Test error");

        errorResponseBuilder.logError(ex, ErrorCode.INTERNAL_ERROR, request);
    }

    @Test
    void should_log_error_with_unknown_ccd_case_id_when_not_in_request_attributes() {
        Exception ex = new RuntimeException("Test error");

        errorResponseBuilder.logError(ex, ErrorCode.INTERNAL_ERROR, request);
    }

    @Test
    void should_log_error_with_unknown_ccd_case_id_when_no_request_attributes() {
        RequestContextHolder.resetRequestAttributes();
        Exception ex = new RuntimeException("Test error");

        errorResponseBuilder.logError(ex, ErrorCode.INTERNAL_ERROR, request);
    }
}
