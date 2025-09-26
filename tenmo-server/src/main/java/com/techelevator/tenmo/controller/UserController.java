package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@PreAuthorize("isAuthenticated()")
public class UserController {
    private final UserDao userDao;

    public UserController(UserDao userDao){
        this.userDao = userDao;
    }

    @GetMapping
    public List<User> getAllUsers(){
        return userDao.getUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id){
        return  userDao.getUserById(id);
    }



}
