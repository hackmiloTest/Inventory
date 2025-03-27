package com.hack.InventoryManagementSystem.services;

import com.hack.InventoryManagementSystem.dto.LoginRequest;
import com.hack.InventoryManagementSystem.dto.RegisterRequest;
import com.hack.InventoryManagementSystem.dto.Response;
import com.hack.InventoryManagementSystem.dto.UserDTO;
import com.hack.InventoryManagementSystem.entity.User;

public interface UserService {
    Response registerUser(RegisterRequest registerRequest);
    Response loginUser(LoginRequest loginRequest);
    Response getAllUsers();
    User getCurrentLoggedInUser();
    Response updateUser(Long id, UserDTO userDTO);
    Response deleteUser(Long id);
    Response getUserTransaction(Long id);
}
