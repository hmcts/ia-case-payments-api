package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTest {

    private static final List<String> IA_S2S_AUTH_SERVICES = List.of("iac,payment_app");

    @Mock
    private AuthTokenValidator authTokenValidator;

    private S2STokenValidator s2STokenValidator;

    @BeforeEach
    public void setup() {
        s2STokenValidator = new S2STokenValidator(IA_S2S_AUTH_SERVICES, authTokenValidator);
    }

    @Test
    public void givenServiceNameIsValid() {
        when(authTokenValidator.getServiceName("Bearer payment_app")).thenReturn("payment_app");
        assertEquals(Boolean.FALSE, s2STokenValidator.checkIfServiceIsAllowed("payment_app"));
    }

    @Test
    public void givenServiceNameIsNullFromToken() {
        when(authTokenValidator.getServiceName("Bearer TestService")).thenReturn(null);
        assertEquals(Boolean.FALSE, s2STokenValidator.checkIfServiceIsAllowed("TestService"));
    }

    @Test
    public void givenServiceNameIsEmptyFromToken() throws InvalidTokenException {
        assertThrows(InvalidTokenException.class, () -> s2STokenValidator.checkIfServiceIsAllowed(""));
    }
}
