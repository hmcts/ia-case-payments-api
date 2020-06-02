package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;

public enum AsylumCaseDefinition {

    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),

    APPEAL_FEE_HEARING_DESC(
        "appealFeeHearingDesc", new TypeReference<String>(){}),

    FEE_HEARING_AMOUNT_FOR_DISPLAY(
        "feeHearingAmountForDisplay", new TypeReference<String>(){}),

    APPEAL_FEE_WITHOUT_HEARING_DESC(
        "appealFeeWithoutHearingDesc", new TypeReference<String>(){}),

    FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY(
        "feeWithoutHearingAmountForDisplay", new TypeReference<String>(){}),

    PAYMENT_STATUS(
        "paymentStatus", new TypeReference<String>(){}),

    FEE_PAYMENT_APPEAL_TYPE(
        "feePaymentAppealType", new TypeReference<String>(){}),

    APPEAL_TYPE(
        "appealType", new TypeReference<AppealType>(){});

    private final String value;
    private final TypeReference typeReference;

    AsylumCaseDefinition(String value, TypeReference typeReference) {
        this.value = value;
        this.typeReference = typeReference;
    }

    public String value() {
        return value;
    }

    public TypeReference getTypeReference() {
        return typeReference;
    }
}
