package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;

import java.math.BigDecimal;

@SuperBuilder
public class PaymentDtoForTest extends PaymentDto {

    public static PaymentDtoBuilder generateValid() {
        return builder()
            .id("1234")
            .amount(BigDecimal.valueOf(140))
            .description("Payment status update")
            .reference("RC-1627-5070-9329-7815")
            .currency("GBP")
            .ccdCaseNumber("1627506765384547")
            .channel("online")
            .method("card")
            .externalProvider("gov pay")
            .externalReference("8saf7t8kav53mmubrff738nord")
            .status("Success");
    }

}
