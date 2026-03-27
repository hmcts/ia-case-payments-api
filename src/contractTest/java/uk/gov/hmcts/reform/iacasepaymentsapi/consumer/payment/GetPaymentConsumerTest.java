package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.payment;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.consumer.util.CardPaymentApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "payment_cardPayment", port = "8991")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentConsumerApplication.class})
@TestPropertySource(properties = {"payment.api.url=localhost:8991"})
@PactFolder("pacts")
public class GetPaymentConsumerTest {

    @Autowired
    CardPaymentApi cardPaymentApi;

    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String PAYMENT_REFERENCE = "654321ABC";

    @Pact(provider = "payment_cardPayment", consumer = "ia_casePaymentsApi")
    public V4Pact generateGetPaymentPactFragment(PactDslWithProvider builder) throws JSONException, IOException {
        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("paymentReference", PAYMENT_REFERENCE);

        PaymentDto response = getPaymentResponse();

        return builder
            .given("A payment reference exists", paymentMap)
            .uponReceiving("A request for card payment details by reference")
            .path("/card-payments/" + paymentMap.get("paymentReference"))
            .method("GET")
            .headers("Authorization", AUTHORIZATION_TOKEN)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(buildGetPaymentResponse(response))
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "generateGetPaymentPactFragment")
    public void getPayment() {
        cardPaymentApi.getPayment(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, PAYMENT_REFERENCE);
    }

    private DslPart buildGetPaymentResponse(PaymentDto paymentDto) {
        return newJsonBody((o) -> {
            o.numberType("amount", paymentDto.getAmount()) // numeric, not string
                .stringType("description", paymentDto.getDescription())
                .stringType("reference", paymentDto.getReference())
                .stringType("currency", paymentDto.getCurrency())
                .stringType("ccd_case_number", paymentDto.getCcdCaseNumber())
                .stringType("channel", paymentDto.getChannel())
                .stringType("status", paymentDto.getStatus())
                .stringType("service_name", paymentDto.getService()) // provider uses service_name
                .stringType("external_reference", paymentDto.getExternalReference());
        }).build();
    }

    private PaymentDto getPaymentResponse() {
        return PaymentDto.builder()
            .amount(new BigDecimal("100"))              // numeric example compatible with provider
            .description("description")
            .reference(PAYMENT_REFERENCE)
            .currency("GBP")
            .ccdCaseNumber("ccdCaseNumber1")
            .channel("online")
            .service("Divorce")                         // matches service_name sample
            .status("Initiated")
            .externalReference("paymentId")
            .build();
    }
}
