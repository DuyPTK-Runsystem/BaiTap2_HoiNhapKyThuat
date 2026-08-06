# KiemThuPhanMem - Database Entities

## Overview
Các entity (JPA entities) định nghĩa cấu trúc cơ sở dữ liệu cho ứng dụng.

---

## 1. User.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/table/User.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.table;

import java.time.Instant;
import java.util.UUID;

import com.uit.nhom7.KiemThuPhanMem.util.SecurityUtil;
import com.uit.nhom7.KiemThuPhanMem.util.UuidV7Generator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    private String name;

    @NotBlank(message = "Không được để trống email")
    private String email;

    @NotBlank(message = "Không được để trống mật khẩu")
    private String password;

    private String accountStatus;

    @Min(value = 0)
    private Integer failedLoginAttempts;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = generateUUIDv7();
        }
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    private UUID generateUUIDv7() {
        return UuidV7Generator.generate();
    }
}
```

---

## 2. Role.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/table/Role.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.table;

import java.time.Instant;
import java.util.Set;

import com.uit.nhom7.KiemThuPhanMem.util.SecurityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private boolean active;

    @OneToMany(mappedBy = "role")
    private Set<User> users;

    @ManyToMany
    @JoinTable(name = "role_permission", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
        if (!active) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }
}
```

---

## 3. Permission.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/table/Permission.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.table;

import java.time.Instant;
import java.util.Set;

import com.uit.nhom7.KiemThuPhanMem.util.SecurityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "apiPath", "method" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name không được để trống")
    private String name;

    private String apiPath;
    private String method;
    private String module;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }
}
```

---

## Database Relationships

### User → Role (N:1)
- One Role can have many Users
- Each User belongs to one Role

### Role ↔ Permission (N:M)
- Multiple Roles can have the same Permission
- One Role can have multiple Permissions
- Junction table: `role_permission`

---

## Table Creation SQL (Reference)

```sql
CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    account_status VARCHAR(50),
    failed_login_attempts INT DEFAULT 0,
    refresh_token MEDIUMTEXT,
    role_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    api_path VARCHAR(255),
    method VARCHAR(50),
    module VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE KEY unique_api_path_method (api_path, method)
);

CREATE TABLE IF NOT EXISTS role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);
```