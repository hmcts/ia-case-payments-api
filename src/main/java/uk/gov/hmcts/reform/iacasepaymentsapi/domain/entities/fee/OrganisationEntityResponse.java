package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private List<String> pbaAccountList;
    private String contactInformation;

    public OrganisationEntityResponse(String name) {
        this.name = name;
    }

    public List<String> getPbaAccountList() {
        return pbaAccountList;
    }

}
