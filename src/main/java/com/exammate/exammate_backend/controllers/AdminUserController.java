package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.AdminUserResponse;
import com.exammate.exammate_backend.models.Role;
import com.exammate.exammate_backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all users (optionally filter by role)")
    public List<AdminUserResponse> getAllUsers(@RequestParam(required = false) Role role) {
        if (role == null) return userService.getAllUsers();
        return userService.getUsersByRole(role);
    }
}
