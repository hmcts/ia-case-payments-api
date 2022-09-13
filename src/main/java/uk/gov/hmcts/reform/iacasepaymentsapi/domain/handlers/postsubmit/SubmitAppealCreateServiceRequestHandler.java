package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit.ErrorHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit.FeesHelper;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.ServiceRequestService;

@Component
public class SubmitAppealCreateServiceRequestHandler implements PostSubmitCallbackHandler<AsylumCase> {

    private final ServiceRequestService serviceRequestService;
    private final FeeService feeService;

    private final Optional<ErrorHandler<AsylumCase>> errorHandling;

    public SubmitAppealCreateServiceRequestHandler(
        ServiceRequestService serviceRequestService,
        FeeService feeService,
        Optional<ErrorHandler<AsylumCase>> errorHandling) {
        this.serviceRequestService = serviceRequestService;
        this.feeService = feeService;
        this.errorHandling = errorHandling;
    }

    public boolean canHandle(
        PostSubmitCallbackStage callbackStage, Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PostSubmitCallbackResponse handle(
        PostSubmitCallbackStage callbackStage, Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Fee fee = FeesHelper.findFeeByHearingType(feeService, asylumCase);

        if (isWaysToPay(callback, isLegalRepJourney(asylumCase))
            && hasNoRemission(asylumCase)) {
            try {
                ServiceRequestResponse serviceRequestResponse = serviceRequestService
                    .createServiceRequest(callback, fee);
            } catch (Exception e) {
                errorHandling.ifPresent(asylumCaseErrorHandler -> asylumCaseErrorHandler.accept(callback, e));
            }
        }
        return new PostSubmitCallbackResponse();
    }

    private boolean isWaysToPay(Callback<AsylumCase> callback,
                                boolean isLegalRepJourney) {

        List<Event> waysToPayEvents = List.of(Event.SUBMIT_APPEAL,
                                              Event.GENERATE_SERVICE_REQUEST,
                                              Event.RECORD_REMISSION_DECISION);

        return waysToPayEvents.contains(callback.getEvent())
               && isLegalRepJourney
               && isHuOrEaOrPa(callback.getCaseDetails().getCaseData());
    }

    private boolean isHuOrEaOrPa(AsylumCase asylumCase) {
        Optional<AppealType> optionalAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);
        if (optionalAppealType.isPresent()) {
            AppealType appealType = optionalAppealType.get();
            return appealType.equals(AppealType.EA)
                   || appealType.equals(AppealType.HU)
                   || appealType.equals(AppealType.PA);
        }
        return false;
    }

    private boolean isLegalRepJourney(AsylumCase asylumCase) {
        return asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(journey -> journey == JourneyType.REP)
            .orElse(true);
    }

    private boolean hasNoRemission(AsylumCase asylumCase) {
        Optional<RemissionType> optRemissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        Optional<RemissionDecision> optionalRemissionDecision =
            asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        return (optRemissionType.isPresent() && optRemissionType.get() == RemissionType.NO_REMISSION)
               || optRemissionType.isEmpty()
               || (optionalRemissionDecision.isPresent()
                   && optionalRemissionDecision.get() == RemissionDecision.REJECTED);
    }
}
