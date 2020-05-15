package uk.gov.hmcts.reform.iacasepaymentsapi.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.ORAL_FEE_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;

import groovy.util.logging.Slf4j;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.fixtures.PreSubmitCallbackResponseForTest;

@Slf4j
public class AppealSubmitFeeIntegrationTest extends SpringBootIntegrationTest {

    @Test
    public void should_retrieve_the_fee_amount_for_the_appeal() {

        PreSubmitCallbackResponseForTest response = iaCasePaymentsApiClient.aboutToStart(callback()
            .event(Event.SUBMIT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, "refusalOfEu"))));

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(APPEAL_FEE_DESC, String.class),
            Optional.of("The fee for this type of appeal with a hearing is £140.00"));
        assertEquals(asylumCase.read(ORAL_FEE_AMOUNT_FOR_DISPLAY), Optional.of("£140.00"));
    }
}
