package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.fee;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.FeesRegisterApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    FeesRegisterApi.class
})
@ActiveProfiles("contract")
public class FeeApiConsumerApplication {
}
