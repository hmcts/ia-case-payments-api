package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HEARING_DECISION_SELECTED;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ACCOUNT_LIST;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DATE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ERROR_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_FAILED_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PBA_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.PAID;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.StatusHistories;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.PaymentService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.RefDataService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.PaymentProperties;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PaymentAppealHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeeService feeService;
    @Mock private Fee fee;
    @Mock private PaymentService paymentService;
    @Mock private RefDataService refDataService;
    @Mock private PaymentProperties paymentProperties;

    private ObjectMapper objectMapper;
    private PaymentAppealHandler appealFeePaymentHandler;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        appealFeePaymentHandler =
            new PaymentAppealHandler(feeService, paymentService, refDataService, paymentProperties);

    }

    @Test
    public void should_return_error_when_fee_does_not_exists() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(null);

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot retrieve the fee from fees-register.");
    }

    @Test
    void should_call_payment_api_on_pay_now() throws Exception {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(Long.valueOf("112233445566"));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCode()).thenReturn("FEE0123");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getDescription())
            .thenReturn("Appeal determined with a hearing");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getVersion()).thenReturn("1");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCalculatedAmount()).thenReturn(BigDecimal.valueOf(140.00));
        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("PBA1234567")));
        when(asylumCase.read(APPEAL_FEE_HEARING_DESC, String.class)).thenReturn(Optional.of("Hearing appeal"));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("Hearing appeal"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getFeeForDisplay()).thenReturn("£140");

        when(paymentService.creditAccountPayment(any(CreditAccountPayment.class)))
            .thenReturn(new PaymentResponse("RC-1590-6748-2373-9129", new Date(),
                                            "Success",
                                            "2020-1590674823325", null
            ));

        when(refDataService.getOrganisationResponse()).thenReturn(
            new OrganisationResponse(new OrganisationEntityResponse("ia-legal-rep-org")));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealFeePaymentHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        AsylumCase asylumCase = callbackResponse.getData();

        verify(asylumCase, times(1))
            .write(PAYMENT_REFERENCE, "RC-1590-6748-2373-9129");

        String pattern = "d MMM yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        verify(asylumCase, times(1))
            .write(PAYMENT_DATE, simpleDateFormat.format(new Date()));
        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, "Paid");
        verify(asylumCase, times(1))
            .write(HEARING_DECISION_SELECTED, "Decision with a hearing");
        verify(asylumCase, times(1))
            .write(FEE_AMOUNT, "140.0");
        verify(asylumCase, times(1))
            .write(FEE_AMOUNT_FOR_DISPLAY, "£140");

        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, PAID);

        verify(asylumCase, times(1))
            .clear(PAYMENT_FAILED_FOR_DISPLAY);
    }

    @Test
    void should_call_payment_api_on_pay_now_and_return_failure() throws Exception {

        StatusHistories statusHistory = new StatusHistories(
            "failed",
            "CA-E0004",
            "Your account is deleted",
            "2020-05-28T14:04:06.048+0000",
            "2020-05-28T14:04:06.048+0000"
        );

        List<StatusHistories> statusHistories = new ArrayList<>();
        statusHistories.add(statusHistory);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(Long.valueOf("112233445566"));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCode()).thenReturn("FEE0123");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getDescription())
            .thenReturn("Appeal determined with a hearing");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getVersion()).thenReturn("1");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCalculatedAmount()).thenReturn(BigDecimal.valueOf(140.00));
        when(asylumCase.read(PBA_NUMBER, String.class)).thenReturn(Optional.of("PBA12345678"));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("Some description"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("A1234567/003"));

        when(asylumCase.read(FEE_CODE, String.class)).thenReturn(Optional.of("FEE0123"));
        when(asylumCase.read(FEE_DESCRIPTION, String.class))
            .thenReturn(Optional.of("Appeal determined with a hearing"));
        when(asylumCase.read(FEE_VERSION, String.class)).thenReturn(Optional.of("1"));
        when(asylumCase.read(FEE_AMOUNT, BigDecimal.class)).thenReturn(Optional.of(BigDecimal.valueOf(140.00)));

        when(paymentService.creditAccountPayment(any(CreditAccountPayment.class)))
            .thenReturn(new PaymentResponse("RC-1590-6748-2373-9129", new Date(),
                                            "Failed",
                                            "2020-1590674823325",
                                            statusHistories
            ));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealFeePaymentHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        AsylumCase asylumCase = callbackResponse.getData();

        verify(asylumCase, times(1))
            .write(PAYMENT_REFERENCE, "RC-1590-6748-2373-9129");

        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, FAILED);

        verify(asylumCase, times(1))
            .write(PAYMENT_FAILED_FOR_DISPLAY, "Pending");

        verify(asylumCase, times(1))
            .write(PAYMENT_ERROR_CODE, statusHistories.get(0).getErrorCode());

        verify(asylumCase, times(1))
            .write(PAYMENT_ERROR_MESSAGE, statusHistories.get(0).getErrorMessage());
    }

    @Test
    void should_throw_when_no_account_number_is_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCode()).thenReturn("FEE0123");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getDescription())
            .thenReturn("Appeal determined with a hearing");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getVersion()).thenReturn("1");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCalculatedAmount()).thenReturn(BigDecimal.valueOf(140.00));
        when(asylumCase.read(APPEAL_FEE_HEARING_DESC, String.class)).thenReturn(Optional.of("Hearing appeal"));
        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("PBA account number is not present");
    }

    @Test
    void should_throw_when_no_payment_description_is_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCode()).thenReturn("FEE0123");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getDescription())
            .thenReturn("Appeal determined with a hearing");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getVersion()).thenReturn("1");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCalculatedAmount()).thenReturn(BigDecimal.valueOf(140.00));

        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("PBA1234567")));

        when(asylumCase.read(APPEAL_FEE_HEARING_DESC, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Payment description is not present");
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealFeePaymentHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealFeePaymentHandler
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealFeePaymentHandler.canHandle(callbackStage, callback);

                if ((event == Event.PAYMENT_APPEAL || callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL)
                    && (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }
}
