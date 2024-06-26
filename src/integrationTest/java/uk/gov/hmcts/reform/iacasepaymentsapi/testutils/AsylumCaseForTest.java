package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition;

import java.util.Map;

import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.PAYMENT_CASE_REFERENCE;

public class AsylumCaseForTest implements Builder<AsylumCase> {

    private AsylumCase asylumCase = new AsylumCase();

    public static AsylumCaseForTest anAsylumCase() {
        return new AsylumCaseForTest();
    }

    public AsylumCaseForTest withCaseDetails(AsylumCase asylumCase) {
        this.asylumCase.putAll(asylumCase);
        return this;
    }

    public <T> AsylumCaseForTest with(AsylumCaseDefinition field, T value) {
        asylumCase.write(field, value);
        return this;
    }

    public AsylumCaseForTest writeOrOverwrite(Map<String, Object> additionalAsylumCaseData) {
        asylumCase.putAll(additionalAsylumCaseData);
        return this;
    }

    public AsylumCase build() {
        return asylumCase;
    }

    public static AsylumCase generateValidPaymentStatusAsylumCase() {
        return anAsylumCase().with(AsylumCaseDefinition.PAYMENT_REFERENCE, PAYMENT_CASE_REFERENCE)
            .with(AsylumCaseDefinition.APPEAL_TYPE, HU).build();
    }
}
