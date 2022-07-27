package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions;

public class AuthorisationException extends RuntimeException {

    public AuthorisationException(String ex) {
        super(ex);
    }
}
