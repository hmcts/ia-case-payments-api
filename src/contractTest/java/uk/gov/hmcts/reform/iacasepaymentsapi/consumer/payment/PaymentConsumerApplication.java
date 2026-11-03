package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.payment;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.iacasepaymentsapi.consumer.util.CardPaymentApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.PaymentApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.FeignConfiguration;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.JacksonConfiguration;

@SpringBootApplication
@EnableFeignClients(clients = {
    PaymentApi.class,
    CardPaymentApi.class
})
@Import({FeignConfiguration.class, JacksonConfiguration.class})
public class PaymentConsumerApplication {

}
