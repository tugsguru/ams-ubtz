package ams.mn.ubtz.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ams.mn.ubtz.user.service.UsersService;
import ams.mn.ubtz.user.model.Users;


import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UsersService usersService;

    @GetMapping("/list")
    public List<Users> getAll() {
        return usersService.getAllUsers();
    }

    /*@PostMapping
    public void add(@RequestBody User user) {
        usersService.addUser(user);
    }*/
}