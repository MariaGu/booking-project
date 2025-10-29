package ru.mgubina.booking_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import ru.mgubina.booking_service.dto.AuthRequest;
import ru.mgubina.booking_service.dto.CreateBookingRequest;
import ru.mgubina.booking_service.dto.TokenResponse;
import ru.mgubina.booking_service.repository.UserRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebMvc
class BookingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private String userToken;
    private String adminToken;
    private String BOOKING_API = "/api/booking";

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;

        userToken = getToken("user", "pass");
        adminToken = getToken("admin", "admin");
    }

    private String getToken(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AuthRequest> entity = new HttpEntity<>(new AuthRequest(username, password), headers);
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/user/auth", entity, TokenResponse.class);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        return response.getBody().getToken();
    }

    @Test
    void testGetUserBookings() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + BOOKING_API, HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void userRoleAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + BOOKING_API, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUnauthorizedAccess() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .roomId(1L)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(15))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateBookingRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + BOOKING_API, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testSuccessfulBooking() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .roomId(1L)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(15))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userToken);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + BOOKING_API, new HttpEntity<>(request, headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("CANCELLED"));
    }
}
