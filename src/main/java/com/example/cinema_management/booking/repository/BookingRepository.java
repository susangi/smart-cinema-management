package com.example.cinema_management.booking.repository;

import com.example.cinema_management.booking.entity.Booking;
import com.example.cinema_management.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

//    // Sum total price by booking status
//    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = :status")
//    BigDecimal sumTotalPriceByStatus(@Param("status") BookingStatus status);
//
//    // Alternative: Sum revenue for confirmed bookings with successful payments
//    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
//    BigDecimal sumTotalRevenue();
//
//    // Get revenue for a specific period
//    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = 'CONFIRMED' AND b.createdAt BETWEEN :startDate AND :endDate")
//    BigDecimal sumRevenueByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}

