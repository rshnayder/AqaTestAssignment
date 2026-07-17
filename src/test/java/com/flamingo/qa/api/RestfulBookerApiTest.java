package com.flamingo.qa.api;

import com.flamingo.qa.api.client.RestfulBookerClient;
import com.flamingo.qa.api.model.AuthRequest;
import com.flamingo.qa.api.model.Booking;
import com.flamingo.qa.api.model.CreateBookingResponse;
import com.flamingo.qa.api.testdata.BookingDataFactory;
import com.flamingo.qa.config.FrameworkConfig;
import com.flamingo.qa.support.RetryTestExtension;
import com.flamingo.qa.support.TestLoggingExtension;
import com.flamingo.qa.support.data.JsonFileSource;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Tag("api")
@ExtendWith({TestLoggingExtension.class, RetryTestExtension.class})
@Execution(ExecutionMode.SAME_THREAD)
class RestfulBookerApiTest {

    private final RestfulBookerClient client = new RestfulBookerClient();
    private final List<Integer> createdBookingIds = new ArrayList<>();

    @AfterEach
    void cleanupCreatedBookings() {
        List<Integer> bookingIds = new ArrayList<>(createdBookingIds);
        createdBookingIds.clear();
        Collections.reverse(bookingIds);
        bookingIds.forEach(this::cleanup);
    }

    @Test
    void shouldAuthenticateAdminUser() {
        AuthRequest request = AuthRequest.builder()
                .username(FrameworkConfig.getRequired("restful.booker.username"))
                .password(FrameworkConfig.getRequired("restful.booker.password"))
                .build();

        Response response = client.authenticate(request);

        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(response.jsonPath().getString("token")).isNotBlank();
        });
    }

    @ParameterizedTest(name = "should create booking from data set [{index}]")
    @JsonFileSource(value = "test-data/bookings.json", type = Booking.class)
    void shouldCreateBookingFromDataSet(Booking booking) {
        CreateBookingResponse created = createBookingResponse(booking);

        assertSoftly(softly -> {
            softly.assertThat(created.getBookingid()).isPositive();
            softly.assertThat(created.getBooking()).usingRecursiveComparison().isEqualTo(booking);
        });
    }

    @Test
    void shouldRetrieveCreatedBookingById() {
        Booking booking = BookingDataFactory.validBooking();
        int bookingId = createBooking(booking);

        Response response = client.getBooking(bookingId);

        assertThat(response.statusCode()).isEqualTo(200);
        assertSoftly(softly ->
                softly.assertThat(response.as(Booking.class)).usingRecursiveComparison().isEqualTo(booking));
    }

    @Test
    void shouldUpdateCreatedBooking() {
        Booking original = BookingDataFactory.validBooking();
        Booking updated = BookingDataFactory.updatedFrom(original);
        int bookingId = createBooking(original);

        Response response = client.updateBooking(bookingId, updated);

        assertThat(response.statusCode()).isEqualTo(200);
        assertSoftly(softly ->
                softly.assertThat(response.as(Booking.class)).usingRecursiveComparison().isEqualTo(updated));
    }

    @Test
    void shouldDeleteCreatedBooking() {
        int bookingId = createBooking(BookingDataFactory.validBooking());

        Response deleteResponse = client.deleteBooking(bookingId);
        Response getResponse = client.getBooking(bookingId);

        assertSoftly(softly -> {
            softly.assertThat(deleteResponse.statusCode()).isEqualTo(201);
            softly.assertThat(getResponse.statusCode()).isEqualTo(404);
        });
        createdBookingIds.remove(Integer.valueOf(bookingId));
    }

    @Test
    void shouldRejectDeleteWhenTokenIsInvalid() {
        int bookingId = createBooking(BookingDataFactory.validBooking());

        Response response = client.deleteBookingWithInvalidToken(bookingId);

        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(403);
            softly.assertThat(response.asString()).containsIgnoringCase("Forbidden");
        });
    }

    @Step("Create booking test precondition")
    private int createBooking(Booking booking) {
        return createBookingResponse(booking).getBookingid();
    }

    @Step("Create booking and track cleanup")
    private CreateBookingResponse createBookingResponse(Booking booking) {
        Response response = client.createBooking(booking);
        assertThat(response.statusCode()).isEqualTo(200);
        CreateBookingResponse created = response.as(CreateBookingResponse.class);
        trackForCleanup(created.getBookingid());
        return created;
    }

    @Step("Track booking id={0} for cleanup")
    private void trackForCleanup(Integer bookingId) {
        if (bookingId != null) {
            createdBookingIds.add(bookingId);
        }
    }

    @Step("Cleanup booking id={0}")
    private void cleanup(Integer bookingId) {
        if (bookingId != null) {
            client.deleteBooking(bookingId);
        }
    }
}
