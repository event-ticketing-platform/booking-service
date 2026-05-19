package ee.ut.eventticketing.booking_service.client;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TicketingClientTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reserveTicketsForwardsCurrentBearerToken() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TicketingClient client = new TicketingClient(builder, "http://ticketing-service:8091");

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt("gateway-token")));

        server.expect(requestTo("http://ticketing-service:8091/ticket-types/ticket-type-1/reserve"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "Bearer gateway-token"))
                .andRespond(withSuccess());

        client.reserveTickets("ticket-type-1", 2);

        server.verify();
    }

    private Jwt jwt(String token) {
        return new Jwt(
                token,
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "customer"));
    }
}
