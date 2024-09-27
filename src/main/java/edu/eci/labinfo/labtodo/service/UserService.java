package edu.eci.labinfo.labtodo.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.eci.labinfo.labtodo.data.UserRepository;
import edu.eci.labinfo.labtodo.model.User;
import edu.eci.labinfo.labtodo.model.LabToDoExeption;
import java.time.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User addUser(User user) throws LabToDoExeption {
        if (!userRepository.existsByUserName(user.getUserName())) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            user.setCreationDate(LocalDateTime.now());
            user.setUpdateDate(LocalDateTime.now());
            user.setLastLoginDate(LocalDateTime.now());
            user.setConnect(false);
        } else {
            throw new LabToDoExeption(LabToDoExeption.USER_NAME_EXISTS);
        }

        return userRepository.save(user);
    }

    public User getUserByUserName(String username) {
        if (userRepository.findByUserName(username).isPresent()) {
            return userRepository.findByUserName(username).get();
        }
        return null;
    }

    public User getUserByFullName(String fullName) {
        if (userRepository.findByFullName(fullName).isPresent()) {
            return userRepository.findByFullName(fullName).get();
        }
        return null;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public User updateUser(User user) {
        if (userRepository.existsById(user.getUserId())) {

            return userRepository.save(user);
        }
        return null;
    }

    public void deleteUser(String userName) {
        userRepository.delete(getUserByUserName(userName));
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

}
