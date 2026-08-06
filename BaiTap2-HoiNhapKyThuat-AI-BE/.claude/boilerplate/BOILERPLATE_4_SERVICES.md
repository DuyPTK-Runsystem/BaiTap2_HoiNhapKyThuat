# KiemThuPhanMem - Services

## Overview
Lớp Service chứa business logic của ứng dụng.

---

## 1. UserService.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/service/UserService.java`

```java
package com.uit.nhom7.KiemThuPhanMem.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.uit.nhom7.KiemThuPhanMem.domain.responseDTO.ResLoginDTO;
import com.uit.nhom7.KiemThuPhanMem.domain.responseDTO.ResUserDTO;
import com.uit.nhom7.KiemThuPhanMem.domain.responseDTO.ResultPaginationDTO;
import com.uit.nhom7.KiemThuPhanMem.domain.table.User;
import com.uit.nhom7.KiemThuPhanMem.repository.UserRepository;
import com.uit.nhom7.KiemThuPhanMem.util.error.IdInvalidException;

@Service
@Validated
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Convert User entity to ResUserDTO
     */
    private ResUserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }

        return ResUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .accountStatus(user.getAccountStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    /**
     * Convert User entity to ResLoginDTO
     */
    private ResLoginDTO convertToLoginDTO(User user) {
        if (user == null) {
            return null;
        }

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                user.getId(),
                user.getEmail(),
                user.getName());

        ResLoginDTO.Role roleDTO = null;
        if (user.getRole() != null) {
            roleDTO = new ResLoginDTO.Role(user.getRole().getId(), user.getRole().getName());
        }

        return ResLoginDTO.builder()
                .user(userLogin)
                .role(roleDTO)
                .build();
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public User handleFindByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Find user by email and refresh token
     */
    @Transactional(readOnly = true)
    public User handleFindByEmailAndRefreshToken(String email, String refreshToken) {
        return userRepository.findByEmailAndRefreshToken(email, refreshToken).orElse(null);
    }

    /**
     * Get user by ID and convert to DTO
     */
    @Transactional(readOnly = true)
    public ResUserDTO handleFetchUserById(UUID id) {
        User user = userRepository.findById(id).orElse(null);
        return convertToDTO(user);
    }

    /**
     * Get user entity by ID (internal)
     */
    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Get all users with filter and pagination
     */
    @Transactional(readOnly = true)
    public ResultPaginationDTO handleGetAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUsers = userRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotalPages(pageUsers.getTotalPages());
        meta.setTotalItems(pageUsers.getTotalElements());

        rs.setMeta(meta);

        List<ResUserDTO> userDTOs = pageUsers.getContent().stream()
                .map(this::convertToDTO)
                .toList();
        rs.setResult(userDTOs);

        return rs;
    }

    /**
     * Update user refresh token
     */
    @Transactional
    public void updateUserRefreshToken(String refreshToken, String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        }
    }

    /**
     * Handle user logout
     */
    @Transactional
    public void handleLogOutUser(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setRefreshToken(null);
            userRepository.save(user);
        }
    }

    /**
     * Delete user by ID
     */
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("User with id " + id + " does not exist"));
        userRepository.deleteById(id);
    }
}
```

**Key Methods**:
- `handleFindByEmail(String)` - Tìm user theo email
- `handleGetAllUsers(Specification, Pageable)` - Lấy tất cả users với filter và pagination
- `updateUserRefreshToken(String, String)` - Cập nhật refresh token
- `handleLogOutUser(String)` - Xử lý logout
- `deleteUser(UUID)` - Xóa user

---

## 2. RoleService.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/service/RoleService.java`

```java
package com.uit.nhom7.KiemThuPhanMem.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.nhom7.KiemThuPhanMem.domain.requestDTO.ReqCreateRoleDTO;
import com.uit.nhom7.KiemThuPhanMem.domain.requestDTO.ReqUpdateRoleDTO;
import com.uit.nhom7.KiemThuPhanMem.domain.responseDTO.ResPermissionDTO;
import com.uit.nhom7.KiemThuPhanMem.domain.responseDTO.ResRoleDTO;
import com.uit.nhom7.KiemThuPhanMem.domain.responseDTO.ResultPaginationDTO;
import com.uit.nhom7.KiemThuPhanMem.domain.table.Role;
import com.uit.nhom7.KiemThuPhanMem.repository.PermissionRepository;
import com.uit.nhom7.KiemThuPhanMem.repository.RoleRepository;
import com.uit.nhom7.KiemThuPhanMem.util.error.IdInvalidException;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Convert Role entity to ResRoleDTO
     */
    private ResRoleDTO convertToDTO(Role role) {
        if (role == null) {
            return null;
        }

        List<ResPermissionDTO> permissionDTOs = new ArrayList<>();
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            permissionDTOs = role.getPermissions().stream()
                    .map(permission -> ResPermissionDTO.builder()
                            .id(permission.getId())
                            .name(permission.getName())
                            .apiPath(permission.getApiPath())
                            .method(permission.getMethod())
                            .module(permission.getModule())
                            .createdAt(permission.getCreatedAt())
                            .updatedAt(permission.getUpdatedAt())
                            .createdBy(permission.getCreatedBy())
                            .updatedBy(permission.getUpdatedBy())
                            .build())
                    .collect(Collectors.toList());
        }

        return ResRoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.isActive())
                .permissions(permissionDTOs)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .build();
    }

    /**
     * Create a new role
     */
    @Transactional
    public ResRoleDTO createRole(ReqCreateRoleDTO request) {
        // Check if role with same name already exists
        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
        }

        // Create new role entity
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .permissions(new HashSet<>())
                .build();

        // Save and convert to DTO
        Role savedRole = roleRepository.save(role);
        return convertToDTO(savedRole);
    }

    /**
     * Update an existing role
     */
    @Transactional
    public ResRoleDTO updateRole(ReqUpdateRoleDTO request) {
        // Validate role exists
        Role role = roleRepository.findById(request.getId())
                .orElseThrow(() -> new IdInvalidException("Role with id " + request.getId() + " does not exist"));

        // Check if new name already exists (and is different from current name)
        if (request.getName() != null && !request.getName().equals(role.getName())
                && roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
        }

        // Update fields if provided
        if (request.getName() != null) {
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            role.setActive(request.getActive());
        }

        // Save and convert to DTO
        Role updatedRole = roleRepository.save(role);
        return convertToDTO(updatedRole);
    }

    /**
     * Get all roles with filter and pagination
     */
    @Transactional(readOnly = true)
    public ResultPaginationDTO handleGetAllRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> pageRoles = this.roleRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotalPages(pageRoles.getTotalPages());
        meta.setTotalItems(pageRoles.getTotalElements());

        rs.setMeta(meta);
        rs.setResult(pageRoles.getContent());
        return rs;
    }

    /**
     * Get role by ID
     */
    @Transactional(readOnly = true)
    public ResRoleDTO handleFetchRoleById(Long id) {
        Role role = roleRepository.findById(id).orElse(null);
        return convertToDTO(role);
    }

    /**
     * Get role entity by ID (internal use)
     */
    public Role getRoleById(Long id) {
        return this.roleRepository.findById(id).orElse(null);
    }

    /**
     * Delete role by ID
     */
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Role with id " + id + " does not exist"));
        this.roleRepository.deleteById(id);
    }
}
```

**Key Methods**:
- `createRole(ReqCreateRoleDTO)` - Tạo role mới
- `updateRole(ReqUpdateRoleDTO)` - Cập nhật role
- `handleGetAllRoles(Specification, Pageable)` - Lấy tất cả roles với filter
- `handleFetchRoleById(Long)` - Lấy role theo ID
- `deleteRole(Long)` - Xóa role

---

## Service Best Practices

1. **@Transactional**: Dùng cho các operation thay đổi dữ liệu
2. **@Transactional(readOnly = true)**: Dùng cho các query read-only
3. **@Validated**: Validate input DTOs
4. **Converter Methods**: Chuyển đổi từ Entity sang DTO
5. **Exception Handling**: Ném exception khi không tìm thấy resource