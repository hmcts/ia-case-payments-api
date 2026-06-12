package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.LegRepAddressUk;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationEntityResponse {

    private String organisationIdentifier;
    private String name;
    private String status;
    private String sraId;
    private String sraRegulated;
    private String companyNumber;
    private String companyUrl;
    private SuperUser superUser;
    @Getter(lombok.AccessLevel.NONE)
    private List<String> paymentAccount;
    @Getter(lombok.AccessLevel.NONE)
    private List<LegRepAddressUk> contactInformation;

    public OrganisationEntityResponse(String name) {
        this.name = name;
    }

    public List<String> getPaymentAccount() {
        return paymentAccount == null ? new ArrayList<>() : Collections.unmodifiableList(paymentAccount);
    }

    public List<LegRepAddressUk> getContactInformation() {
        return contactInformation == null ? new ArrayList<>() : Collections.unmodifiableList(contactInformation);
    }

}
