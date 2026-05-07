package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EventTest {
    @ParameterizedTest
    @MethodSource("eventMapping")
    void has_correct_values(String expected, String actual) {
        assertEquals(expected, actual);
    }

    @Test
    void if_this_test_fails_it_is_because_eventMapping_needs_updating_with_your_changes() {
        List<String> eventMappingStrings = eventMapping().map(arg -> arg.get()[1])
            .map(String.class::cast)
            .toList();
        List<Event> missingEvents = Arrays.stream(Event.values())
            .filter(event -> !eventMappingStrings.contains(event.toString())).toList();
        assertTrue(missingEvents.isEmpty(), "The following events are missing from the eventMapping method: " + missingEvents);
    }

    static Stream<Arguments> eventMapping() {
        return Stream.of(
            Arguments.of("startAppeal", Event.START_APPEAL.toString()),
            Arguments.of("editAppeal", Event.EDIT_APPEAL.toString()),
            Arguments.of("paymentAppeal", Event.PAYMENT_APPEAL.toString()),
            Arguments.of("submitAppeal", Event.SUBMIT_APPEAL.toString()),
            Arguments.of("payAndSubmitAppeal", Event.PAY_AND_SUBMIT_APPEAL.toString()),
            Arguments.of("payForAppeal", Event.PAY_FOR_APPEAL.toString()),
            Arguments.of("recordRemissionDecision", Event.RECORD_REMISSION_DECISION.toString()),
            Arguments.of("updatePaymentStatus", Event.UPDATE_PAYMENT_STATUS.toString()),
            Arguments.of("generateServiceRequest", Event.GENERATE_SERVICE_REQUEST.toString()),
            Arguments.of("unknown", Event.UNKNOWN.toString())
        );
    }
}
