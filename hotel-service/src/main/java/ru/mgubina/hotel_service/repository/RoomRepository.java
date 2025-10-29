package ru.mgubina.hotel_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mgubina.hotel_service.entity.Room;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.timesBooked ASC, r.id ASC")
    List<Room> findAvailableRoomsOrderedByTimesBooked();

    List<Room> findByHotelId(Long hotelId);

    List<Room> findByAvailableTrue();
}
