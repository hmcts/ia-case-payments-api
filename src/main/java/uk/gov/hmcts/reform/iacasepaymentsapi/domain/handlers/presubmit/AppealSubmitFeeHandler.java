package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.CUSTOMER_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.ORAL_FEE_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAY_FOR_THE_APPEAL;

import java.util.Arrays;
import java.util.Collections;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Currency;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.PaymentService;

@Component
public class AppealSubmitFeeHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeeService feeService;
    private final PaymentService paymentService;

    private Fee fee;

    public AppealSubmitFeeHandler(
        FeeService feeService,
        PaymentService paymentService
    ) {
        this.feeService = feeService;
        this.paymentService = paymentService;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                || callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
               && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("AppealType is not present"));

        if (appealType.equals(AppealType.EA)
            || appealType.equals(AppealType.HU)
            || appealType.equals(AppealType.PA)) {
            if (!isFeeExists(FeeType.ORAL_FEE)) {
                PreSubmitCallbackResponse<AsylumCase> response =
                    new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
                response.addErrors(Collections.singleton("Cannot retrieve the fee from fees-register."));

                return response;
            }

            asylumCase.write(APPEAL_FEE_DESC,
                "The fee for this type of appeal with a hearing is £" + fee.getCalculatedAmount());
            asylumCase.write(ORAL_FEE_AMOUNT_FOR_DISPLAY, "£" + fee.getCalculatedAmount());


            String payForTheAppeal = asylumCase.read(PAY_FOR_THE_APPEAL, String.class)
                .orElseThrow(() -> new IllegalStateException("Pay for the appeal is not present"));

            if (payForTheAppeal.equals("payNow")) {
                String pbaAccountNumber = asylumCase.read(ACCOUNT_NUMBER, String.class)
                    .orElseThrow(() -> new IllegalStateException("PBA account number is not present"));
                String paymentDescription = asylumCase.read(PAYMENT_DESCRIPTION, String.class)
                    .orElseThrow(() -> new IllegalStateException("Payment description is not present"));
                String customerReference = asylumCase.read(CUSTOMER_REFERENCE, String.class)
                    .orElseThrow(() -> new IllegalStateException("Customer payment reference is not present"));

                CreditAccountPayment creditAccountPayment = new CreditAccountPayment(
                    pbaAccountNumber,
                    fee.getCalculatedAmount(),
                    null,
                    Long.toString(callback.getCaseDetails().getId()),
                    Currency.GBP,
                    customerReference,
                    paymentDescription,
                    Arrays.asList(fee)
                );
                PaymentResponse paymentResponse = makePayment(creditAccountPayment);

                asylumCase.write(PAYMENT_STATUS, paymentResponse.getStatus());
                asylumCase.write(PAYMENT_REFERENCE, paymentResponse.getReference());
            } else {
                asylumCase.write(PAYMENT_STATUS, "Payment due");
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isFeeExists(FeeType feeType) {

        fee = feeService.getFee(feeType);
        return fee != null;
    }

    private PaymentResponse makePayment(CreditAccountPayment creditAccountPayment) {

        return paymentService.creditAccountPayment(creditAccountPayment);
    }
}
