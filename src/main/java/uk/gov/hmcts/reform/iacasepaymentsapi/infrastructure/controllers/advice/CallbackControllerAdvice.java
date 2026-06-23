package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions.BadRequestException;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions.PaymentServiceRequestException;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CallbackControllerAdvice {

    private final ErrorResponseBuilder errorResponseBuilder;

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
        BadRequestException ex, HttpServletRequest request) {

        errorResponseBuilder.logError(ex, ErrorCode.BAD_REQUEST, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.BAD_REQUEST, request, ex.getMessage());
        return new ResponseEntity<>(response, ErrorCode.BAD_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
        ResponseStatusException ex, HttpServletRequest request) {

        errorResponseBuilder.logError(ex, ErrorCode.BAD_REQUEST, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.BAD_REQUEST, request, ex.getReason());
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
        AccessDeniedException ex, HttpServletRequest request) {

        errorResponseBuilder.logError(ex, ErrorCode.ACCESS_DENIED, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.ACCESS_DENIED, request,
            "Service name from S2S token ('ServiceAuthorization' header) is invalid");
        return new ResponseEntity<>(response, ErrorCode.ACCESS_DENIED.getHttpStatus());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
        AuthenticationException ex, HttpServletRequest request) {

        errorResponseBuilder.logError(ex, ErrorCode.UNAUTHORIZED, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.UNAUTHORIZED, request, null);
        return new ResponseEntity<>(response, ErrorCode.UNAUTHORIZED.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> ErrorResponse.FieldError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .build())
            .toList();

        errorResponseBuilder.logError(ex, ErrorCode.VALIDATION_ERROR, request);
        ErrorResponse response = errorResponseBuilder.buildWithFieldErrors(
            ErrorCode.VALIDATION_ERROR, request, fieldErrors);
        return new ResponseEntity<>(response, ErrorCode.VALIDATION_ERROR.getHttpStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
        ConstraintViolationException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
            .map(violation -> ErrorResponse.FieldError.builder()
                .field(violation.getPropertyPath().toString())
                .message(violation.getMessage())
                .build())
            .toList();

        errorResponseBuilder.logError(ex, ErrorCode.VALIDATION_ERROR, request);
        ErrorResponse response = errorResponseBuilder.buildWithFieldErrors(
            ErrorCode.VALIDATION_ERROR, request, fieldErrors);
        return new ResponseEntity<>(response, ErrorCode.VALIDATION_ERROR.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Parameter '%s' has invalid value", ex.getName());
        errorResponseBuilder.logError(ex, ErrorCode.BAD_REQUEST, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.BAD_REQUEST, request, message);
        return new ResponseEntity<>(response, ErrorCode.BAD_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex, HttpServletRequest request) {

        errorResponseBuilder.logError(ex, ErrorCode.INVALID_REQUEST_BODY, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.INVALID_REQUEST_BODY, request, null);
        return new ResponseEntity<>(response, ErrorCode.INVALID_REQUEST_BODY.getHttpStatus());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        errorResponseBuilder.logError(ex, ErrorCode.UNSUPPORTED_MEDIA_TYPE, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.UNSUPPORTED_MEDIA_TYPE, request, null);
        return new ResponseEntity<>(response, ErrorCode.UNSUPPORTED_MEDIA_TYPE.getHttpStatus());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
        NoResourceFoundException ex, HttpServletRequest request) {

        errorResponseBuilder.logError(ex, ErrorCode.NOT_FOUND, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.NOT_FOUND, request, null);
        return new ResponseEntity<>(response, ErrorCode.NOT_FOUND.getHttpStatus());
    }

    @ExceptionHandler(PaymentServiceRequestException.class)
    public ResponseEntity<ErrorResponse> handlePaymentServiceRequestException(
        PaymentServiceRequestException ex, HttpServletRequest request) {

        errorResponseBuilder.logError(ex, ErrorCode.PAYMENT_SERVICE_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.PAYMENT_SERVICE_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.PAYMENT_SERVICE_ERROR.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex, HttpServletRequest request) {

        errorResponseBuilder.logError(ex, ErrorCode.INTERNAL_ERROR, request);
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.INTERNAL_ERROR, request, null);
        return new ResponseEntity<>(response, ErrorCode.INTERNAL_ERROR.getHttpStatus());
    }
}
