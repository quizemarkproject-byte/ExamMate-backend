package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.AdminUserResponse;
import com.exammate.exammate_backend.models.Role;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<AdminUserResponse> getAllUsers();
    List<AdminUserResponse> getUsersByRole(Role role);
    AdminUserResponse getUserById(UUID id);
}

