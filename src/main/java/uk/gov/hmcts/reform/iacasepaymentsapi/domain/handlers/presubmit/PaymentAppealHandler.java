package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_WITHOUT_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_PAYMENT_APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HEARING_DECISION_SELECTED;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ACCOUNT_LIST;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DATE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Currency;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Service;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.PaymentService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.RefDataService;

@Component
@Slf4j
public class PaymentAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeeService feeService;
    private final PaymentService paymentService;
    private final RefDataService refDataService;

    public PaymentAppealHandler(
        FeeService feeService,
        PaymentService paymentService,
        RefDataService refDataService
    ) {
        this.feeService = feeService;
        this.paymentService = paymentService;
        this.refDataService = refDataService;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                && (callback.getEvent() == Event.PAYMENT_APPEAL
                    || callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("AppealType is not present"));
        asylumCase.write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.NO);
        Fee feeSelected = null;

        if (appealType.equals(AppealType.EA)
            || appealType.equals(AppealType.HU)
            || appealType.equals(AppealType.PA)) {

            String hearingFeeOption = asylumCase
                .read(DECISION_HEARING_FEE_OPTION, String.class).orElse("");

            if (hearingFeeOption.equals(DECISION_WITH_HEARING.value())) {

                feeSelected = feeService.getFee(FeeType.FEE_WITH_HEARING);
                asylumCase.write(PAYMENT_DESCRIPTION,
                                 asylumCase.read(APPEAL_FEE_HEARING_DESC, String.class).orElse(""));
                asylumCase.write(HEARING_DECISION_SELECTED, "Decision with a hearing");

            } else if (hearingFeeOption.equals(DECISION_WITHOUT_HEARING.value())) {

                feeSelected = feeService.getFee(FeeType.FEE_WITHOUT_HEARING);
                asylumCase.write(PAYMENT_DESCRIPTION,
                                 asylumCase.read(APPEAL_FEE_WITHOUT_HEARING_DESC, String.class).orElse(""));
                asylumCase.write(HEARING_DECISION_SELECTED, "Decision without a hearing");
            }
        }

        if (feeSelected != null) {

            writeFeeDetailsToCaseData(asylumCase, feeSelected);

            DynamicList pbaAccountNumber = asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class)
                .orElseThrow(() -> new IllegalStateException("PBA account number is not present"));
            String paymentDescription = asylumCase.read(PAYMENT_DESCRIPTION, String.class)
                .orElseThrow(() -> new IllegalStateException("Payment description is not present"));
            String customerReference = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("Customer payment reference is not present"));

            String orgName =  refDataService.getOrganisationResponse().getOrganisationEntityResponse().getName();
            String caseId = String.valueOf(callback.getCaseDetails().getId());
            CreditAccountPayment creditAccountPayment = new CreditAccountPayment(
                pbaAccountNumber.getValue().getCode(),
                feeSelected.getCalculatedAmount(),
                caseId,
                caseId,
                Currency.GBP,
                caseId,
                paymentDescription,
                orgName,
                Service.IAC,
                "BFA1",
                Arrays.asList(feeSelected)
            );
            PaymentResponse paymentResponse = makePayment(creditAccountPayment);
            asylumCase.write(PAYMENT_STATUS, (paymentResponse.getStatus().equals("Success") ?  "Paid" : "Payment due"));
            asylumCase.write(PAYMENT_REFERENCE, paymentResponse.getReference());

            String pattern = "d MMM yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            asylumCase.write(PAYMENT_DATE, simpleDateFormat.format(paymentResponse.getDateCreated()));
        } else {
            throw new IllegalStateException("Cannot retrieve the fee from fees-register.");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void writeFeeDetailsToCaseData(AsylumCase asylumCase, Fee fee) {

        asylumCase.write(FEE_CODE, fee.getCode());
        asylumCase.write(FEE_DESCRIPTION, fee.getDescription());
        asylumCase.write(FEE_VERSION, fee.getVersion());
        asylumCase.write(FEE_AMOUNT, fee.getCalculatedAmount().toString());
        asylumCase.write(FEE_AMOUNT_FOR_DISPLAY, fee.getFeeForDisplay());
        asylumCase.write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.YES);
    }

    private PaymentResponse makePayment(CreditAccountPayment creditAccountPayment) {

        return paymentService.creditAccountPayment(creditAccountPayment);
    }
}
