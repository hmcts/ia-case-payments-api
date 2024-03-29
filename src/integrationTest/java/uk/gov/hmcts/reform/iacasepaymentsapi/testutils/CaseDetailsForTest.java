package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.CCD_CASE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient.JURISDICTION;

@Data
public class CaseDetailsForTest {

    private long id;
    private String jurisdiction;
    private State state;
    @JsonProperty("case_data")
    private AsylumCase caseData;
    @JsonProperty("created_date")
    private LocalDateTime createdDate;

    CaseDetailsForTest(long id, String jurisdiction, State state, AsylumCase caseData, LocalDateTime createdDate) {
        this.id = id;
        this.jurisdiction = jurisdiction;
        this.state = state;
        this.caseData = caseData;
        this.createdDate = createdDate;
    }

    public static class CaseDetailsForTestBuilder implements Builder<CaseDetailsForTest> {

        public static CaseDetailsForTestBuilder someCaseDetailsWith() {
            return new CaseDetailsForTestBuilder();
        }

        private long id = 1;
        private String jurisdiction = JURISDICTION;
        private State state;
        private AsylumCase caseData;
        private LocalDateTime createdDate = LocalDateTime.now();

        CaseDetailsForTestBuilder() {
        }

        public CaseDetailsForTestBuilder id(long id) {
            this.id = id;
            return this;
        }

        public CaseDetailsForTestBuilder jurisdiction(String jurisdiction) {
            this.jurisdiction = jurisdiction;
            return this;
        }

        public CaseDetailsForTestBuilder state(State state) {
            this.state = state;
            return this;
        }

        public CaseDetailsForTestBuilder caseData(AsylumCaseForTest caseData) {
            this.caseData = caseData.build();
            return this;
        }

        public CaseDetailsForTestBuilder createdDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public CaseDetailsForTest build() {
            return new CaseDetailsForTest(id, jurisdiction, state, caseData, createdDate);
        }

        public String toString() {
            return "CaseDetailsForTest.CaseDetailsForTestBuilder(id="
                   + this.id + ", jurisdiction="
                   + this.jurisdiction + ", state="
                   + this.state + ", caseData="
                   + this.caseData + ", createdDate="
                   + this.createdDate + ")";
        }
    }

    public static CaseDetails<AsylumCase> generateValidCaseDetailWithAsylumCase() {
        return new CaseDetails<>(
            Long.parseLong(CCD_CASE_NUMBER),
            JURISDICTION,
            State.PENDING_PAYMENT,
            AsylumCaseForTest.generateValidPaymentStatusAsylumCase()
        );
    }

}
