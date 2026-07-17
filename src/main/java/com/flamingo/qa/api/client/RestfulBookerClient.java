package com.flamingo.qa.api.client;

import com.flamingo.qa.api.model.AuthRequest;
import com.flamingo.qa.api.model.AuthResponse;
import com.flamingo.qa.api.model.Booking;
import com.flamingo.qa.api.spec.ApiSpecifications;
import com.flamingo.qa.config.FrameworkConfig;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class RestfulBookerClient {

    private final RequestSpecification spec;
    private final Object authTokenLock = new Object();
    private volatile String authToken;

    public RestfulBookerClient() {
        this(FrameworkConfig.getRequired("restful.booker.base.url"));
    }

    public RestfulBookerClient(String baseUrl) {
        this.spec = ApiSpecifications.jsonSpec(baseUrl);
    }

    public Response authenticate(AuthRequest request) {
        stepAuthenticate(request.getUsername());
        return sendAuthRequest(request);
    }

    private Response sendAuthRequest(AuthRequest request) {
        return given()
                .spec(spec)
                .body(request)
                .post("/auth");
    }

    public Response createBooking(Booking booking) {
        stepCreateBooking(booking.getFirstname(), booking.getLastname());
        return given()
                .spec(spec)
                .body(booking)
                .post("/booking");
    }

    @Step("Get booking by id: {0}")
    public Response getBooking(int bookingId) {
        return given()
                .spec(spec)
                .get("/booking/{bookingId}", bookingId);
    }

    public Response updateBooking(int bookingId, Booking booking) {
        stepUpdateBooking(bookingId, booking.getFirstname(), booking.getLastname());
        return given()
                .spec(spec)
                .cookie("token", authToken())
                .body(booking)
                .put("/booking/{bookingId}", bookingId);
    }

    public Response deleteBooking(int bookingId) {
        stepDeleteBooking(bookingId);
        return deleteBookingWithToken(bookingId, authToken());
    }

    public Response deleteBookingWithInvalidToken(int bookingId) {
        stepDeleteBookingWithInvalidToken(bookingId);
        return deleteBookingWithToken(bookingId, "invalid-token");
    }

    private Response deleteBookingWithToken(int bookingId, String token) {
        return given()
                .spec(spec)
                .cookie("token", token)
                .delete("/booking/{bookingId}", bookingId);
    }

    private String authToken() {
        String cached = authToken;
        if (hasText(cached)) {
            return cached;
        }

        synchronized (authTokenLock) {
            cached = authToken;
            if (hasText(cached)) {
                return cached;
            }
            authToken = requestAuthToken();
            return authToken;
        }
    }

    @Step("Create Restful Booker auth token")
    private String requestAuthToken() {
        AuthRequest request = AuthRequest.builder()
                .username(FrameworkConfig.getRequired("restful.booker.username"))
                .password(FrameworkConfig.getRequired("restful.booker.password"))
                .build();

        Response response = sendAuthRequest(request);
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Unable to create Restful Booker auth token. Status: "
                    + response.statusCode());
        }

        String token = response.as(AuthResponse.class).getToken();
        if (!hasText(token)) {
            throw new IllegalStateException("Restful Booker auth response did not contain token");
        }
        return token;
    }

    @Step("Authenticate Restful Booker user: username={0}")
    private void stepAuthenticate(String username) {
    }

    @Step("Create booking for guest: {0} {1}")
    private void stepCreateBooking(String firstName, String lastName) {
    }

    @Step("Update booking id={0} for guest: {1} {2}")
    private void stepUpdateBooking(int bookingId, String firstName, String lastName) {
    }

    @Step("Delete booking id={0}")
    private void stepDeleteBooking(int bookingId) {
    }

    @Step("Try to delete booking id={0} with invalid token")
    private void stepDeleteBookingWithInvalidToken(int bookingId) {
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
