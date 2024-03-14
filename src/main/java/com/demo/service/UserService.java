package com.demo.service;

import com.demo.entities.User;
import com.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private UserRepository userRepository;
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User createNewUser(User users){
        return userRepository.save(users);
    }

    public List<User> fetchAll(){
        return userRepository.findAll();
    }
}
