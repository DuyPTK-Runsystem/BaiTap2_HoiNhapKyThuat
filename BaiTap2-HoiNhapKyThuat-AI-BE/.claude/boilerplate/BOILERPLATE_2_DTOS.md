# KiemThuPhanMem - DTOs (Request & Response)

## Overview
Data Transfer Objects (DTOs) để transfer dữ liệu giữa client và server.

---

## Request DTOs

### 1. ReqLoginDTO.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/requestDTO/ReqLoginDTO.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqLoginDTO {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
```

---

### 2. ReqCreateRoleDTO.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/requestDTO/ReqCreateRoleDTO.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new Role
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqCreateRoleDTO {

    @NotBlank(message = "Tên role không được để trống")
    private String name;

    private String description;
}
```

---

### 3. ReqUpdateRoleDTO.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/requestDTO/ReqUpdateRoleDTO.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing Role
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqUpdateRoleDTO {

    @NotNull(message = "ID không được để trống")
    private Long id;

    private String name;

    private String description;

    private Boolean active;
}
```

---

## Response DTOs

### 1. RestResponse.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/responseDTO/RestResponse.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestResponse<T> {
    private int statusCode;
    private String error;
    private Object message;
    private T data;
}
```

---

### 2. ResLoginDTO.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/responseDTO/ResLoginDTO.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.responseDTO;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class ResLoginDTO {
    @JsonProperty("access_token")
    private String accessToken;
    private UserLogin user;
    private Role role;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserLogin {
        private UUID id;
        private String email;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGetAccount {
        private UserLogin user;
        private Role role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInsideToken {
        private UUID id;
        private String email, name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Role {
        private Long roleId;
        private String roleName;
    }
}
```

---

### 3. ResUserDTO.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/responseDTO/ResUserDTO.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.responseDTO;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResUserDTO {
    private UUID id;
    private String email;
    private String name;
    private String accountStatus;

    @JsonProperty("role")
    private ResRoleDTO role;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_by")
    private String updatedBy;
}
```

---

### 4. ResRoleDTO.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/responseDTO/ResRoleDTO.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.responseDTO;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Role information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResRoleDTO {

    private Long id;

    private String name;

    private String description;

    private boolean active;

    private List<ResPermissionDTO> permissions;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;
}
```

---

### 5. ResPermissionDTO.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/responseDTO/ResPermissionDTO.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.responseDTO;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Permission information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResPermissionDTO {

    private Long id;

    private String name;

    private String apiPath;

    private String method;

    private String module;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;
}
```

---

### 6. ResultPaginationDTO.java

**Path**: `src/main/java/com/uit/nhom7/KiemThuPhanMem/domain/responseDTO/ResultPaginationDTO.java`

```java
package com.uit.nhom7.KiemThuPhanMem.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ResultPaginationDTO {
    private Meta meta;
    private Object result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Meta {
        private int page;
        private int pageSize;
        private int totalPages;
        private long totalItems;
    }
}
```

---

## JSON Examples

### Login Request
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Login Response
```json
{
  "statusCode": 200,
  "message": "Đăng nhập",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "email": "user@example.com",
      "name": "User Name"
    },
    "role": {
      "roleId": 1,
      "roleName": "ADMIN"
    }
  }
}
```

### Role Response (with pagination)
```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "meta": {
      "page": 1,
      "pageSize": 20,
      "totalPages": 1,
      "totalItems": 2
    },
    "result": [
      {
        "id": 1,
        "name": "ADMIN",
        "description": "Admin role",
        "active": true,
        "permissions": [...],
        "created_at": "2024-01-01T10:00:00Z",
        "updated_at": "2024-01-01T10:00:00Z",
        "created_by": "system",
        "updated_by": "system"
      }
    ]
  }
}
```