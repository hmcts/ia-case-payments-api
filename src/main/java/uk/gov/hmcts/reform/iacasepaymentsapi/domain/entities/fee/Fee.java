package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Fee {

    private BigDecimal calculatedAmount;
    private String description;
    private String version;
    private String code;

    private Fee() {

    }

    public Fee(String code, String description, String version, BigDecimal calculatedAmount) {

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

    public String getVersion() {
        requireNonNull(version);
        return version;
    }

    public String getCode() {
        requireNonNull(code);
        return code;
    }

    @Override
    public String toString() {
        return "Fee{ calculatedAmount=" + calculatedAmount
            + ", description='" + description + '\''
            + ", version=" + version
            + ", code='" + code + '\'' + '}';
    }
}
