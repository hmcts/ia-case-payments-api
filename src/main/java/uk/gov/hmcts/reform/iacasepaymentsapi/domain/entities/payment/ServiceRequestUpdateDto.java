package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class ServiceRequestUpdateDto {
    @JsonProperty("service_request_reference")
    private String serviceRequestReference;
    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;
    @JsonProperty("service_request_amount")
    private String serviceRequestAmount;
    @JsonProperty("service_request_status")
    private String serviceRequestStatus;
    @JsonProperty("payment")
    private PaymentDto payment;

}
