package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit.ServiceRequestHelper;

@Component
public class SubmitAppealCreateServiceRequestHandler implements PostSubmitCallbackHandler<AsylumCase> {

    private final ServiceRequestHelper serviceRequestHelper;

    public SubmitAppealCreateServiceRequestHandler(ServiceRequestHelper serviceRequestHelper) {
        this.serviceRequestHelper = serviceRequestHelper;
    }

    @Override
    public boolean canHandle(
        PostSubmitCallbackStage callbackStage, Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
            && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    @Override
    public PostSubmitCallbackResponse handle(
        PostSubmitCallbackStage callbackStage, Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        serviceRequestHelper.createServiceRequestIfRequired(callback);

        return new PostSubmitCallbackResponse();
    }
}