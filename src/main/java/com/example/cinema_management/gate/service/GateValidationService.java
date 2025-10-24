package com.example.cinema_management.gate.service;

import com.example.cinema_management.ticket.entity.Ticket;
import com.example.cinema_management.ticket.entity.TicketStatus;
import com.example.cinema_management.ticket.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Validates a ticket from a QR payload and marks it USED if valid.
 * Payload formats supported: "TICKET:<id>" or "TICKET:<id>;SCHEDULE:<sid>"
 */
@Service
public class GateValidationService {

    private final TicketRepository ticketRepo;

    private static final Pattern ONLY_TICKET = Pattern.compile("^\\s*TICKET:(\\d+)\\s*$");
    private static final Pattern TICKET_SCHEDULE = Pattern.compile("^\\s*TICKET:(\\d+);SCHEDULE:(\\d+)\\s*$");

    public GateValidationService(TicketRepository ticketRepo) {
        this.ticketRepo = ticketRepo;
    }

    @Transactional
    public Result validate(String qrPayload) {
        if (qrPayload == null || qrPayload.isBlank()) {
            throw new IllegalArgumentException("QR token is required");
        }

        Long ticketId = parseTicketId(qrPayload);

        Ticket t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        if (t.getStatus() == TicketStatus.USED) {
            return Result.alreadyUsed(Integer.parseInt(t.getSeatNumber()), t.getUpdatedAt());
        }
        if (t.getStatus() == TicketStatus.CANCELLED || t.getStatus() == TicketStatus.EXPIRED) {
            return Result.invalid("Ticket is " + t.getStatus());
        }

        t.setStatus(TicketStatus.USED);
        t.setUpdatedAt(LocalDateTime.now());
        ticketRepo.save(t);

        return Result.valid(Integer.parseInt(t.getSeatNumber()), t.getUpdatedAt());
    }

    private Long parseTicketId(String token) {
        var m1 = ONLY_TICKET.matcher(token);
        if (m1.matches()) return Long.parseLong(m1.group(1));

        var m2 = TICKET_SCHEDULE.matcher(token);
        if (m2.matches()) return Long.parseLong(m2.group(1));

        throw new IllegalArgumentException("Invalid QR format");
    }

    public record Result(boolean success, String message, Integer seatNumber, LocalDateTime usedAt) {
        public static Result valid(Integer seat, LocalDateTime usedAt) {
            return new Result(true, "VALID – entry granted", seat, usedAt);
        }
        public static Result alreadyUsed(Integer seat, LocalDateTime usedAt) {
            return new Result(false, "ALREADY USED – deny entry", seat, usedAt);
        }
        public static Result invalid(String reason) {
            return new Result(false, "INVALID – " + reason, null, null);
        }
    }
}
