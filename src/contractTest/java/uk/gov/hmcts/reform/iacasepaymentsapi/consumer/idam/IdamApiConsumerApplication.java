package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.idam;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.FeignConfiguration;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.JacksonConfiguration;

@SpringBootApplication
@EnableFeignClients(clients = {
    IdamApi.class
})
@Import({FeignConfiguration.class, JacksonConfiguration.class})
public class IdamApiConsumerApplication {
    @MockitoBean
    RestTemplate restTemplate;
}
