package dev.pedrocosta.sentinel.presentation.api;

import dev.pedrocosta.sentinel.configuration.CorrelationIdFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class RiskAnalysisApiIntegrationTest {

    private static final String API_KEY = "integration-test-key";
    private static final String VALID_REQUEST = """
            {
              "transactionId": "tx-integration-1",
              "customerId": "customer-42",
              "amount": 12500.00,
              "currency": "BRL",
              "originCountry": "BR",
              "cardCountry": "US",
              "merchantCategory": "7995",
              "occurredAt": "2026-08-16T12:00:00Z"
            }
            """;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CorrelationIdFilter correlationIdFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .addFilters(correlationIdFilter)
                .build();
    }

    @Test
    void rejectsRequestsWithoutAnApiKey() throws Exception {
        mockMvc.perform(post("/api/v1/analyses")
                        .header("Idempotency-Key", "missing-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("A valid X-API-Key is required"));
    }

    @Test
    void healthEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void createsAndRetrievesAnExplainableDecision() throws Exception {
        MvcResult created = create("create-and-read", VALID_REQUEST)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.decision").value("DECLINED"))
                .andExpect(jsonPath("$.score").value(90))
                .andExpect(jsonPath("$.factors.length()").value(3))
                .andReturn();

        JsonNode response = objectMapper.readTree(created.getResponse().getContentAsString());
        String analysisId = response.get("analysisId").asText();

        mockMvc.perform(get("/api/v1/analyses/{analysisId}", analysisId)
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(analysisId))
                .andExpect(jsonPath("$.transactionId").value("tx-integration-1"));
    }

    @Test
    void replaysTheOriginalResponseForTheSameIdempotencyKey() throws Exception {
        MvcResult first = create("stable-replay", VALID_REQUEST)
                .andExpect(status().isCreated())
                .andReturn();
        MvcResult second = create("stable-replay", VALID_REQUEST)
                .andExpect(status().isCreated())
                .andReturn();

        String firstId = objectMapper.readTree(first.getResponse().getContentAsString())
                .get("analysisId").asText();
        String secondId = objectMapper.readTree(second.getResponse().getContentAsString())
                .get("analysisId").asText();
        assertThat(secondId).isEqualTo(firstId);
    }

    @Test
    void rejectsReuseOfAKeyWithAnotherPayload() throws Exception {
        create("conflicting-key", VALID_REQUEST).andExpect(status().isCreated());
        String changedRequest = VALID_REQUEST.replace("12500.00", "9000.00");

        create("conflicting-key", changedRequest)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Idempotency conflict"));
    }

    @Test
    void returnsFieldErrorsForAnInvalidPayload() throws Exception {
        String invalidRequest = VALID_REQUEST.replace("\"BRL\"", "\"BR\"");

        create("invalid-payload", invalidRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.currency").exists());
    }

    @Test
    void returnsNotFoundForAnUnknownAnalysis() throws Exception {
        mockMvc.perform(get("/api/v1/analyses/00000000-0000-0000-0000-000000000000")
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Analysis not found"));
    }

    private org.springframework.test.web.servlet.ResultActions create(
            String idempotencyKey,
            String request
    ) throws Exception {
        return mockMvc.perform(post("/api/v1/analyses")
                .header("X-API-Key", API_KEY)
                .header("Idempotency-Key", idempotencyKey)
                .header("X-Correlation-ID", "integration-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));
    }
}
