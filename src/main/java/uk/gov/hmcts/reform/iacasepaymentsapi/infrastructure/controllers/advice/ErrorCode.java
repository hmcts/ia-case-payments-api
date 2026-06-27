package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers.advice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation failed"),
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "Invalid request"),
    INVALID_REQUEST_BODY("INVALID_REQUEST_BODY", HttpStatus.BAD_REQUEST, "Request body is malformed or unreadable"),

    // 401 Unauthorized
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"),

    // 403 Forbidden
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN, "Access denied"),

    // 404 Not Found
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "Resource not found"),

    // 415 Unsupported Media Type
    UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),

    // 500 Internal Server Error
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    PAYMENT_SERVICE_ERROR("PAYMENT_SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
        "Payment service request failed");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
