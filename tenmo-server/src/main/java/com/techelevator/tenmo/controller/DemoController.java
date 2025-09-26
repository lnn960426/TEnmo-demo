package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.LoginDto;
import com.techelevator.tenmo.model.LoginResponseDto;
import com.techelevator.tenmo.model.RegisterUserDto;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.security.jwt.TokenProvider;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Minimal endpoints to mirror your CLI flow.
 */
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

    // ---- health ----
    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    // ---- auth ----
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password are required"));
        }
        User created = userDao.createUser(dto);
        if (created != null) created.setPassword(null); // never return password hash
        return ResponseEntity.ok(Map.of("status", "registered", "user", created));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        String token = tokenProvider.createToken(auth, false);
        User user = userDao.getUserByUsername(loginDto.getUsername());
        if (user != null) user.setPassword(null);
        return ResponseEntity.ok(new LoginResponseDto(token, user));
    }

    // ---- helpers ----
    /** Extract userId from Authorization: Bearer <jwt>. */
    private Integer currentUserId(String authz) {
        if (!StringUtils.hasText(authz) || !authz.startsWith("Bearer ")) return null;
        String token = authz.substring(7);
        if (!tokenProvider.validateToken(token)) return null;
        Authentication auth = tokenProvider.getAuthentication(token);
        User u = userDao.getUserByUsername(auth.getName());
        return (u != null) ? u.getId() : null;
    }

    // ---- users ----
    @GetMapping("/users")
    public ResponseEntity<?> users(@RequestHeader(value = "Authorization", required = false) String authz) {
        Integer me = currentUserId(authz);
        if (me == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        List<User> all = userDao.getUsers();
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : all) {
            if (u.getId() == me) continue;
            list.add(Map.of("id", u.getId(), "username", u.getUsername()));
        }
        return ResponseEntity.ok(list);
    }

    // ---- balance ----
    @GetMapping("/balance")
    public ResponseEntity<?> balance(@RequestHeader(value = "Authorization", required = false) String authz) {
        Integer userId = currentUserId(authz);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        BigDecimal balance = accountDao.getBalanceByUserId(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "balance", balance));
    }

    // ---- transfers: list/history ----
    @GetMapping("/transfers")
    public ResponseEntity<?> history(@RequestHeader(value = "Authorization", required = false) String authz) {
        Integer userId = currentUserId(authz);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        List<Transfer> list = transferDao.getTransferbyUserId(userId);
        return ResponseEntity.ok(list);
    }

    // ---- transfers: detail ----
    @GetMapping("/transfers/{id}")
    public ResponseEntity<?> transferDetail(@RequestHeader(value = "Authorization", required = false) String authz,
                                            @PathVariable int id) {
        Integer userId = currentUserId(authz);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        Transfer t = transferDao.getTransferById(id);
        return ResponseEntity.ok(t);
    }

    // ---- pending requests ----
    @GetMapping("/transfers/pending")
    public ResponseEntity<?> pending(@RequestHeader(value = "Authorization", required = false) String authz) {
        Integer userId = currentUserId(authz);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        List<Transfer> list = transferDao.getPendingRequests(userId);
        return ResponseEntity.ok(list);
    }

    // ---- approve / reject ----
    @PostMapping("/transfers/{id}/approve")
    public ResponseEntity<?> approve(@RequestHeader(value = "Authorization", required = false) String authz,
                                     @PathVariable int id) {
        Integer userId = currentUserId(authz);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        boolean ok = transferDao.updateTransferStatus(id, 2);
        return ok ? ResponseEntity.ok(Map.of("status", "approved", "transferId", id))
                : ResponseEntity.badRequest().body(Map.of("error", "update failed"));
    }

    @PostMapping("/transfers/{id}/reject")
    public ResponseEntity<?> reject(@RequestHeader(value = "Authorization", required = false) String authz,
                                    @PathVariable int id) {
        Integer userId = currentUserId(authz);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        boolean ok = transferDao.updateTransferStatus(id, 3);
        return ok ? ResponseEntity.ok(Map.of("status", "rejected", "transferId", id))
                : ResponseEntity.badRequest().body(Map.of("error", "update failed"));
    }

    // ---- send / request ----
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestHeader(value = "Authorization", required = false) String authz,
                                  @RequestBody SendOrRequestDto body) {
        Integer fromUserId = currentUserId(authz);
        if (fromUserId == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        if (body.getToUserId() == null || !StringUtils.hasText(body.getAmount()))
            return ResponseEntity.badRequest().body(Map.of("error", "toUserId and amount are required"));
        BigDecimal amt;
        try { amt = new BigDecimal(body.getAmount()); } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid amount"));
        }
        boolean ok = transferDao.createSendTransfer(fromUserId, body.getToUserId(), amt);
        return ok ? ResponseEntity.ok(Map.of("status", "ok", "amount", amt))
                : ResponseEntity.badRequest().body(Map.of("error", "transfer failed"));
    }

    @PostMapping("/request")
    public ResponseEntity<?> request(@RequestHeader(value = "Authorization", required = false) String authz,
                                     @RequestBody SendOrRequestDto body) {
        Integer fromUserId = currentUserId(authz);
        if (fromUserId == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        if (body.getToUserId() == null || !StringUtils.hasText(body.getAmount()))
            return ResponseEntity.badRequest().body(Map.of("error", "toUserId and amount are required"));
        BigDecimal amt;
        try { amt = new BigDecimal(body.getAmount()); } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid amount"));
        }
        boolean ok = transferDao.createRequestTransfer(fromUserId, body.getToUserId(), amt);
        return ok ? ResponseEntity.ok(Map.of("status", "ok", "amount", amt))
                : ResponseEntity.badRequest().body(Map.of("error", "request failed"));
    }

    /** DTO used by /send and /request */
    public static class SendOrRequestDto {
        private Integer toUserId;
        private String amount;
        public SendOrRequestDto() {}
        public Integer getToUserId() { return toUserId; }
        public void setToUserId(Integer toUserId) { this.toUserId = toUserId; }
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
    }
}
