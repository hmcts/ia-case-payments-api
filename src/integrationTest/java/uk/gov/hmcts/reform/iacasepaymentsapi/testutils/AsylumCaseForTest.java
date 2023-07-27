package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition;

import java.util.Map;

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
        return anAsylumCase().with(AsylumCaseDefinition.PAYMENT_REFERENCE, "RC-1627-5070-9329-7815").build();
    }
}
