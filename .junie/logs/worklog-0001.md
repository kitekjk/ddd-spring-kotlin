# User CRUD Implementation Worklog

## Prompt
```
User 를 만들고 CRUD 까지 생성해줘
- id(자동증가형), 성명, email, loginId, password
- 등록일, 등록자, 수정일, 수정자는 별도 VO로 만들고 User 에 포함시켜줘
```

## Work Start Time
2023-11-01 10:00:00

## Work End Time
2023-11-01 11:30:00

## Summary of Changes
Implemented a complete User entity with CRUD functionality following DDD principles in a multi-module Spring Boot + Kotlin project:

1. Created domain model:
   - User aggregate root with required fields
   - UserId value object for identity
   - AuditInfo value object for audit fields
   - UserRepository interface

2. Implemented infrastructure layer:
   - UserEntity JPA entity
   - JpaUserRepository Spring Data JPA repository
   - UserRepositoryImpl implementation of UserRepository

3. Implemented application layer:
   - DTOs for application layer
   - CreateUserAppService for creating users
   - GetUserAppService for retrieving users
   - UpdateUserAppService for updating users
   - DeleteUserAppService for deleting users

4. Implemented interfaces layer:
   - Request and response DTOs for web layer
   - UserController with CRUD endpoints

## Modified Files
1. domain/src/main/kotlin/com/example/domain/model/user/AuditInfo.kt (new)
2. domain/src/main/kotlin/com/example/domain/model/user/UserId.kt (new)
3. domain/src/main/kotlin/com/example/domain/model/user/User.kt (new)
4. domain/src/main/kotlin/com/example/domain/model/user/UserRepository.kt (new)
5. infrastructure/src/main/kotlin/com/example/infrastructure/persistence/user/UserEntity.kt (new)
6. infrastructure/src/main/kotlin/com/example/infrastructure/persistence/user/JpaUserRepository.kt (new)
7. infrastructure/src/main/kotlin/com/example/infrastructure/persistence/user/UserRepositoryImpl.kt (new)
8. application/src/main/kotlin/com/example/application/user/dto/UserDto.kt (new)
9. application/src/main/kotlin/com/example/application/user/CreateUserAppService.kt (new)
10. application/src/main/kotlin/com/example/application/user/GetUserAppService.kt (new)
11. application/src/main/kotlin/com/example/application/user/UpdateUserAppService.kt (new)
12. application/src/main/kotlin/com/example/application/user/DeleteUserAppService.kt (new)
13. interfaces/src/main/kotlin/com/example/interfaces/web/user/dto/UserRequestDto.kt (new)
14. interfaces/src/main/kotlin/com/example/interfaces/web/user/dto/UserResponseDto.kt (new)
15. interfaces/src/main/kotlin/com/example/interfaces/web/user/UserController.kt (new)