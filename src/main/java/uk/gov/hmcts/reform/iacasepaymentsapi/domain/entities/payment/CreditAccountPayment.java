package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.List;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreditAccountPayment {

    private String accountNumber;
    private BigDecimal amount;
    private String caseReference;
    private String ccdCaseNumber;
    private Currency currency;
    private String customerReference;
    private String description;
    private String organisationName;
    private Service service;
    private String siteId;
    private List<Fee> fees;

    private CreditAccountPayment() {
    }

    public CreditAccountPayment(String accountNumber,
                                BigDecimal amount,
                                String caseReference,
                                String ccdCaseNumber,
                                Currency currency,
                                String customerReference,
                                String description,
                                List<Fee> fees) {
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.caseReference = caseReference;
        this.ccdCaseNumber = ccdCaseNumber;
        this.currency = currency;
        this.customerReference = customerReference;
        this.description = description;
        this.fees = fees;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCaseReference() {
        return caseReference;
    }

    public String getCcdCaseNumber() {
        return ccdCaseNumber;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public String getDescription() {
        return description;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public Service getService() {
        return service;
    }

    public String getSiteId() {
        return siteId;
    }

    public List<Fee> getFees() {
        return fees;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
}
