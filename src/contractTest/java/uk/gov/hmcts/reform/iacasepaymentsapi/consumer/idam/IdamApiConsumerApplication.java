package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.idam;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    IdamApi.class
})
public class IdamApiConsumerApplication {
    @MockitoBean
    RestTemplate restTemplate;
}
