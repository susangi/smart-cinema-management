package com.example.cinema_management.booking.repository;

import com.example.cinema_management.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {}

