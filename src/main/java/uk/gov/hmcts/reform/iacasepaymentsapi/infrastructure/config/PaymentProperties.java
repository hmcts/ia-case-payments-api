package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("payment.params")
public class PaymentProperties {

    private String organisationUrn;
    private String caseType;

    public String getOrganisationUrn() {
        return organisationUrn;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setOrganisationUrn(String organisationUrn) {
        this.organisationUrn = organisationUrn;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }
}
