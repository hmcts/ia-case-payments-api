package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import tools.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Optional;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.JsonMapperFactory;

public class AsylumCase extends HashMap<String, Object> implements CaseData {

    private final ObjectMapper objectMapper = JsonMapperFactory.CUSTOM_MAPPER;

    public AsylumCase() {
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> read(AsylumCaseDefinition asylumCaseDefinition) {

        Object o = this.get(asylumCaseDefinition.value());

        if (o == null) {
            return Optional.empty();
        }

        Object value = objectMapper.convertValue(o, asylumCaseDefinition.getTypeReference());

        return Optional.of((T) value);
    }

    public <T> Optional<T> read(AsylumCaseDefinition asylumCaseDefinition, Class<T> type) {
        return this.read(asylumCaseDefinition);
    }

    public <T> void write(AsylumCaseDefinition asylumCaseDefinition, T value) {
        this.put(asylumCaseDefinition.value(), value);
    }

    public void clear(AsylumCaseDefinition asylumCaseDefinition) {
        this.put(asylumCaseDefinition.value(), null);
    }
}
