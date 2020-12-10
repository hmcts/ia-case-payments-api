package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus;

public enum AsylumCaseDefinition {

    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),
    LEGAL_REP_REFERENCE_NUMBER(
        "legalRepReferenceNumber", new TypeReference<String>(){}),
    FEE_WITH_HEARING(
        "feeWithHearing", new TypeReference<String>(){}),
    FEE_WITHOUT_HEARING(
        "feeWithoutHearing", new TypeReference<String>(){}),
    APPEAL_FEE_HEARING_DESC(
        "appealFeeHearingDesc", new TypeReference<String>(){}),
    APPEAL_FEE_WITHOUT_HEARING_DESC(
        "appealFeeWithoutHearingDesc", new TypeReference<String>(){}),
    FEE_CODE(
        "feeCode", new TypeReference<String>(){}),
    FEE_DESCRIPTION(
        "feeDescription", new TypeReference<String>(){}),
    FEE_VERSION(
        "feeVersion", new TypeReference<String>(){}),
    FEE_AMOUNT_GBP(
        "feeAmountGbp", new TypeReference<String>(){}),
    FEE_AMOUNT_FOR_DISPLAY(
        "feeAmountForDisplay", new TypeReference<String>(){}),
    PAYMENT_STATUS(
        "paymentStatus", new TypeReference<PaymentStatus>(){}),
    PAYMENT_FAILED_FOR_DISPLAY(
        "paymentFailedForDisplay", new TypeReference<String>(){}),
    FEE_PAYMENT_APPEAL_TYPE(
        "feePaymentAppealType", new TypeReference<String>(){}),
    PBA_NUMBER(
        "pbaNumber", new TypeReference<String>(){}),
    PAYMENT_ACCOUNT_LIST(
        "paymentAccountList", new TypeReference<DynamicList>(){}),
    PAYMENT_DESCRIPTION(
        "paymentDescription", new TypeReference<String>(){}),
    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>(){}),
    PAYMENT_REFERENCE(
        "paymentReference", new TypeReference<String>(){}),
    PAYMENT_DATE(
        "paymentDate", new TypeReference<String>(){}),
    PAYMENT_ERROR_CODE(
        "paymentErrorCode", new TypeReference<String>(){}),
    PAYMENT_ERROR_MESSAGE(
        "paymentErrorMessage", new TypeReference<String>(){}),
    APPEAL_TYPE(
        "appealType", new TypeReference<AppealType>(){}),
    HEARING_DECISION_SELECTED(
        "hearingDecisionSelected", new TypeReference<String>(){}),
    DECISION_WITH_HEARING(
        "decisionWithHearing", new TypeReference<String>(){}),
    DECISION_WITHOUT_HEARING(
        "decisionWithoutHearing", new TypeReference<String>(){}),
    DECISION_HEARING_FEE_OPTION(
        "decisionHearingFeeOption", new TypeReference<String>(){}),

    REMISSION_TYPE(
        "remissionType", new TypeReference<RemissionType>(){});

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
