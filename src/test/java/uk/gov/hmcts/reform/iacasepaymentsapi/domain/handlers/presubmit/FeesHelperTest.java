package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITH_HEARING;

@ExtendWith(MockitoExtension.class)
class FeesHelperTest {

    @Mock
    private AsylumCase asylumCase;

    @Test
    void should_return_true_when_decision_with_hearing_and_fee_with_hearing_exists() {
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithHearing"));
        when(asylumCase.read(FEE_WITH_HEARING, String.class))
            .thenReturn(Optional.of("140"));

        assertTrue(FeesHelper.feeExistsForDecisionType(asylumCase));
    }

    @Test
    void should_return_false_when_decision_with_hearing_and_fee_with_hearing_does_not_exist() {
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithHearing"));
        when(asylumCase.read(FEE_WITH_HEARING, String.class))
            .thenReturn(Optional.empty());

        assertFalse(FeesHelper.feeExistsForDecisionType(asylumCase));
    }

    @Test
    void should_return_true_when_decision_without_hearing_and_fee_without_hearing_exists() {
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithoutHearing"));
        when(asylumCase.read(FEE_WITHOUT_HEARING, String.class))
            .thenReturn(Optional.of("80"));

        assertTrue(FeesHelper.feeExistsForDecisionType(asylumCase));
    }

    @Test
    void should_return_false_when_decision_without_hearing_and_fee_without_hearing_does_not_exist() {
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithoutHearing"));
        when(asylumCase.read(FEE_WITHOUT_HEARING, String.class))
            .thenReturn(Optional.empty());

        assertFalse(FeesHelper.feeExistsForDecisionType(asylumCase));
    }

    @Test
    void should_return_false_when_decision_hearing_fee_option_is_empty() {
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.empty());

        assertFalse(FeesHelper.feeExistsForDecisionType(asylumCase));
    }

    @Test
    void readExistingFee_should_return_fee_when_decision_with_hearing_and_all_fields_exist() {
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithHearing"));
        when(asylumCase.read(FEE_WITH_HEARING, String.class)).thenReturn(Optional.of("140"));
        when(asylumCase.read(FEE_CODE, String.class)).thenReturn(Optional.of("FEE0001"));
        when(asylumCase.read(FEE_DESCRIPTION, String.class)).thenReturn(Optional.of("Fee with hearing"));
        when(asylumCase.read(FEE_VERSION, String.class)).thenReturn(Optional.of("1"));

        Fee fee = FeesHelper.readExistingFee(asylumCase);

        assertNotNull(fee);
        assertEquals("FEE0001", fee.getCode());
        assertEquals("Fee with hearing", fee.getDescription());
        assertEquals("1", fee.getVersion());
        assertEquals(new BigDecimal("140"), fee.getCalculatedAmount());
    }

    @Test
    void readExistingFee_should_return_fee_when_decision_without_hearing_and_all_fields_exist() {
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithoutHearing"));
        when(asylumCase.read(FEE_WITHOUT_HEARING, String.class)).thenReturn(Optional.of("80"));
        when(asylumCase.read(FEE_CODE, String.class)).thenReturn(Optional.of("FEE0002"));
        when(asylumCase.read(FEE_DESCRIPTION, String.class)).thenReturn(Optional.of("Fee without hearing"));
        when(asylumCase.read(FEE_VERSION, String.class)).thenReturn(Optional.of("2"));

        Fee fee = FeesHelper.readExistingFee(asylumCase);

        assertNotNull(fee);
        assertEquals("FEE0002", fee.getCode());
        assertEquals("Fee without hearing", fee.getDescription());
        assertEquals("2", fee.getVersion());
        assertEquals(new BigDecimal("80"), fee.getCalculatedAmount());
    }

    @Test
    void readExistingFee_should_return_null_when_decision_hearing_fee_option_is_empty() {
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.empty());

        Fee fee = FeesHelper.readExistingFee(asylumCase);

        assertNull(fee);
    }

    @Test
    void readExistingFee_should_return_null_when_fee_amount_does_not_exist() {
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithHearing"));
        when(asylumCase.read(FEE_WITH_HEARING, String.class)).thenReturn(Optional.empty());

        Fee fee = FeesHelper.readExistingFee(asylumCase);

        assertNull(fee);
    }
}
