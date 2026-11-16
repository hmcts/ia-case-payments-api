package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit.ServiceRequestHelper;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class SubmitAppealCreateServiceRequestHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private ServiceRequestHelper serviceRequestHelper;

    private SubmitAppealCreateServiceRequestHandler submitAppealCreateServiceRequestHandler;

    @BeforeEach
    public void setUp() {
        submitAppealCreateServiceRequestHandler = new SubmitAppealCreateServiceRequestHandler(serviceRequestHelper);

        lenient().when(callback.getCaseDetails()).thenReturn(caseDetails);
        lenient().when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_call_service_request_helper_when_handling() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(serviceRequestHelper.createServiceRequestIfRequired(callback)).thenReturn(true);

        PostSubmitCallbackResponse callbackResponse =
            submitAppealCreateServiceRequestHandler.handle(PostSubmitCallbackStage.CCD_SUBMITTED, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestHelper, times(1)).createServiceRequestIfRequired(callback);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> submitAppealCreateServiceRequestHandler
            .handle(PostSubmitCallbackStage.CCD_SUBMITTED, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PostSubmitCallbackStage callbackStage : PostSubmitCallbackStage.values()) {
                boolean canHandle = submitAppealCreateServiceRequestHandler.canHandle(callbackStage, callback);

                if (callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                    && callback.getEvent() == Event.SUBMIT_APPEAL) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> submitAppealCreateServiceRequestHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> submitAppealCreateServiceRequestHandler.canHandle(PostSubmitCallbackStage.CCD_SUBMITTED, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}