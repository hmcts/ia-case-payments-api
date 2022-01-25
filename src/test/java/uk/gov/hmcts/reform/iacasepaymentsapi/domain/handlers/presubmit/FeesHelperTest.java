package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_PAYMENT_APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DESCRIPTION;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeesHelperTest {

    @Mock private AsylumCase asylumCase;
    @Mock private FeeService feeService;

    private final Fee fee = new Fee(
        "feeCode",
        "feeDescription",
        "feeVersion",
        new BigDecimal("140.00")
    );

    private final String feeAmountInPence =
        String.valueOf(new BigDecimal(fee.getAmountAsString()).multiply(new BigDecimal("100")));

    @Test
    void should_throw_exception_when_fee_is_null() {

        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(null);

        verify(asylumCase, times(0)).write(FEE_CODE, fee.getCode());
        verify(asylumCase, times(0)).write(FEE_DESCRIPTION, fee.getDescription());
        verify(asylumCase, times(0)).write(FEE_VERSION, fee.getVersion());
        verify(asylumCase, times(0)).write(FEE_AMOUNT_GBP, fee.getAmountAsString());
        verify(asylumCase, times(0)).write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.YES);

        assertThatThrownBy(() -> FeesHelper.findFeeByHearingType(feeService, asylumCase))
            .isExactlyInstanceOf(RestClientException.class);
    }

    @Test
    void should_handle_fee_with_a_hearing() {

        when(asylumCase.read(
            DECISION_HEARING_FEE_OPTION,
            String.class
        )).thenReturn(Optional.of("decisionWithHearing"));

        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);

        FeesHelper.findFeeByHearingType(feeService, asylumCase);

        verify(asylumCase, times(1)).write(FEE_CODE, fee.getCode());
        verify(asylumCase, times(1)).write(FEE_DESCRIPTION, fee.getDescription());
        verify(asylumCase, times(1)).write(FEE_VERSION, fee.getVersion());
        verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, feeAmountInPence);
        verify(asylumCase, times(1)).write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.YES);
        verify(asylumCase, times(1)).write(FEE_WITH_HEARING, fee.getAmountAsString());
        verify(asylumCase, times(0)).write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
        verify(asylumCase, times(1)).write(PAYMENT_DESCRIPTION, "Appeal determined with a hearing");
    }

    @Test
    void should_handle_fee_without_a_hearing() {

        when(asylumCase.read(
            DECISION_HEARING_FEE_OPTION,
            String.class
        )).thenReturn(Optional.of("decisionWithoutHearing"));

        when(feeService.getFee(FeeType.FEE_WITHOUT_HEARING)).thenReturn(fee);

        FeesHelper.findFeeByHearingType(feeService, asylumCase);

        verify(asylumCase, times(1)).write(FEE_CODE, fee.getCode());
        verify(asylumCase, times(1)).write(FEE_DESCRIPTION, fee.getDescription());
        verify(asylumCase, times(1)).write(FEE_VERSION, fee.getVersion());
        verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, feeAmountInPence);
        verify(asylumCase, times(1)).write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.YES);
        verify(asylumCase, times(1)).write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
        verify(asylumCase, times(0)).write(FEE_WITH_HEARING, fee.getAmountAsString());
        verify(asylumCase, times(1)).write(PAYMENT_DESCRIPTION, "Appeal determined without a hearing");
    }
}
