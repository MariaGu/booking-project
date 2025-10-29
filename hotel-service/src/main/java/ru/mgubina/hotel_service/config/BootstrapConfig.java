package ru.mgubina.hotel_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.mgubina.hotel_service.entity.Hotel;
import ru.mgubina.hotel_service.entity.Room;
import ru.mgubina.hotel_service.repository.HotelRepository;
import ru.mgubina.hotel_service.repository.RoomRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootstrapConfig implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) throws Exception {
        if (hotelRepository.count() == 0) {

            Hotel hotel1 = Hotel.builder()
                    .name("Grand Hotel")
                    .address("Irkutsk 101")
                    .build();
            hotelRepository.save(hotel1);

            Hotel hotel2 = Hotel.builder()
                    .name("Grand Hotel2")
                    .address("Irkutsk 102")
                    .build();
            hotelRepository.save(hotel2);

            for (int i = 1001; i <= 1008; i++) {
                roomRepository.save(Room.builder().hotel(hotel1).number(i + "").build());
            }

            for (int i = 2001; i <= 2010; i++) {
                Room room = new Room();
                room.setHotel(hotel2);
                room.setNumber(String.valueOf(i));
                room.setAvailable(true);
                room.setTimesBooked(0);
                roomRepository.save(Room.builder().hotel(hotel2).number(i + "").build());
            }
        } else {
            log.info("Bootstrap skipped: hotels already exist");
        }
    }
}

