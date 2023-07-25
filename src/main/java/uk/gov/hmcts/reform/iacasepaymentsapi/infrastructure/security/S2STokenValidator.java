package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class S2STokenValidator {

    public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String BEARER = "Bearer ";

    @Value("${idam.s2s-authorised.services}")
    private final List<String> iaS2sAuthorisedServices;

    @Autowired
    private final AuthTokenValidator authTokenValidator;

    public boolean checkIfServiceIsAllowed(String token) throws InvalidTokenException {
        String serviceName = authenticate(token);
        if (Objects.nonNull(serviceName)) {
            return iaS2sAuthorisedServices.contains(serviceName);
        } else {
            log.warn("Service name from S2S token ('ServiceAuthorization' header) is null");
            return false;
        }
    }

    private String authenticate(String authHeader) throws InvalidTokenException {
        if (isBlank(authHeader)) {
            throw new InvalidTokenException("Provided S2S token is missing or invalid");
        }

        String bearerAuthToken = getBearerToken(authHeader);

        log.info("S2S token found in the request");
        return authTokenValidator.getServiceName(bearerAuthToken);
    }

    private String getBearerToken(String token) {
        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }

}
