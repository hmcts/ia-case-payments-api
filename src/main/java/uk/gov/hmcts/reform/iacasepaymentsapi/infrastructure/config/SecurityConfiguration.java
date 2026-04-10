package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.AuthorizedRolesProvider;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.CcdEventAuthorizor;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SpringAuthorizedRolesProvider;

@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration  {

    @Getter
    private final List<String> anonymousPaths = new ArrayList<>();
    @Getter
    private final Map<String, List<Event>> roleEventAccess = new HashMap<>();
    private final Converter<Jwt, Collection<GrantedAuthority>> idamAuthoritiesConverter;
    private final ServiceAuthFilter serviceAuthFilter;

    public SecurityConfiguration(
        Converter<Jwt, Collection<GrantedAuthority>> idamAuthoritiesConverter,
        ServiceAuthFilter serviceAuthFilter
    ) {
        this.idamAuthoritiesConverter = idamAuthoritiesConverter;
        this.serviceAuthFilter = serviceAuthFilter;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(anonymousPaths
                .stream()
                .toArray(String[]::new)
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(idamAuthoritiesConverter);


        http
            .addFilterBefore(serviceAuthFilter, AbstractPreAuthenticatedProcessingFilter.class)
            .sessionManagement(management -> management.sessionCreationPolicy(STATELESS))
            .exceptionHandling(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
            .oauth2ResourceServer(server -> server
                .jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                .and())
            .oauth2Client(withDefaults());

        return http.build();
    }


    @Bean
    public AuthorizedRolesProvider authorizedRolesProvider() {
        return new SpringAuthorizedRolesProvider();
    }

    @Bean
    public CcdEventAuthorizor getCcdEventAuthorizor(AuthorizedRolesProvider authorizedRolesProvider) {

        return new CcdEventAuthorizor(
            ImmutableMap.copyOf(roleEventAccess),
            authorizedRolesProvider
        );
    }
}
