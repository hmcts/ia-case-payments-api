package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AsylumCaseDefinitionTest {

    @Test
    public void has_correct_values() {

        assertEquals("appealReferenceNumber",
                     AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER.value());
        assertEquals("feeHearingAmountForDisplay",
                     AsylumCaseDefinition.FEE_HEARING_AMOUNT_FOR_DISPLAY.value());
        assertEquals("appealFeeHearingDesc", AsylumCaseDefinition.APPEAL_FEE_HEARING_DESC.value());
        assertEquals("appealFeeWithoutHearingDesc",
                     AsylumCaseDefinition.APPEAL_FEE_WITHOUT_HEARING_DESC.value());
        assertEquals("feeWithoutHearingAmountForDisplay",
                     AsylumCaseDefinition.FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY.value());
        assertEquals("paymentStatus",
                     AsylumCaseDefinition.PAYMENT_STATUS.value());
        assertEquals("feePaymentAppealType",
                     AsylumCaseDefinition.FEE_PAYMENT_APPEAL_TYPE.value());
        assertEquals("appealType",
                     AsylumCaseDefinition.APPEAL_TYPE.value());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(8, AsylumCaseDefinition.values().length);
    }
}
