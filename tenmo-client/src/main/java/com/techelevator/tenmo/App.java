package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferServices;

import org.springframework.http.converter.json.GsonBuilderUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    private final TransferServices transferServices = new TransferServices(API_BASE_URL);

    private String statusDescription(int statusId){
        switch(statusId){
            case 1: return "Pending";
            case 2: return "Approve";
            case 3: return "Rejected";
        }
        return null;
    }

    private String typeDescription(int typeId){
        switch(typeId){
            case 1: return "Request";
            case 2: return "Send";
            case 3: return "Unknow";
        }
        return null;

    }


    ;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {

        BigDecimal balance = transferServices.getBalance(currentUser);
        if (balance != null) {
            System.out.println("Your current balance is: $" + balance);
        } else {
            System.out.println("Your have $0");
        }

    }


    private String findUsernameById(User[] users, int userId) {
        for (User u : users) {
            if (u.getId() == userId) {
                return u.getUsername();
            }
        }
        return "";
    }

    private void viewTransferHistory() {
        Transfer[] transfers = transferServices.getTransferHistory(currentUser);
        int currentUserId = currentUser.getUser().getId();


        System.out.println("-----------------------------------");
        System.out.println("Transfers");
        System.out.println("ID            From/To        Amount");
        System.out.println("-----------------------------------");


        for (Transfer t : transfers) {
            String from = "From: " + t.getFromUsername();
            String to = "To: " + t.getToUsername();

            String line = t.getTransferId() + "    " + from + " " + to +  "  " + " $ " + t.getTransferAmount();
            System.out.println(line);
        }

        System.out.println("-----------------------------------");
        int transferId = consoleService.promptForInt("Please enter transfer ID to view detail (0 to cancel): ");

        if(transferId == 0) {
            System.out.println("Cancelled.");
            return;
        }

        Transfer transfer = transferServices.getTransferById(currentUser, transferId);

        if (transfer == null) {
            System.out.println("Transfer not found.");
            return;
        }

        System.out.println("_________________________________________");
        System.out.println("Transfer Details");
        System.out.println("_________________________________________");
        System.out.println("ID: " + transfer.getTransferId());
        System.out.println("From: " + transfer.getFromUsername());
        System.out.println("To: " + transfer.getToUsername());
        System.out.println("TransferType: " + typeDescription(transfer.getTransferTypeId()));
        System.out.println("TransferStatus: " + statusDescription(transfer.getTransferStatusId()));
        System.out.println("Amount: $" + transfer.getTransferAmount());
        System.out.println("_________________________________________");

    }

        private void viewPendingRequests () {

        Transfer[] pendingRequests = transferServices.getPendingRequests(currentUser);
            //User[] users = transferServices.getAllUsers(currentUser);
            System.out.println("PendingID       TransferTo          Amount");
            System.out.println("------------------------------------------");

            for (Transfer t : pendingRequests) {
                System.out.println(t.getTransferId()
                        + "             "
                        + t.getToUsername()
                        + "              "
                        + "$" + t.getTransferAmount());
            }

            System.out.println("------------------------------------------");
            int choice = consoleService.promptForInt("Please enter transfer ID to approve/reject,(0) to cancel");

            if (choice == 0) {
                System.out.println("Cancelled.");
                return;
            }

            System.out.println("1:Approve");
            System.out.println("2.Reject");

            int action = consoleService.promptForInt("Choose action: ");

            if (action == 1) {
                transferServices.approveTransfer(currentUser, choice);
                System.out.println("Transfer approve.");
                viewCurrentBalance();

            } else if (action == 2) {
                transferServices.rejectTransfer(currentUser, choice);
                System.out.println("Transfer rejected.");
                viewCurrentBalance();

            } else {
                System.out.println("Invalid choice");
            }
        }


        private void sendBucks () {
            User[] users = transferServices.getAllUsers(currentUser);
            System.out.println("-----------------------------------");
            System.out.println("Users");
            System.out.println("ID                             Name");
            System.out.println("-----------------------------------");

            for (User u :users){
                if (u.getId() != currentUser.getUser().getId()){
                    System.out.println(u.getId() + "                           " + u.getUsername());
                }
            }

            System.out.println("-----------------------------------");

            int toUserId = consoleService.promptForInt("Enter ID of user you want to sending TEBucks (0 to cancel)");
            if(toUserId == 0){
                System.out.println("Cancelled.");
                return;
            }

            BigDecimal amount = consoleService.promptForBigDecimal("Please Enter the amount you want to send: ");

            SendTransferRequest request = new SendTransferRequest();
            request.setToUserId(toUserId);
            request.setAmount(amount);

            String result = transferServices.sendBucks(currentUser,request);
            System.out.println(result);

        }

        private void requestBucks () {
            User[] users = transferServices.getAllUsers(currentUser);
            System.out.println("-----------------------------------");
            System.out.println("Users");
            System.out.println("ID                             Name");
            System.out.println("-----------------------------------");

            for (User u :users){
                if (u.getId() != currentUser.getUser().getId()){
                    System.out.println(u.getId() + "                           " + u.getUsername());
                }
            }

            System.out.println("-----------------------------------");

            int toUserId = consoleService.promptForInt("Enter ID of user you want to requesting TEBucks (0 to cancel)");
            if(toUserId == 0){
                System.out.println("Cancelled.");
                return;
            }

            BigDecimal amount = consoleService.promptForBigDecimal("Please Enter the amount you want to request: ");

            SendTransferRequest request = new SendTransferRequest();
            request.setToUserId(toUserId);
            request.setAmount(amount);

            String result = transferServices.requestBucks(currentUser,request);
            System.out.println(result);

        }
    }









