package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.AdminUserResponse;
import com.exammate.exammate_backend.exception.NotFoundException;
import com.exammate.exammate_backend.models.Role;
import com.exammate.exammate_backend.repositories.UserRepository;
import com.exammate.exammate_backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> modelMapper.map(u, AdminUserResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminUserResponse> getUsersByRole(Role role) {
        return userRepository.findAllByRole(role).stream()
                .map(u -> modelMapper.map(u, AdminUserResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public AdminUserResponse getUserById(UUID id) {
        var u = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        return modelMapper.map(u, AdminUserResponse.class);
    }
}

