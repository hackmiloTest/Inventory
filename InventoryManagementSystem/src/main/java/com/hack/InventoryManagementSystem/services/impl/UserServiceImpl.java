package com.hack.InventoryManagementSystem.services.impl;

import com.hack.InventoryManagementSystem.dto.LoginRequest;
import com.hack.InventoryManagementSystem.dto.RegisterRequest;
import com.hack.InventoryManagementSystem.dto.Response;
import com.hack.InventoryManagementSystem.dto.UserDTO;
import com.hack.InventoryManagementSystem.entity.User;
import com.hack.InventoryManagementSystem.enums.UserRole;
import com.hack.InventoryManagementSystem.exceptions.InvalidCredentialsException;
import com.hack.InventoryManagementSystem.exceptions.NotFoundException;
import com.hack.InventoryManagementSystem.repository.UserRepository;
import com.hack.InventoryManagementSystem.security.JwtUtils;
import com.hack.InventoryManagementSystem.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;

    @Override
    public Response registerUser(RegisterRequest registerRequest) {
        UserRole userRole = UserRole.MANAGER;
        if (registerRequest.getRole() != null) {
            userRole = registerRequest.getRole();
        }
        User userToSave = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(userRole)
                .build();

        userRepository.save(userToSave);

        return Response.builder()
                .status(200)
                .message("User created successfully")
                .build();

    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()
                -> new NotFoundException("Email not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("password does not match");
        }
        String token = jwtUtils.generateToken(user.getEmail());

        return Response.builder()
                .status(200)
                .message("user logged in successfully")
                .role(user.getRole())
                .token(token)
                .expirationTime("6 months")
                .build();
    }

    @Override
    public Response getAllUsers() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<UserDTO> userDTOS = modelMapper.map(users, new TypeToken<List<UserDTO>>() {
        }.getType());

        userDTOS.forEach(userDTO -> userDTO.setTransactions(null));

        return Response.builder()
                .status(200)
                .message("success")
                .users(userDTOS)
                .build();
    }

    @Override
    public User getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not Found"));
        user.setTransactions(null);
        return user;

    }

    @Override
    public Response updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not Found"));

        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getName() != null) existingUser.setName(userDTO.getName());
        if (userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getRole() != null) existingUser.setRole(userDTO.getRole());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        userRepository.save(existingUser);

        return Response.builder()
                .status(200)
                .message("User Successfully updated")
                .build();
    }

    @Override
    public Response deleteUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not Found"));

        userRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("User Successfully Deleted")
                .build();
    }

    @Override
    public Response getUserTransaction(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not Found"));
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        userDTO.getTransactions().forEach(transactionDTO -> {
            transactionDTO.setUser(null);
            transactionDTO.setSupplier(null);
        });
        return Response.builder()
                .status(200)
                .message("success")
                .user(userDTO)
                .build();
    }
}
