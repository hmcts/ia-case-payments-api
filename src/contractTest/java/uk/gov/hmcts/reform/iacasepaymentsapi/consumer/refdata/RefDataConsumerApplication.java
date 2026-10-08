package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.refdata;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.RefDataApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.FeignConfiguration;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.JacksonConfiguration;

@SpringBootApplication
@EnableFeignClients(clients = {
    RefDataApi.class
})
@Import({FeignConfiguration.class, JacksonConfiguration.class})
public class RefDataConsumerApplication {

}
