package com.myproject.inventorymanagement.service;

import com.myproject.inventorymanagement.entity.User;
import com.myproject.inventorymanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public List<User> getActiveUsers(){
        return userRepository.findByIsActiveTrue();
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User getUserByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public List<User> getUsersByRole(User.Role role){
        return userRepository.findByRole(role);
    }

    @Transactional
    public User createUser(User user){
        if(userRepository.existsByUsername(user.getUsername())){
            throw new RuntimeException("Username already taken: " + user.getUsername());
        }

        if(userRepository.existsByEmail(user.getEmail())){
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }

        //hash password//

        user.setIsActive(true);
        if(user.getRole() == null)  user.setRole(User.Role.STAFF);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User updated){
        User existing = getUserById(id);

        if(!existing.getUsername().equalsIgnoreCase(updated.getUsername()) && userRepository.existsByUsername(updated.getUsername())){
            throw new RuntimeException("Username already taken: " + updated.getUsername());
        }

        if(!existing.getEmail().equalsIgnoreCase(updated.getEmail()) && userRepository.existsByEmail(updated.getEmail())){
            throw new RuntimeException("Email already registered: "+ updated.getEmail());
        }

        existing.setUsername(updated.getUsername());
        existing.setEmail(updated.getEmail());
        existing.setRole(updated.getRole());
        return userRepository.save(existing);
    }

    @Transactional
    public void changePassword(Long id, String newPassword){
        User user = getUserById(id);
        //hash new pw//
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    @Transactional
    public  void deactivateUser(Long id){
        User user = getUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id){
        if(!userRepository.existsById(id)){
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public User login(String username, String password){
        User user = getUserByUsername(username);
        if(!user.getPassword().equals(password)){
            throw new RuntimeException("Wrong password or username!");
        }
        if(!user.getIsActive()){
            throw new RuntimeException("Your account has been deactivated!");
        }
        return user;
    }
}
