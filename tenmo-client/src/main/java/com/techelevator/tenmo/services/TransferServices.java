package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.SendTransferRequest;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;

public class TransferServices {

    public static final String API_BASE_URL = "http://localhost:8080/transfers";
    private final RestClient restClient = RestClient.create(API_BASE_URL);

    private final String baseUrl;

    private String authToken = null;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public TransferServices (String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public User[] getAllUsers(AuthenticatedUser user) {
        try {
            return  restClient.get()
                .uri(baseUrl + "users")
                .header("Authorization", "Bearer " + user.getToken())
                .retrieve()
                .body(User[].class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            return new User[0];
        }

    }



    // Get account balance
    public BigDecimal getBalance(AuthenticatedUser user) {
        try {
            return restClient.get()
                    .uri(baseUrl + "balance")
                    .header("Authorization", "Bearer " + user.getToken())
                    .retrieve()
                    .body(BigDecimal.class);

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            return BigDecimal.ZERO;
        }

    }

    public  Transfer[] getTransferHistory(AuthenticatedUser user){
        try {
            return restClient.get()
                    .uri(baseUrl + "transfers")
                    .header("Authorization", "Bearer " + user.getToken())
                    .retrieve()
                    .body(Transfer[].class);

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            return new Transfer[0];
        }
    }


    public Transfer getTransferById(AuthenticatedUser user, int transferId){


        try {
            return restClient.get()
                    .uri(baseUrl + "transfers/" + transferId)
                    .header("Authorization", "Bearer " + user.getToken())
                    .retrieve()
                    .body(Transfer.class);

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            return null;
        }

    }

    public Transfer [] getPendingRequests (AuthenticatedUser user){
        try {
            return restClient.get()
                    .uri(baseUrl + "transfers" + "/pending")
                    .header("Authorization", "Bearer " + user.getToken())
                    .retrieve()
                    .body(Transfer[].class);

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            return new Transfer[0];
        }
    }

    public void approveTransfer (AuthenticatedUser user, int transferId){
        try {
            restClient.post()
                    .uri(baseUrl + "transfers/pending/" + transferId + "/approve")
                    .header("Authorization", "Bearer " + user.getToken())
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());

        }
    }

    public void rejectTransfer (AuthenticatedUser user, int transferId){
        try {
            restClient.post()
                    .uri(baseUrl + "transfers/pending/" + transferId + "/reject")
                    .header("Authorization", "Bearer " + user.getToken())
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());

        }
    }

    public String sendBucks (AuthenticatedUser user, SendTransferRequest request){
        try {
            return restClient.post()
                    .uri(baseUrl + "transfers" + "/send")
                    .header("Authorization", "Bearer " + user.getToken())
                    .body(request)
                    .retrieve()
                    .body(String.class);

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            return "Fail to send Bucks";
        }

    }
    public String requestBucks(AuthenticatedUser user, SendTransferRequest request){
        try {
            return restClient.post()
                    .uri(baseUrl + "transfers" + "/request")
                    .header("Authorization", "Bearer" + " " + user.getToken())
                    .body(request)
                    .retrieve()
                    .body(String.class);

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            return"Fail to request Bucks";
        }


    }

}
