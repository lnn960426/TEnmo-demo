package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.util.BasicLogger;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

public class AuthenticationService {

    private final String baseUrl;
    private final RestClient restClient = RestClient.create();

    public AuthenticationService(String url) {
        this.baseUrl = url;
    }

    public AuthenticatedUser login(UserCredentials credentials) {

        AuthenticatedUser user = null;
        try {
            user = restClient.post()
                    .uri(baseUrl + "login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(credentials)
                    .retrieve()
                    .body(AuthenticatedUser.class);

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return user;
    }

    public boolean register(UserCredentials credentials) {

        boolean success = false;
        try {
            restClient.post()
                    .uri(baseUrl + "register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(credentials)
                    .retrieve()
                    .toBodilessEntity();

            success = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;
    }

}
