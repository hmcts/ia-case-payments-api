package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class FeeDto {

    @JsonProperty("calculated_amount")
    private BigDecimal calculatedAmount;
    private String description;
    private Integer version;
    private String code;

    private FeeDto() {

    }

    public FeeDto(String code, String description, Integer version, BigDecimal calculatedAmount) {

        this.calculatedAmount = calculatedAmount;
        this.description = description;
        this.version = version;
        this.code = code;
    }

    public BigDecimal getCalculatedAmount() {
        requireNonNull(calculatedAmount);
        return calculatedAmount;
    }

    public String getDescription() {
        requireNonNull(description);
        return description;
    }

    public Integer getVersion() {
        requireNonNull(version);
        return version;
    }

    public String getCode() {
        requireNonNull(code);
        return code;
    }

}
