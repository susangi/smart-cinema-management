package com.example.cinema_management.booking.dto;

import java.math.BigDecimal;

public class BookingStartVM {
    private final Long scheduleId;
    private final Long movieId;
    private final String movieTitle;
    private final String screenName;
    private final String showTimeText;
    private final BigDecimal adultPrice;
    private final BigDecimal childPrice;

    public BookingStartVM(Long scheduleId, Long movieId, String movieTitle, String screenName,
                          String showTimeText, BigDecimal adultPrice, BigDecimal childPrice) {
        this.scheduleId = scheduleId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.screenName = screenName;
        this.showTimeText = showTimeText;
        this.adultPrice = adultPrice;
        this.childPrice = childPrice;
    }

    public Long getScheduleId() { return scheduleId; }
    public Long getMovieId() { return movieId; }
    public String getMovieTitle() { return movieTitle; }
    public String getScreenName() { return screenName; }
    public String getShowTimeText() { return showTimeText; }
    public BigDecimal getAdultPrice() { return adultPrice; }
    public BigDecimal getChildPrice() { return childPrice; }
}