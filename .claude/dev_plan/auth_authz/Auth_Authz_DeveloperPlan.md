# Developer Plan: Authentication và Authorization

## 1. Trạng thái

- Trạng thái: Đã được người dùng phê duyệt.
- Ngày phê duyệt: 2026-08-03.
- Phạm vi authorization: Ứng dụng không sử dụng Roles và Permissions.
- Được phép bắt đầu chỉnh sửa mã nguồn trong phạm vi kế hoạch này.
- Điều chỉnh refactor: Người dùng yêu cầu refactor Auth/Authz theo convention Lombok trong boilerplate.

## 2. Mục tiêu

Cấu hình project theo các tài liệu boilerplate trong `.claude/boilerplate` và triển khai nền tảng Authentication cho ứng dụng Spring Boot, sử dụng JWT và BCrypt.

Authorization trong phạm vi này chỉ dừng ở việc yêu cầu người dùng đã xác thực cho các API protected. Không triển khai mô hình Role/Permission hoặc phân quyền theo authority.

## 3. Tài liệu đối chiếu

- `.claude/boilerplate/BOILERPLATE_0_SUMMARY.md`
  - Kiến trúc starter, authentication flow và security layer.
- `.claude/boilerplate/BOILERPLATE_1_ENTITIES.md`
  - Thiết kế entity User.
- `.claude/boilerplate/BOILERPLATE_2_DTOS.md`
  - Request/response DTO và pagination response.
- `.claude/boilerplate/BOILERPLATE_3_REPOSITORIES.md`
  - UserRepository.
- `.claude/boilerplate/BOILERPLATE_4_SERVICES.md`
  - UserService.
- `.claude/boilerplate/BOILERPLATE_5_CONTROLLERS_UTILITIES.md`
  - AuthController, SecurityUtil và response formatter.
- `.claude/boilerplate/BOILERPLATE_6_CONFIGURATION.md`
  - Security, OpenAPI, JWT và application configuration.
- `.claude/docs/modules/Auth_Module.md`
  - Đăng ký, đăng nhập, password hashing và email unique.
- `.claude/docs/Data_Architecture.md`
  - Ràng buộc dữ liệu User và password đã hash.
- `.claude/rules/CLAUDE.md`
  - Quy định bắt buộc về Developer Plan, phạm vi, chất lượng và báo cáo.
- `.claude/workflows/WORKFLOW.md`
  - Quy trình trước, trong và sau khi triển khai code.

## 4. Phạm vi thực hiện

### 4.1. Cấu hình nền tảng

- Bổ sung dependency cần thiết cho JWT Resource Server nếu dependency hiện tại chưa đáp ứng.
- Giữ Java 21 và Spring Boot 4.0.7 hiện tại.
- Cấu hình database, JWT, server, pagination và multipart bằng biến môi trường khi chứa thông tin nhạy cảm.
- Cấu hình BCrypt password encoder.
- Cấu hình stateless JWT security.
- Cấu hình CORS.
- Cấu hình OpenAPI/Swagger với Bearer JWT.
- Cấu hình response lỗi 401 và exception handler tập trung.

### 4.2. Authentication

Triển khai các chức năng:

- Đăng ký tài khoản.
- Đăng nhập và cấp access token.
- Refresh token.
- Lấy thông tin account hiện tại.
- Logout và thu hồi refresh token.
- Kiểm tra email unique.
- Hash password trước khi lưu.

API dự kiến:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/account
GET  /api/v1/auth/refresh
POST /api/v1/auth/logout
```

### 4.3. Authorization tối giản

- Endpoint đăng ký, đăng nhập, refresh và Swagger được public theo cấu hình được duyệt.
- Các endpoint còn lại yêu cầu JWT hợp lệ.
- Không dùng `Role`, `Permission`, `@Secured`, `@PreAuthorize` hoặc authority-based authorization.
- JWT không chứa claim Role hoặc Permission.

## 5. Phạm vi không thực hiện

Không tạo, chỉnh sửa hoặc triển khai:

- `Role.java`.
- `Permission.java`.
- `RoleRepository.java`.
- `PermissionRepository.java`.
- `RoleService.java`.
- `PermissionService.java`.
- `ResRoleDTO.java`.
- `ResPermissionDTO.java`.
- API `/api/v1/roles/**`.
- API `/api/v1/permissions/**`.
- Quan hệ User–Role.
- Quan hệ Role–Permission.
- Role/Permission claims trong JWT.
- Logic phân quyền theo Role/Permission.
- Vocabulary, Organization và Testing implementation trong phase này.

## 6. Kiến trúc và thiết kế dự kiến

Package gốc sử dụng package hiện tại của project:

```text
net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI
```

Các layer:

```text
controller
service
repository
domain/table
domain/requestDTO
domain/responseDTO
config
util
```

Luồng đăng ký:

1. Client gửi email và password.
2. Controller validate request.
3. Service kiểm tra email đã tồn tại.
4. Service hash password bằng BCrypt.
5. Service lưu User.
6. API trả response User không chứa password, refresh token hoặc thông tin Roles/Permissions.

Luồng đăng nhập:

1. Client gửi email và password.
2. Spring Security xác thực thông tin đăng nhập.
3. Service tạo access token và refresh token.
4. Refresh token được lưu theo thiết kế được phê duyệt.
5. Response trả access token và thông tin User an toàn.

Luồng refresh:

1. Client gửi refresh token theo cơ chế được cấu hình.
2. Backend kiểm tra token hợp lệ và đối chiếu với User.
3. Backend cấp access token mới.
4. Không thêm Role/Permission claims.

Luồng logout:

1. Backend xác định User hiện tại.
2. Xóa hoặc vô hiệu hóa refresh token đã lưu.
3. Trả response thành công.

## 7. Thiết kế User và database

### 7.1. Fields dự kiến

- `Long id` hoặc kiểu ID quan hệ tương đương theo schema database được chọn.
- `String email`.
- `String password` trong Java.
- `String refreshToken`.
- `Instant createdAt`.
- `Instant updatedAt`.
- `String createdBy`.
- `String updatedBy`.

### 7.2. Mapping dự kiến

Do tài liệu đang có khác biệt giữa boilerplate và data architecture, kiểu ID sẽ được chọn theo schema database thực tế; project không bắt buộc sử dụng UUID.

- ID dùng kiểu quan hệ phù hợp với schema, mặc định dự kiến là `Long`.
- Database column dự kiến là `user_id`.
- Java field password dự kiến map tới column `hash_password`.
- Email phải unique và not null.
- Password phải not null và luôn là giá trị đã hash.
- Audit fields map tới `created_at`, `updated_at`, `created_by`, `updated_by`.

Nếu người dùng muốn dùng schema khác, cần cập nhật Developer Plan trước khi code.

## 8. Danh sách file dự kiến

| File | Loại thay đổi | Mục đích |
|---|---|---|
| `build.gradle.kts` | Chỉnh sửa | Dependency JWT Resource Server nếu cần |
| `src/main/resources/application.properties` | Chỉnh sửa | Database, JWT, server, pagination và multipart config |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/table/User.java` | Tạo mới | Entity User không có Role/Permission |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/requestDTO/ReqLoginDTO.java` | Tạo mới | Request login |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/requestDTO/ReqRegisterDTO.java` | Tạo mới | Request register |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/RestResponse.java` | Tạo mới | Chuẩn hóa response |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/ResLoginDTO.java` | Tạo mới | Response login không có Role |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/ResUserDTO.java` | Tạo mới | Response User an toàn |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/ResultPaginationDTO.java` | Tạo mới | Pagination response |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/repository/UserRepository.java` | Tạo mới | Repository User |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/UserService.java` | Tạo mới | Nghiệp vụ User/Auth |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/controller/AuthController.java` | Tạo mới | Auth APIs |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/config/SecurityConfiguration.java` | Tạo mới | JWT, BCrypt, CORS và protected routes |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/config/OpenAPIConfig.java` | Tạo mới | Swagger/OpenAPI |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/config/CustomAuthenticationEntryPoint.java` | Tạo mới | JSON response cho 401 |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/util/SecurityUtil.java` | Tạo mới | JWT utility không có Role/Permission |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/util/FormatRestResponse.java` | Tạo mới | Response formatter |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/util/annotation/ApiMessage.java` | Tạo mới | API message annotation |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/util/error/IdInvalidException.java` | Tạo mới | Invalid ID exception |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/util/error/GlobalExceptionHandler.java` | Tạo mới | Centralized exception handling |
| `src/test/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/...` | Tạo/chỉnh sửa | Test Auth, Security và response |

## 9. Vị trí thay đổi trong từng file

| File | Class/Component | Method/Khu vực | Nội dung thay đổi |
|---|---|---|---|
| `build.gradle.kts` | Gradle dependencies | `dependencies` | Bổ sung dependency phục vụ JWT nếu cần |
| `User.java` | `User` | Fields, lifecycle callbacks | Mapping User, unique email, password hash, refresh token và audit fields |
| `UserRepository.java` | `UserRepository` | Query methods | `findByEmail`, `existsByEmail`, `findByEmailAndRefreshToken` |
| `UserService.java` | `UserService` | Register/login/refresh/logout | Xử lý nghiệp vụ User và token |
| `AuthController.java` | `AuthController` | `/api/v1/auth/**` | Register, login, account, refresh và logout |
| `SecurityConfiguration.java` | `SecurityFilterChain` | Security beans | BCrypt, JWT, CORS, stateless session và route protection |
| `SecurityUtil.java` | `SecurityUtil` | Token methods | Tạo/kiểm tra JWT không có Role/Permission |
| `application.properties` | Application configuration | Config sections | Database/JWT/server/pagination/multipart |
| `GlobalExceptionHandler.java` | Exception handler | `@ExceptionHandler` methods | Validation, auth, invalid ID và unexpected errors |
| Test files | Test classes | Unit/integration test methods | Kiểm tra behavior Auth/Security |

## 9.1. Refactor Lombok theo boilerplate

Người dùng xác nhận boilerplate sử dụng Lombok annotation và yêu cầu refactor các class Auth/Authz hiện tại.

Phạm vi refactor:

- Entity `User`: dùng Lombok thay getter/setter/constructor thủ công.
- Request/response DTO: dùng class + Lombok annotation theo boilerplate thay vì Java record.
- `RestResponse` và `ResultPaginationDTO`: dùng Lombok thay getter/setter thủ công.
- Service/controller/utility có constructor injection: dùng `@RequiredArgsConstructor` khi phù hợp.
- Không thay đổi schema, endpoint, business logic, Role/Permission scope hoặc docs requirement.

## 10. API và response dự kiến

### Register

```text
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secret"
}
```

- Thành công: trả thông tin User an toàn.
- Email đã tồn tại: trả lỗi validation/business phù hợp.
- Password không hợp lệ: trả lỗi validation.
- Không trả password hoặc refresh token.

### Login

```text
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secret"
}
```

- Thành công: trả access token và thông tin User an toàn.
- Sai thông tin: trả `401`.
- Không chứa Role/Permission.

### Account

```text
GET /api/v1/auth/account
Authorization: Bearer <access-token>
```

- JWT hợp lệ: trả User hiện tại.
- Thiếu hoặc sai JWT: trả `401`.

### Refresh

```text
GET /api/v1/auth/refresh
```

- Token hợp lệ: cấp access token mới.
- Token không hợp lệ hoặc đã thu hồi: trả `401`.

### Logout

```text
POST /api/v1/auth/logout
Authorization: Bearer <access-token>
```

- Thu hồi refresh token.
- Token access hiện tại không được ghi log đầy đủ.

## 11. Unit test dự kiến

- Application context load.
- User entity lifecycle audit fields.
- Email unique.
- Password được hash, không lưu plain text.
- UserRepository tìm theo email.
- UserRepository tìm theo email và refresh token.
- Register thành công.
- Register với email trùng.
- Login thành công.
- Login sai thông tin.
- Account với JWT hợp lệ.
- Account không có JWT.
- Refresh token hợp lệ/không hợp lệ.
- Logout xóa refresh token.
- Protected endpoint yêu cầu authentication.
- JWT không chứa Role/Permission claims.
- Response lỗi validation và authentication có format thống nhất.

## 12. Rủi ro và phương án xử lý

1. **Khác biệt schema User**
    - Rủi ro: tên cột hoặc kiểu ID không khớp tài liệu.
    - Phương án: dùng kiểu ID quan hệ phù hợp với schema, mặc định là `Long`, map rõ tên column `user_id` và `hash_password`; cập nhật plan nếu người dùng chọn schema khác.

2. **JWT dependency chưa đầy đủ**
   - Rủi ro: các bean encoder/decoder không compile.
   - Phương án: kiểm tra dependency hiện tại trước khi bổ sung, chỉ thêm dependency cần thiết.

3. **Lộ secret**
   - Rủi ro: database password/JWT secret bị commit.
   - Phương án: dùng environment variables và giá trị mặc định không nhạy cảm cho local.

4. **CORS sai origin**
   - Rủi ro: frontend không gọi được API hoặc mở CORS quá rộng.
   - Phương án: giới hạn origin cụ thể, mặc định `http://localhost:5173` chỉ khi được xác nhận.

5. **Response đã committed trong authentication entry point**
   - Rủi ro: lỗi ghi response hoặc response không nhất quán.
   - Phương án: chỉ delegate hoặc tự ghi JSON một lần, kiểm tra trạng thái response trước khi ghi.

6. **Boilerplate có cấu hình permit all**
   - Rủi ro: API protected bị public.
   - Phương án: dùng `.anyRequest().authenticated()`.

7. **Không có Roles/Permissions**
   - Rủi ro: triển khai nhầm authority từ boilerplate.
   - Phương án: kiểm tra toàn bộ entity, DTO, JWT, security converter và endpoint để không có tham chiếu Role/Permission.

## 13. Điều kiện phê duyệt

Developer Plan được xem là đã sẵn sàng triển khai khi người dùng xác nhận:

1. Phạm vi phase đầu là Auth và project configuration.
2. Giữ Java 21/Spring Boot 4.0.7.
3. Không bắt buộc UUID; dùng kiểu ID quan hệ phù hợp với schema, mặc định là `Long`, và map database column `user_id`, `hash_password`.
4. Triển khai `POST /api/v1/auth/register`.
5. Không tạo hoặc tham chiếu Roles/Permissions.
6. Cho phép CORS mặc định `http://localhost:5173` nếu chưa có origin khác.
7. Chấp nhận cấu hình secret qua environment variables.

## 14. Trạng thái cuối kế hoạch

- Đã được phê duyệt.
- Đã triển khai mã nguồn trong phạm vi Auth/Authz.
- Đã đối chiếu lại với `.claude/docs` theo yêu cầu người dùng.
- Ghi chú sau triển khai: schema `users` được điều chỉnh chỉ còn `user_id`, `email`, `hash_password`, `refresh_token` theo `.claude/docs/Data_Architecture.md`.
- Cập nhật sau yêu cầu mới: thêm audit fields `created_at`, `updated_at`, `created_by`, `updated_by` cho entity.
