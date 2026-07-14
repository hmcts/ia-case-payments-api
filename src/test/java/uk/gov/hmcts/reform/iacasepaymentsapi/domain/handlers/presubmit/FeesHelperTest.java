package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
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
}
