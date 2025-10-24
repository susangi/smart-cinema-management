package com.example.cinema_management.booking.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class StartView {
    Long scheduleId;
    Long movieId;
    String movieTitle;
    String screenName;
    String showTimeText;
    BigDecimal adultPrice;
    BigDecimal childPrice;
    BigDecimal initialTotal;
}
