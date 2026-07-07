package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.roleassignment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AssignmentTest {

    @Test
    void has_correct_values() {

        LocalDateTime now = LocalDateTime.now();

        Assignment assignment = new Assignment(
            "id",
            now,
            Collections.emptyList(),
            ActorIdType.IDAM,
            "actorId",
            RoleType.CASE,
            RoleName.TRIBUNAL_CASEWORKER,
            RoleCategory.JUDICIAL,
            Classification.PRIVATE,
            GrantType.BASIC,
            true,
            Collections.emptyMap()
        );

        assertEquals(
            "Assignment(id=id, "
            + "created=" + now.toString() + ","
            + " authorisations=[],"
            + " actorIdType=IDAM,"
            + " actorId=actorId,"
            + " roleType=CASE,"
            + " roleName=tribunal-caseworker,"
            + " roleCategory=JUDICIAL,"
            + " classification=PRIVATE,"
            + " grantType=BASIC,"
            + " readOnly=true,"
            + " attributes={}"
            + ")",
            assignment.toString()
        );

        assertEquals("id", assignment.getId());
        assertEquals(now, assignment.getCreated());
        assertEquals(Collections.emptyList(), assignment.getAuthorisations());
        assertEquals(ActorIdType.IDAM, assignment.getActorIdType());
        assertEquals("actorId", assignment.getActorId());
        assertEquals(RoleType.CASE, assignment.getRoleType());
        assertEquals(RoleName.TRIBUNAL_CASEWORKER, assignment.getRoleName());
        assertEquals(RoleCategory.JUDICIAL, assignment.getRoleCategory());
        assertEquals(Classification.PRIVATE, assignment.getClassification());
        assertEquals(GrantType.BASIC, assignment.getGrantType());
        assertEquals(Collections.<String, String>emptyMap(), assignment.getAttributes());
    }

    @Test
    void should_return_empty_list_when_authorisations_is_null() {
        Assignment assignment = new Assignment(
            "id",
            LocalDateTime.now(),
            null,
            ActorIdType.IDAM,
            "actorId",
            RoleType.CASE,
            RoleName.TRIBUNAL_CASEWORKER,
            RoleCategory.JUDICIAL,
            Classification.PRIVATE,
            GrantType.BASIC,
            true,
            Collections.emptyMap()
        );

        assertEquals(Collections.emptyList(), assignment.getAuthorisations());
    }

    @Test
    void should_return_empty_map_when_attributes_is_null() {
        Assignment assignment = new Assignment(
            "id",
            LocalDateTime.now(),
            Collections.emptyList(),
            ActorIdType.IDAM,
            "actorId",
            RoleType.CASE,
            RoleName.TRIBUNAL_CASEWORKER,
            RoleCategory.JUDICIAL,
            Classification.PRIVATE,
            GrantType.BASIC,
            true,
            null
        );

        assertEquals(Collections.emptyMap(), assignment.getAttributes());
    }

    @Test
    void should_return_unmodifiable_list_for_authorisations() {
        List<String> authorisations = Collections.singletonList("auth1");
        Assignment assignment = new Assignment(
            "id",
            LocalDateTime.now(),
            authorisations,
            ActorIdType.IDAM,
            "actorId",
            RoleType.CASE,
            RoleName.TRIBUNAL_CASEWORKER,
            RoleCategory.JUDICIAL,
            Classification.PRIVATE,
            GrantType.BASIC,
            true,
            Collections.emptyMap()
        );

        assertEquals(authorisations, assignment.getAuthorisations());
    }

    @Test
    void should_return_unmodifiable_map_for_attributes() {
        Map<String, String> attributes = Collections.singletonMap("key", "value");
        Assignment assignment = new Assignment(
            "id",
            LocalDateTime.now(),
            Collections.emptyList(),
            ActorIdType.IDAM,
            "actorId",
            RoleType.CASE,
            RoleName.TRIBUNAL_CASEWORKER,
            RoleCategory.JUDICIAL,
            Classification.PRIVATE,
            GrantType.BASIC,
            true,
            attributes
        );

        assertEquals(attributes, assignment.getAttributes());
    }
}
