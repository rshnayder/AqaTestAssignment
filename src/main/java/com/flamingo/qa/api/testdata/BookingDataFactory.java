package com.flamingo.qa.api.testdata;

import com.flamingo.qa.api.model.Booking;
import com.flamingo.qa.api.model.BookingDates;

public final class BookingDataFactory {

    private BookingDataFactory() {
    }

    public static Booking validBooking() {
        String suffix = Long.toString(System.nanoTime());
        return Booking.builder()
                .firstname("QA" + suffix.substring(Math.max(0, suffix.length() - 5)))
                .lastname("Automation")
                .totalprice(350)
                .depositpaid(true)
                .bookingdates(BookingDates.builder()
                        .checkin("2026-11-10")
                        .checkout("2026-11-14")
                        .build())
                .additionalneeds("Breakfast")
                .build();
    }

    public static Booking updatedFrom(Booking original) {
        return original.toBuilder()
                .firstname(original.getFirstname() + "Updated")
                .totalprice(original.getTotalprice() + 75)
                .additionalneeds("Dinner")
                .build();
    }
}
