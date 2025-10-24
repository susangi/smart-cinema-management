package com.example.cinema_management.pricing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PricingTypeRepository extends JpaRepository<PricingType, Long> {
    List<PricingType> findByPricingId(Long pricingId);
    Optional<PricingType> findByPricingIdAndType(Long pricingId, SeatType type);
    void deleteByPricingId(Long pricingId);

}
