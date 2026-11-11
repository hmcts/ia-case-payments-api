package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.serialization;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StdSerializerTest {

    private final StdSerializer<Object> serializer = new StdSerializer<>(new JsonMapper());

    @Test
    void should_serialize_object() {
        assertEquals("{\"a\":\"b\"}", serializer.serialize(Map.of("a", "b")));
    }

    @Test
    void should_wrap_jackson_exception() {
        Map<String, Object> self = new HashMap<>();
        self.put("me", self); // self referencing cycle forces jackson exception

        assertThatThrownBy(() -> serializer.serialize(self))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Could not serialize data")
            .hasCauseInstanceOf(JacksonException.class);
    }
}
