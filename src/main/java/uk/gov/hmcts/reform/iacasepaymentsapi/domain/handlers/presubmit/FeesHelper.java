package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.isNull;
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
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;

public class FeesHelper {

    private static final String DECISION_WITH_HEARING = "decisionWithHearing";
    private static final String DECISION_WITHOUT_HEARING = "decisionWithoutHearing";

    private FeesHelper() {
    }

    public static final Fee findFeeByHearingType(FeeService feeService, AsylumCase asylumCase) {
        Optional<String> decisionHearingFeeOption = getDecisionHearingFeeOption(asylumCase);
        if (decisionHearingFeeOption.isPresent()) {

            String hearingOption = decisionHearingFeeOption.get();
            FeeType feeType = isDecisionWithHearing(hearingOption)
                ? FeeType.FEE_WITH_HEARING
                : FeeType.FEE_WITHOUT_HEARING;

            Fee fee = feeService.getFee(feeType);

            if (!isNull(fee)) {
                writeFeeDetails(asylumCase, fee);
                writeHearingSpecificDetails(asylumCase, fee, hearingOption);
                return fee;
            }
        }
        return null;
    }

    public static boolean feeExistsForDecisionType(AsylumCase asylumCase) {
        Optional<String> decisionHearingFeeOption = getDecisionHearingFeeOption(asylumCase);

        if (decisionHearingFeeOption.isEmpty()) {
            return false;
        }

        if (isDecisionWithHearing(decisionHearingFeeOption.get())) {
            return asylumCase.read(FEE_WITH_HEARING, String.class).isPresent();
        } else {
            return asylumCase.read(FEE_WITHOUT_HEARING, String.class).isPresent();
        }
    }

    public static Fee readExistingFee(AsylumCase asylumCase) {
        Optional<String> decisionHearingFeeOption = getDecisionHearingFeeOption(asylumCase);

        if (decisionHearingFeeOption.isEmpty()) {
            return null;
        }

        Optional<String> feeAmount = isDecisionWithHearing(decisionHearingFeeOption.get())
            ? asylumCase.read(FEE_WITH_HEARING, String.class)
            : asylumCase.read(FEE_WITHOUT_HEARING, String.class);

        if (feeAmount.isEmpty()) {
            return null;
        }

        String code = asylumCase.read(FEE_CODE, String.class).orElse("");
        String description = asylumCase.read(FEE_DESCRIPTION, String.class).orElse("");
        String version = asylumCase.read(FEE_VERSION, String.class).orElse("");

        return new Fee(code, description, version, new BigDecimal(feeAmount.get()));
    }

    private static Optional<String> getDecisionHearingFeeOption(AsylumCase asylumCase) {
        return asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class);
    }

    private static boolean isDecisionWithHearing(String hearingOption) {
        return DECISION_WITH_HEARING.equals(hearingOption);
    }

    private static void writeFeeDetails(AsylumCase asylumCase, Fee fee) {
        String feeAmountInPence =
            String.valueOf(new BigDecimal(fee.getAmountAsString()).multiply(new BigDecimal("100")));
        asylumCase.write(FEE_CODE, fee.getCode());
        asylumCase.write(FEE_DESCRIPTION, fee.getDescription());
        asylumCase.write(FEE_VERSION, fee.getVersion());
        asylumCase.write(FEE_AMOUNT_GBP, feeAmountInPence);
        asylumCase.write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.YES);
    }

    private static void writeHearingSpecificDetails(AsylumCase asylumCase, Fee fee, String hearingOption) {
        if (DECISION_WITH_HEARING.equals(hearingOption)) {
            asylumCase.write(FEE_WITH_HEARING, fee.getAmountAsString());
            asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined with a hearing");
        } else if (DECISION_WITHOUT_HEARING.equals(hearingOption)) {
            asylumCase.write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
            asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined without a hearing");
        }
    }
}
