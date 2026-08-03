# KiemThuPhanMem - Repositories

## Overview
JPA Repository interfaces để thao tác với database.

---

## 1. UserRepository.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/repository/UserRepository.java`

```java
package com.uit.nhom7.KiemThuPhanMem.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.uit.nhom7.KiemThuPhanMem.domain.table.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndRefreshToken(String email, String refreshToken);
}
```

**Methods**:
- `findByEmail(String email)` - Find user by email
- `existsByEmail(String email)` - Check if email exists
- `findByEmailAndRefreshToken(String email, String token)` - Find user by email and refresh token
- Inherited from `JpaRepository<User, UUID>`: save(), findById(), delete(), etc.
- Inherited from `JpaSpecificationExecutor<User>`: findAll(Specification), etc.

---

## 2. RoleRepository.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/repository/RoleRepository.java`

```java
package com.uit.nhom7.KiemThuPhanMem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.uit.nhom7.KiemThuPhanMem.domain.table.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    boolean existsByName(String name);

    Role findByName(String name);
}
```

**Methods**:
- `existsByName(String name)` - Check if role with name exists
- `findByName(String name)` - Find role by name
- Inherited from `JpaRepository<Role, Long>`: save(), findById(), delete(), etc.
- Inherited from `JpaSpecificationExecutor<Role>`: findAll(Specification), etc.

---

## 3. PermissionRepository.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/repository/PermissionRepository.java`

```java
package com.uit.nhom7.KiemThuPhanMem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.uit.nhom7.KiemThuPhanMem.domain.table.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    boolean existsByApiPathAndMethodAndModule(String apiPath, String method, String module);
}
```

**Methods**:
- `existsByApiPathAndMethodAndModule(String, String, String)` - Check if permission with same apiPath, method, and module exists
- Inherited from `JpaRepository<Permission, Long>`: save(), findById(), delete(), etc.
- Inherited from `JpaSpecificationExecutor<Permission>`: findAll(Specification), etc.

---

## Usage Examples

### UserRepository Examples

```java
// Find user by email
Optional<User> user = userRepository.findByEmail("user@example.com");

// Check if email exists
boolean exists = userRepository.existsByEmail("user@example.com");

// Find user by email and refresh token
Optional<User> user = userRepository.findByEmailAndRefreshToken(
    "user@example.com", 
    "refreshTokenValue"
);

// Find by ID
Optional<User> user = userRepository.findById(uuid);

// Save user
User savedUser = userRepository.save(user);

// Delete user
userRepository.delete(user);

// Find all with pagination and filter
Page<User> users = userRepository.findAll(spec, pageable);
```

### RoleRepository Examples

```java
// Check if role exists
boolean exists = roleRepository.existsByName("ADMIN");

// Find by name
Role role = roleRepository.findByName("ADMIN");

// Find by ID
Optional<Role> role = roleRepository.findById(1L);

// Save role
Role savedRole = roleRepository.save(role);

// Find all with pagination
Page<Role> roles = roleRepository.findAll(spec, pageable);
```

### PermissionRepository Examples

```java
// Check if permission exists
boolean exists = permissionRepository.existsByApiPathAndMethodAndModule(
    "/api/v1/users", 
    "GET", 
    "USER_MANAGEMENT"
);

// Find all with pagination
Page<Permission> permissions = permissionRepository.findAll(spec, pageable);

// Save permission
Permission savedPermission = permissionRepository.save(permission);
```

---

## JpaSpecificationExecutor Interface

Cung cấp khả năng thực hiện các truy vấn phức tạp:

```java
// Example: Filter users by email containing "admin"
Specification<User> spec = (root, query, cb) -> 
    cb.like(root.get("email"), "%admin%");

Page<User> users = userRepository.findAll(spec, pageable);
```

---

## JpaRepository Interface

Cung cấp các phương thức CRUD cơ bản:
- `save(S entity)` - Lưu entity
- `findById(ID id)` - Tìm theo ID
- `findAll()` - Lấy tất cả
- `delete(T entity)` - Xóa entity
- `deleteById(ID id)` - Xóa theo ID
- `count()` - Đếm số lượng
- `existsById(ID id)` - Kiểm tra tồn tại
