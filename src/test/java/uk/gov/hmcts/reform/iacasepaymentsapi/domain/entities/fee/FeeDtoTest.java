package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FeeDtoTest {

    private BigDecimal calculatedAmount = new BigDecimal("140.00");
    private String description = "Appeal determined with a hearing";
    private Integer version = 1;
    private String code = "FEE0123";

    private FeeDto feeDto;

    @BeforeEach
    public void setUp() {

        feeDto = new FeeDto(code, description, version, calculatedAmount);
    }

    @Test
    public void should_hold_onto_values() {

        assertEquals(feeDto.getCode(), code);
        assertEquals(feeDto.getDescription(), description);
        assertEquals(feeDto.getVersion(), version);
        assertEquals(feeDto.getCalculatedAmount(), calculatedAmount);
    }

    @Test
    public void should_throw_required_field_exception() {

        feeDto = new FeeDto(null, null, null, null);

        assertThatThrownBy(feeDto::getCode)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(feeDto::getDescription)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(feeDto::getVersion)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(feeDto::getCalculatedAmount)
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
