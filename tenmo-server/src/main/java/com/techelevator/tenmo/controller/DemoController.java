package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.LoginDto;
import com.techelevator.tenmo.model.LoginResponseDto;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.security.jwt.TokenProvider;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/demo")
@CrossOrigin(origins = "*")
public class DemoController {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final UserDao userDao;
    private final AccountDao accountDao;
    private final TransferDao transferDao;

    public DemoController(AuthenticationManager authenticationManager,
                          TokenProvider tokenProvider,
                          UserDao userDao,
                          AccountDao accountDao,
                          TransferDao transferDao) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userDao = userDao;
        this.accountDao = accountDao;
        this.transferDao = transferDao;
    }

    // ---- health check for Render ----
    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    // ---- 1) login: username/password -> JWT ----
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto) {
        // Authenticate
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );

        // Create JWT
        String token = tokenProvider.createToken(auth, false);

        // Load user and sanitize password field before returning
        User user = userDao.getUserByUsername(loginDto.getUsername());
        if (user != null) {
            user.setPassword(null); // do not leak password hash
        }

        // LoginResponseDto(String token, User user)
        return ResponseEntity.ok(new LoginResponseDto(token, user));
    }

    // ---- 2) balance: read from JWT subject ----
    @GetMapping("/balance")
    public ResponseEntity<?> balance(@RequestHeader(value = "Authorization", required = false) String authz) {
        Integer userId = resolveUserIdFromAuthHeader(authz);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid token"));
        }
        BigDecimal balance = accountDao.getBalanceByUserId(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "balance", balance));
    }

    // ---- 3) send: current user -> toUsername ----
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestHeader(value = "Authorization", required = false) String authz,
                                  @RequestBody Map<String, String> body) {
        Integer fromUserId = resolveUserIdFromAuthHeader(authz);
        if (fromUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid token"));
        }

        String toUsername = body.getOrDefault("toUser", "").trim();
        String amtStr = body.getOrDefault("amount", "0").trim();
        if (!StringUtils.hasText(toUsername)) {
            return ResponseEntity.badRequest().body(Map.of("error", "toUser is required"));
        }

        BigDecimal amount;
        try { amount = new BigDecimal(amtStr); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", "amount is invalid")); }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "amount must be > 0"));
        }

        User toUser = userDao.getUserByUsername(toUsername);
        if (toUser == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "recipient not found"));
        }
        if (fromUserId.equals(toUser.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "cannot send to yourself"));
        }

        boolean ok = transferDao.createSendTransfer(fromUserId, toUser.getId(), amount);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("error", "transfer failed"));
        }

        BigDecimal fromBal = accountDao.getBalanceByUserId(fromUserId);
        BigDecimal toBal   = accountDao.getBalanceByUserId(toUser.getId());
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "fromUserId", fromUserId,
                "toUser", toUsername,
                "amount", amount,
                "fromBalance", fromBal,
                "toBalance", toBal
        ));
    }

    /** Extract userId from Authorization: Bearer <jwt>. */
    private Integer resolveUserIdFromAuthHeader(String authz) {
        if (!StringUtils.hasText(authz) || !authz.startsWith("Bearer ")) return null;
        String token = authz.substring(7);
        if (!tokenProvider.validateToken(token)) return null;
        Authentication auth = tokenProvider.getAuthentication(token);
        String username = auth.getName();
        User user = userDao.getUserByUsername(username);
        return user != null ? user.getId() : null;
    }
}
