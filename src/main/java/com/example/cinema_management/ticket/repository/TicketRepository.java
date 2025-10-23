package com.example.cinema_management.ticket.repository;

import com.example.cinema_management.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByQrCode(String qrCode);
}
