package uk.gov.hmcts.reform.iacasepaymentsapi.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.CacheConfiguration;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.oauth2.IdamSystemTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Import({ CacheConfiguration.class, IdamSystemTokenGenerator.class, IdamService.class })
@ExtendWith(SpringExtension.class)
@EnableCaching
class CacheConfigurationTest {

    private static final String BEARER_AUTH = "Bearer ";
    private static final String TOKEN = "SOME_TOKEN";

    // need these static so container starts before spring application context
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    static {
        redis.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.url", () ->
            String.format("redis://localhost:%d", redis.getMappedPort(6379))
        );
    }

    @Autowired
    private IdamService idamService;

    @Autowired
    private IdamSystemTokenGenerator idamSystemTokenGenerator;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private RoleAssignmentService ras;

    @BeforeEach
    void setUp() {
        given(idamApi.token(any()))
            .willReturn(new Token(TOKEN, BEARER_AUTH));
    }

    @AfterAll
    static void tearDown() {
        redis.stop();
    }

    @Test
    void givenRedisCaching_whenFindItemById_thenItemReturnedFromCache() {
        String itemCacheMiss = idamSystemTokenGenerator.generate();
        String itemCacheHit = idamSystemTokenGenerator.generate();

        assertThat(itemCacheMiss).isEqualTo(BEARER_AUTH + TOKEN);
        assertThat(itemCacheHit).isEqualTo(BEARER_AUTH + TOKEN);

        verify(idamApi, times(1)).token(any());
    }

}
