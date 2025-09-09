package ams.mn.ubtz.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ams.mn.ubtz.user.repository.UsersRepository;
import ams.mn.ubtz.user.model.Users;

import java.util.List;

@Service
public class UsersService {
    @Autowired
    private UsersRepository userRepository;

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

   /* public void addUser(Users user) {
        userRepository.save(user);
    }*/
}