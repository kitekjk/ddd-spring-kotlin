# Work Log - May 24, 2023

## Task: Implement User CRUD functionality

### Work Start Time
10:00 AM

### Work End Time
11:30 AM

### Summary of Changes
Implemented a complete User CRUD functionality following Domain-Driven Design principles in a multi-module Spring Boot project. The implementation includes:

1. Domain Model:
   - User aggregate root with encapsulated state and behavior
   - UserId value object
   - UserRepository interface

2. Application Services:
   - CreateUserUseCase for creating users
   - GetUserUseCase for retrieving users
   - UpdateUserUseCase for updating users
   - DeleteUserUseCase for deleting users

3. Infrastructure:
   - JPA entity mapping with UserEntity
   - Spring Data JPA repository implementation
   - Domain-Infrastructure adapter with UserRepositoryImpl

4. Web Interface:
   - REST controller with endpoints for all CRUD operations
   - Request/Response DTOs with validation
   - Proper HTTP status codes and error handling

### Modified Files
- domain/src/main/kotlin/com/example/domain/model/user/UserId.kt
- domain/src/main/kotlin/com/example/domain/model/user/User.kt
- domain/src/main/kotlin/com/example/domain/model/user/UserRepository.kt
- application/src/main/kotlin/com/example/application/user/CreateUserUseCase.kt
- application/src/main/kotlin/com/example/application/user/CreateUserService.kt
- application/src/main/kotlin/com/example/application/user/GetUserUseCase.kt
- application/src/main/kotlin/com/example/application/user/GetUserService.kt
- application/src/main/kotlin/com/example/application/user/UpdateUserUseCase.kt
- application/src/main/kotlin/com/example/application/user/UpdateUserService.kt
- application/src/main/kotlin/com/example/application/user/DeleteUserUseCase.kt
- application/src/main/kotlin/com/example/application/user/DeleteUserService.kt
- infrastructure/src/main/kotlin/com/example/infrastructure/persistence/user/JpaUserRepository.kt
- infrastructure/src/main/kotlin/com/example/infrastructure/persistence/user/UserEntity.kt
- infrastructure/src/main/kotlin/com/example/infrastructure/persistence/user/UserRepositoryImpl.kt
- interfaces/src/main/kotlin/com/example/interfaces/web/user/UserRequest.kt
- interfaces/src/main/kotlin/com/example/interfaces/web/user/UserResponse.kt
- interfaces/src/main/kotlin/com/example/interfaces/web/user/UserController.kt

### Prompt
```
User 를 만들고 CRUD 까지 생성해줘
- id(자동증가형), 성명, email, loginId, password, 등록일, 수정일
```