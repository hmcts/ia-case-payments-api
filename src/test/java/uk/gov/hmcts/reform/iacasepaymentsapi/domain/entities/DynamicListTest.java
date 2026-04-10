package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DynamicListTest {

    private final Value codeValue1 = new Value("code123", "code123");
    private final Value codeValue2 = new Value("code456", "code456");
    private final Value codeValue3 = new Value("code789", "code789");

    private List<Value> accountsFromOrg = new ArrayList<Value>();

    @Test
    void should_hold_onto_values() {

        accountsFromOrg.add(codeValue1);
        accountsFromOrg.add(codeValue2);
        accountsFromOrg.add(codeValue3);

        DynamicList accountList = new DynamicList(accountsFromOrg.getFirst(), accountsFromOrg);

        assertEquals(codeValue1, accountList.getValue());
        assertEquals(codeValue1, accountList.getListItems().getFirst());
        assertEquals(codeValue2, accountList.getListItems().get(1));
        assertEquals(codeValue3, accountList.getListItems().get(2));

        assertEquals(accountsFromOrg, accountList.getListItems());
    }

}
