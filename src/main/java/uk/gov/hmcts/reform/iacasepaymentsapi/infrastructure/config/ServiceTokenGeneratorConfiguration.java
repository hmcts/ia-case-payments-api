package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
@Slf4j
public class ServiceTokenGeneratorConfiguration {

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Bean
    public AuthTokenGenerator authTokenGenerator(
        @Value("${idam.s2s-auth.totp_secret}") String secret,
        @Value("${idam.s2s-auth.microservice}") String microService,
        ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        log.info("Secret :" + secret);
        log.info("microService :" + microService);
        return AuthTokenGeneratorFactory.createDefaultGenerator(
            secret,
            microService,
            serviceAuthorisationApi
        );
    }
}
