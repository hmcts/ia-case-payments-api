package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.util;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;

@FeignClient(name = "payment-api", url = "${payment.api.url}")
public interface CardPaymentApi {

    @GetMapping(value = "/card-payments/{ref}")
    PaymentDto getPayment(@RequestHeader(AUTHORIZATION) String authorization,
                          @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
                          @PathVariable("ref") String paymentReference
    );
}

