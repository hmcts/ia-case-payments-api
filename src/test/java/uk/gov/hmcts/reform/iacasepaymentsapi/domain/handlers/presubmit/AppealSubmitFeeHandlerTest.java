package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_WITHOUT_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_HEARING_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_PAYMENT_APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AppealSubmitFeeHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeeService feeService;

    private AppealSubmitFeeHandler appealSubmitFeeHandler;

    @BeforeEach
    public void setUp() {
        appealSubmitFeeHandler = new AppealSubmitFeeHandler(feeService);
    }

    @Test
    public void should_retrieve_the_fee_with_hearing_for_appeal_type_EA() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING))
            .thenReturn(new Fee("FEE0238",
                "Appeal determined with a hearing",
                2,
                new BigDecimal("140.00")));

        when(feeService.getFee(FeeType.FEE_WITHOUT_HEARING))
            .thenReturn(new Fee("FEE0238",
                                "Appeal determined without a hearing",
                                2,
                                new BigDecimal("80.00")));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealSubmitFeeHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        verify(asylumCase, times(1))
            .write(FEE_HEARING_AMOUNT_FOR_DISPLAY, "£140.00");
        verify(asylumCase, times(1))
            .write(APPEAL_FEE_HEARING_DESC, "The fee for this type of appeal with a hearing is £140.00");
        verify(asylumCase, times(1))
            .write(FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY, "£80.00");
        verify(asylumCase, times(1))
            .write(APPEAL_FEE_WITHOUT_HEARING_DESC, "The fee for this type of appeal with a hearing is £80.00");
        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, "Payment due");
        verify(asylumCase, times(1))
            .write(FEE_PAYMENT_APPEAL_TYPE, "Yes");
    }

    @Test
    public void should_throw_when_no_appeal_type_is_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealSubmitFeeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("AppealType is not present");
    }

    @Test
    public void should_return_error_when_fee_does_not_exists() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealSubmitFeeHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertTrue(callbackResponse.getErrors().contains("Cannot retrieve the fee from fees-register."));

    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealSubmitFeeHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmitFeeHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmitFeeHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmitFeeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealSubmitFeeHandler.canHandle(callbackStage, callback);

                if ((event == Event.SUBMIT_APPEAL)
                    && (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        || callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

}
