package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.refdata;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.RefDataApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    RefDataApi.class
})
@ActiveProfiles("contract")
public class RefDataConsumerApplication {

}
