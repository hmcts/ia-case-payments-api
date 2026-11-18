package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentGroupDto {

    @JsonProperty("payment_group_reference")
    private String paymentGroupReference;

    @JsonProperty("service_request_status")
    private String serviceRequestStatus;

    @JsonProperty("fees")
    private List<FeeDto> fees;

    @JsonProperty("payments")
    private List<PaymentDto> payments;
}