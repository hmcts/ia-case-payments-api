package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary
    public JsonMapper jackson2ObjectMapperBuilder() {
        return JsonMapperFactory.CUSTOM_MAPPER;
    }
}
