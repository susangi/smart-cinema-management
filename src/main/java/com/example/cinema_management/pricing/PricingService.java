package com.example.cinema_management.pricing;

import com.example.cinema_management.pricing.dto.PricingCreateRequest;
import com.example.cinema_management.pricing.dto.PricingTypeUpdateRequest;
import com.example.cinema_management.pricing.dto.PricingUpdateRequest;
import com.example.cinema_management.schedule.entity.Schedule;
import com.example.cinema_management.schedule.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * PricingService
 *
 * Application service for managing Pricing aggregates and their seat-type prices.
 * Responsibilities:
 *  - Paginate/list pricing definitions
 *  - Create/update/delete pricing
 *  - Upsert seat-type price rows (ADULT/CHILD)
 *  - Compute totals for a given schedule & quantities
 *
 * Exceptions:
 *  - IllegalArgumentException for invalid inputs
 *  - EntityNotFoundException when requested data does not exist
 *
 * Transactionality:
 *  - Read operations are marked readOnly where safe
 *  - Mutations participate in a single transactional boundary
 */
@Service
public class PricingService {

    private static final Logger log = LoggerFactory.getLogger(PricingService.class);

    private final PricingRepository pricingRepo;
    private final PricingTypeRepository typeRepo;
    private final ScheduleRepository scheduleRepository;

    public PricingService(PricingRepository pricingRepo,
                          PricingTypeRepository typeRepo,
                          ScheduleRepository scheduleRepository) {
        this.pricingRepo = pricingRepo;
        this.typeRepo = typeRepo;
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * Returns a page of pricing rows.
     *
     * @param pageable page request (must not be null)
     * @return page of Pricing
     */
    @Transactional(readOnly = true)
    public Page<Pricing> page(Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable must not be null");
        return pricingRepo.findAll(pageable);
    }

    /**
     * Loads a Pricing by id or throws if not found.
     *
     * @param id pricing id (must not be null)
     * @return Pricing
     * @throws EntityNotFoundException if missing
     */
    @Transactional(readOnly = true)
    public Pricing getOrThrow(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        return pricingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pricing not found: " + id));
    }

    /**
     * Creates a new pricing with initial ADULT/CHILD prices.
     *
     * @param req create request (must not be null)
     * @return generated id
     * @throws IllegalArgumentException for duplicate name or invalid values
     */
    @Transactional
    public Long create(PricingCreateRequest req) {
        Objects.requireNonNull(req, "request must not be null");
        if (req.name == null || req.name.isBlank()) {
            throw new IllegalArgumentException("Pricing name is required");
        }
        if (pricingRepo.existsByNameIgnoreCase(req.name)) {
            throw new IllegalArgumentException("Pricing name already exists");
        }

        String username = safeCurrentUsername();
        LocalDateTime now = LocalDateTime.now();

        Pricing p = new Pricing();
        p.setName(req.name.trim());
        p.setStatus(Objects.requireNonNull(req.getStatus(), "status is required"));
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        p.setCreatedBy(username);
        p.setUpdatedBy(username);

        Pricing saved = pricingRepo.save(p);

        upsertType(saved.getId(), SeatType.ADULT,
                req.prices == null ? BigDecimal.ZERO : req.prices.getOrDefault(SeatType.ADULT, BigDecimal.ZERO));
        upsertType(saved.getId(), SeatType.CHILD,
                req.prices == null ? BigDecimal.ZERO : req.prices.getOrDefault(SeatType.CHILD, BigDecimal.ZERO));

        log.info("Created pricing id={} name='{}' by {}", saved.getId(), saved.getName(), username);

        return saved.getId();
    }

    /**
     * Updates pricing core fields and seat-type prices.
     *
     * @param id  pricing id
     * @param req update request
     * @throws EntityNotFoundException if pricing does not exist
     * @throws IllegalArgumentException if input invalid
     */
    @Transactional
    public void update(Long id, PricingUpdateRequest req) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(req, "request must not be null");

        Pricing pricing = pricingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pricing not found: " + id));

        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Pricing name is required");
        }
        pricing.setName(req.getName().trim());
        pricing.setStatus(Objects.requireNonNull(req.getStatus(), "status is required"));
        pricing.setUpdatedAt(LocalDateTime.now());
        pricing.setUpdatedBy(safeCurrentUsername());
        pricingRepo.save(pricing);

        upsertType(id, SeatType.ADULT, req.prices == null ? BigDecimal.ZERO :
                req.prices.getOrDefault(SeatType.ADULT, BigDecimal.ZERO));
        upsertType(id, SeatType.CHILD, req.prices == null ? BigDecimal.ZERO :
                req.prices.getOrDefault(SeatType.CHILD, BigDecimal.ZERO));

        log.info("Updated pricing id={} name='{}'", id, pricing.getName());
    }

    /**
     * Updates a single seat-type price for a pricing row.
     *
     * @param pricingId pricing id
     * @param req       update request containing type and price
     * @throws EntityNotFoundException if pricing does not exist
     * @throws IllegalArgumentException if type/price invalid
     */
    @Transactional
    public void updateSingleType(Long pricingId, PricingTypeUpdateRequest req) {
        Objects.requireNonNull(req, "request must not be null");
        getOrThrow(pricingId); // verify exists
        if (req.type == null) {
            throw new IllegalArgumentException("Seat type is required");
        }
        upsertType(pricingId, req.type, req.price);
        log.info("Updated pricing type: pricingId={} type={} price={}", pricingId, req.type, req.price);
    }

    /**
     * Lists all PricingType rows for a pricing definition.
     *
     * @param pricingId pricing id
     * @return list of PricingType for the pricing
     */
    @Transactional(readOnly = true)
    public List<PricingType> listTypes(Long pricingId) {
        Objects.requireNonNull(pricingId, "pricingId must not be null");
        return typeRepo.findByPricingId(pricingId);
    }

    /**
     * Deletes a pricing definition (dependent PricingType rows are removed by FK cascade).
     *
     * @param pricingId pricing id
     */
    @Transactional
    public void delete(Long pricingId) {
        Objects.requireNonNull(pricingId, "pricingId must not be null");
        if (!pricingRepo.existsById(pricingId)) {
            throw new EntityNotFoundException("Pricing not found: " + pricingId);
        }
        pricingRepo.deleteById(pricingId);
        log.info("Deleted pricing id={}", pricingId);
    }

    /**
     * Computes the total amount for a schedule given adult/child quantities
     * based on the schedule's attached Pricing.
     *
     * @param scheduleId schedule id
     * @param adult      adult ticket count (>= 0)
     * @param child      child ticket count (>= 0)
     * @return total as BigDecimal (scale 2)
     */
    @Transactional(readOnly = true)
    public BigDecimal computeTotal(Long scheduleId, int adult, int child) {
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");
        if (adult < 0 || child < 0) {
            throw new IllegalArgumentException("Quantities must be non-negative");
        }

        Pricing pricing = scheduleRepository.findById(scheduleId)
                .map(Schedule::getPricing)
                .orElseThrow(() -> new EntityNotFoundException("Pricing not found for schedule: " + scheduleId));

        BigDecimal adultTotal = safePrice(pricing.getAdultPrice()).multiply(BigDecimal.valueOf(adult));
        BigDecimal childTotal = safePrice(pricing.getChildPrice()).multiply(BigDecimal.valueOf(child));
        return adultTotal.add(childTotal).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Transactional(readOnly = true)
    public BigDecimal computePrice(Long scheduleId, boolean isAdult) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + scheduleId));

        Pricing pricing = schedule.getPricing();
        if (pricing == null) {
            throw new RuntimeException("No pricing found for schedule: " + scheduleId);
        }

        return isAdult ? pricing.getAdultPrice() : pricing.getChildPrice();
    }

    private void upsertType(Long pricingId, SeatType type, BigDecimal price) {
        Objects.requireNonNull(pricingId, "pricingId must not be null");
        Objects.requireNonNull(type, "seat type must not be null");

        BigDecimal sanitized = sanitizePrice(price);

        Pricing pricing = pricingRepo.findById(pricingId)
                .orElseThrow(() -> new RuntimeException("Pricing not found with id: " + pricingId));

        PricingType row = typeRepo.findByPricingIdAndType(pricingId, type)
                .orElseGet(() -> {
                    PricingType t = new PricingType();
                    t.setPricing(pricing);
                    t.setType(type);
                    return t;
                });

        row.setPrice(sanitized);
        typeRepo.save(row);
    }

    private void setPriceForType(Long pricingId, SeatType type, BigDecimal price) {
        BigDecimal sanitized = sanitizePrice(price);

        PricingType row = typeRepo.findByPricingIdAndType(pricingId, type)
                .orElseGet(() -> {
                    PricingType t = new PricingType();
                    // Create a proxy Pricing entity with just the ID
                    Pricing pricing = new Pricing();
                    pricing.setId(pricingId);
                    t.setPricing(pricing);
                    t.setType(type);
                    return t;
                });

        row.setPrice(sanitized);
        typeRepo.save(row);
    }

    private BigDecimal sanitizePrice(BigDecimal price) {
        if (price == null) return BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP);
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        return price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal safePrice(BigDecimal price) {
        return price == null ? BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP)
                : price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private String safeCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && auth.getName() != null)
                ? auth.getName()
                : "system";
    }

}
