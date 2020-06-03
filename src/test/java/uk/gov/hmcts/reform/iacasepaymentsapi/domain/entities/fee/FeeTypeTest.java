package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Test;

public class FeeTypeTest {

    @Test
    public void should_have_correct_values() {

        assertThat("feeWithHearing", is(FeeType.FEE_WITH_HEARING.toString()));
        assertThat("feeWithoutHearing", is(FeeType.FEE_WITHOUT_HEARING.toString()));
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {

        assertEquals(2, FeeType.values().length);
    }
}
