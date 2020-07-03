package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationResponse {

    private String organisationIdentifier;
    private String name;
    private String status;
    private String sraId;
    private String sraRegulated;
    private String companyNumber;
    private String companyUrl;
    private List<String> superUser;
    private List<String> pbaAccountList;
    private String contactInformation;

    public OrganisationResponse(String name, List<String> pbaAccountList) {
        this.name = name;
        this.pbaAccountList = pbaAccountList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPbaAccountList() {
        return pbaAccountList;
    }
}
