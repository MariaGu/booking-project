package ru.mgubina.hotel_service.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import ru.mgubina.hotel_service.dto.CreateHotelRequest;
import ru.mgubina.hotel_service.dto.CreateRoomRequest;
import ru.mgubina.hotel_service.entity.Hotel;
import ru.mgubina.hotel_service.entity.Room;
import ru.mgubina.hotel_service.repository.HotelRepository;
import ru.mgubina.hotel_service.repository.RoomRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HotelIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    private String baseUrl;
    private String adminToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;

        Hotel testHotel = Hotel.builder()
                .name("Hotel")
                .address("Address")
                .build();
          hotelRepository.save(testHotel);

        Room room1 = Room.builder()
                .hotel(testHotel)
                .number("11")
                .available(true)
                .timesBooked(0)
                .build();

        roomRepository.save(room1);

        Room room2 = Room.builder()
                .hotel(testHotel)
                .number("22")
                .available(true)
                .timesBooked(2)
                .build();
        roomRepository.save(room2);
    }

    @Test
    void testGetHotels() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/hotels", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetRecommendedRooms() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/rooms/recommend", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetRooms() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/rooms", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testConfirmAvailability() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-Id", "test-request-123");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Boolean> response = restTemplate.postForEntity(
                baseUrl + "/api/rooms/1/confirm-availability", entity, Boolean.class);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody());
    }

    @Test
    void testIncrementTimesBooked() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-Id", "test-increment-789");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl + "/api/rooms/1/increment-bookings", entity, Void.class);

        assertTrue(response.getStatusCode().is2xxSuccessful());
    }


    @Test
    void testUserRoleAccessToHotels() {
        String userToken = "any-token";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/hotels", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testUserRoleAccessToRecommendedRooms() {
        // Тест доступа USER роли к рекомендованным номерам (должен получить 401 из-за невалидного токена)
        String userToken = "invalid-token";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/rooms/recommend", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testRealRoleAuthorization() {
          String invalidAdminToken = "any-admin-token";

        CreateHotelRequest request = new CreateHotelRequest();
        request.setName("Hotel");
        request.setAddress("Address");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidAdminToken);

        HttpEntity<CreateHotelRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/hotels", entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
