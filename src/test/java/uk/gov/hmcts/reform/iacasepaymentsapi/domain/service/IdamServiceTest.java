package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.UserInfo;

@ExtendWith(MockitoExtension.class)
class IdamServiceTest {
    @Mock
    private IdamApi idamApi;

    private IdamService idamService;

    @Test
    void getUserDetails() {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedRoles = Arrays.asList("role-1", "role-2");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        idamService = new IdamService(idamApi);

        UserInfo expecteduUerInfo = new UserInfo(
                expectedEmailAddress,
                expectedId,
                expectedRoles,
                expectedName,
                expectedForename,
                expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);

        UserInfo actualUserInfo = idamService.getUserInfo(expectedAccessToken);
        verify(idamApi).userInfo(expectedAccessToken);

        assertEquals(expectedId, actualUserInfo.getUid());
        assertEquals(expectedRoles, actualUserInfo.getRoles());
        assertEquals(expectedEmailAddress, actualUserInfo.getEmail());
        assertEquals(expectedForename, actualUserInfo.getGivenName());
        assertEquals(expectedSurname, actualUserInfo.getFamilyName());
    }

    @Test
    void getUserToken() {
        String expectedAccessToken = "ABCDEFG";
        String expectedScope = "systemUserScope";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("redirect_uri", "idamRedirectUrl");
        map.add("client_id", "idamClientId");
        map.add("client_secret", "idamClientSecret");
        map.add("username", "systemUserName");
        map.add("password", "systemUserPass");
        map.add("scope", expectedScope);

        idamService = new IdamService(idamApi);
        Token expectedToken = new Token(expectedAccessToken, expectedScope);
        when(idamApi.token(anyMap())).thenReturn(expectedToken);

        Token actualUserToken = idamService.getUserToken(map);
        verify(idamApi).token(map);

        assertEquals(expectedAccessToken, actualUserToken.getAccessToken());
        assertEquals(expectedScope, actualUserToken.getScope());
    }
}
