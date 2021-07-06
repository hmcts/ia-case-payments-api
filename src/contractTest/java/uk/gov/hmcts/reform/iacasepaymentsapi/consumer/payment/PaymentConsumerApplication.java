package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.payment;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.PaymentApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    PaymentApi.class
})
@ActiveProfiles("contract")
public class PaymentConsumerApplication {

}
