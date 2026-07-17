package com.flamingo.qa.api.model;

import lombok.Data;

@Data
public class CreateBookingResponse {
    private Integer bookingid;
    private Booking booking;
}
