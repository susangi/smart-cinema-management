package com.example.cinema_management.pricing;

import com.example.cinema_management.pricing.dto.PricingCreateRequest;
import com.example.cinema_management.pricing.dto.PricingTypeUpdateRequest;
import com.example.cinema_management.pricing.dto.PricingUpdateRequest;
import com.example.cinema_management.schedule.entity.Schedule;
import com.example.cinema_management.schedule.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PricingService {

    private final PricingRepository pricingRepo;
    private final PricingTypeRepository typeRepo;
    private final ScheduleRepository scheduleRepository;

    public PricingService(PricingRepository pricingRepo, PricingTypeRepository typeRepo, ScheduleRepository scheduleRepository) {
        this.pricingRepo = pricingRepo;
        this.typeRepo = typeRepo;
        this.scheduleRepository = scheduleRepository;
    }

    public Page<Pricing> page(Pageable pageable) {
        return pricingRepo.findAll(pageable);
    }

    public Pricing getOrThrow(Long id) {
        return pricingRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Pricing not found: " + id));
    }

    @Transactional
    public Long create(PricingCreateRequest req) {
        if (pricingRepo.existsByNameIgnoreCase(req.name)) {
            throw new IllegalArgumentException("Pricing name already exists");
        }

        String username = currentUsername();
        LocalDateTime now = LocalDateTime.now();

        Pricing p = new Pricing();
        p.setName(req.name.trim());
        p.setStatus(req.getStatus());
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        p.setCreatedBy(username);
        p.setUpdatedBy(username);

        Pricing saved = pricingRepo.save(p);

        upsertType(saved.getId(), SeatType.ADULT,
                req.prices.getOrDefault(SeatType.ADULT, BigDecimal.ZERO));
        upsertType(saved.getId(), SeatType.CHILD,
                req.prices.getOrDefault(SeatType.CHILD, BigDecimal.ZERO));

        return saved.getId();
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
    }

    @Transactional
    public void update(Long id, PricingUpdateRequest req) {
        Pricing pricing = pricingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pricing not found: " + id));

        pricing.setName(req.getName());
        pricing.setStatus(req.getStatus());
        pricingRepo.save(pricing);

        upsertType(id, SeatType.ADULT, req.prices.getOrDefault(SeatType.ADULT, BigDecimal.ZERO));
        upsertType(id, SeatType.CHILD, req.prices.getOrDefault(SeatType.CHILD, BigDecimal.ZERO));

    }

    @Transactional
    public void updateSingleType(Long pricingId, PricingTypeUpdateRequest req) {
        // verifies pricing exists
        getOrThrow(pricingId);
        upsertType(pricingId, req.type, req.price);
    }

    public List<PricingType> listTypes(Long pricingId) {
        return typeRepo.findByPricingId(pricingId);
    }

    @Transactional
    public void delete(Long pricingId) {
        // types will cascade because of FK ON DELETE CASCADE
        pricingRepo.deleteById(pricingId);
    }

    private void upsertType(Long pricingId, SeatType type, BigDecimal price) {
        PricingType row = typeRepo.findByPricingIdAndType(pricingId, type)
                .orElseGet(() -> {
                    PricingType t = new PricingType();
                    t.setPricingId(pricingId);
                    t.setType(type);
                    return t;
                });

        row.setPrice(price);
        typeRepo.save(row);
    }

    public BigDecimal computeTotal(Long scheduleId, int adult, int child) {
        Pricing pricing = scheduleRepository.findById(scheduleId)
                .map(Schedule::getPricing)
                .orElseThrow(() -> new IllegalArgumentException("Pricing not found"));

        BigDecimal adultTotal = pricing.getAdultPrice().multiply(BigDecimal.valueOf(adult));
        BigDecimal childTotal = pricing.getChildPrice().multiply(BigDecimal.valueOf(child));
        return adultTotal.add(childTotal);
    }
}
