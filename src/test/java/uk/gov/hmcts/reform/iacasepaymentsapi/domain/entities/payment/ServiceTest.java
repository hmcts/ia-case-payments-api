package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Test;

public class ServiceTest {

    @Test
    public void should_have_correct_values() {

        assertThat("IAC", is(Service.IAC.toString()));
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {

        assertEquals(1, Service.values().length);
    }
}
