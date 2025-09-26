package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;


@RestController
//@RequestMapping("/accounts")
@PreAuthorize("isAuthenticated()")
public class AccountController {
    private final AccountDao accountDao;
    private final UserDao userDao;

    public AccountController (AccountDao accountDao, UserDao userDao){
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    @GetMapping("/balance")
    public BigDecimal getBalance(Principal principal){
        String username = principal.getName();
        User user = userDao.getUserByUsername(username);
        return accountDao.getBalanceByUserId(user.getId());


    }


}
