# AuditInfo Refactoring Worklog

## Prompt
```
AuditInfo 는 공용으로 사용할 예정이기 때문에 model.common 밑으로 이동 시켜줘, 그리고 LocalDateTime 대신에 Instant로 변경 해줘
```

## Work Start Time
2023-11-02 14:00:00

## Work End Time
2023-11-02 14:30:00

## Summary of Changes
Refactored the AuditInfo class to make it more reusable across the project:

1. Moved AuditInfo from domain.model.user package to domain.model.common package
2. Changed the timestamp type from LocalDateTime to Instant for better timezone handling
3. Updated all references to AuditInfo in other files:
    - User.kt: Updated import statement
    - UserEntity.kt: Updated import statement and field types
    - UserDto.kt: Updated field types

## Modified Files
1. domain/src/main/kotlin/com/example/domain/model/common/AuditInfo.kt (new)
2. domain/src/main/kotlin/com/example/domain/model/user/User.kt (modified)
3. infrastructure/src/main/kotlin/com/example/infrastructure/persistence/user/UserEntity.kt (modified)
4. application/src/main/kotlin/com/example/application/user/dto/UserDto.kt (modified)
5. domain/src/main/kotlin/com/example/domain/model/user/AuditInfo.kt (deleted)