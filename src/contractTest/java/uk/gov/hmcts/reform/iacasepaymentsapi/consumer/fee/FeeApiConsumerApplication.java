package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.fee;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.FeignConfiguration;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.JacksonConfiguration;

@SpringBootApplication
@EnableFeignClients(clients = {
    FeesRegisterApi.class
})
@Import({FeignConfiguration.class, JacksonConfiguration.class})
public class FeeApiConsumerApplication {
}
