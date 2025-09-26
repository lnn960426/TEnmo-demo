package com.techelevator.tenmo.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

/**
 * This is a thin demo wrapper:
 * - /demo/login -> call your existing auth to get a JWT
 * - /demo/balance -> read current user from JWT and return balance
 * - /demo/send -> transfer from current user to a target username
 *
 * Replace the TODOs with your real services (Auth/Account/Transfer).
 */
@RestController
@RequestMapping("/demo")
@CrossOrigin(origins = "*")
public class DemoController {

    // TODO: inject your real services/components:
    // private final AuthService authService;
    // private final AccountService accountService;
    // private final TransferService transferService;

    public DemoController(/*AuthService authService, AccountService accountService, TransferService transferService*/) {
        // this.authService = authService;
        // this.accountService = accountService;
        // this.transferService = transferService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");

        // TODO: call your real login and return its JWT
        // String token = authService.login(username, password);
        String token = "REPLACE_WITH_REAL_JWT"; // placeholder so the page works first

        return ResponseEntity.ok(Map.of("username", username, "token", token));
    }

    @GetMapping("/balance")
    public ResponseEntity<?> balance(@RequestHeader(value = "Authorization", required = false) String authz) {
        // TODO: parse "Authorization: Bearer <jwt>" -> userId
        // long userId = authService.parseUserId(authz);
        // BigDecimal bal = accountService.getBalanceByUserId(userId);

        BigDecimal bal = new BigDecimal("1000.00"); // placeholder
        return ResponseEntity.ok(Map.of("balance", bal));
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestHeader(value = "Authorization", required = false) String authz,
                                  @RequestBody Map<String, String> body) {
        String toUser = body.getOrDefault("toUser", "");
        BigDecimal amount = new BigDecimal(body.getOrDefault("amount", "0"));

        // TODO:
        // long fromUserId = authService.parseUserId(authz);
        // long toUserId   = accountService.findUserIdByUsername(toUser);
        // long transferId = transferService.send(fromUserId, toUserId, amount);

        long transferId = 999L; // placeholder
        return ResponseEntity.ok(Map.of("status","ok", "transferId", transferId, "toUser", toUser, "amount", amount));
    }
}
