package com.example.interfaces.web.user

import com.example.application.user.CreateUserAppService
import com.example.application.user.DeleteUserAppService
import com.example.application.user.GetUserAppService
import com.example.application.user.UpdateUserAppService
import com.example.interfaces.web.user.dto.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for user management.
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val createUserAppService: CreateUserAppService,
    private val getUserAppService: GetUserAppService,
    private val updateUserAppService: UpdateUserAppService,
    private val deleteUserAppService: DeleteUserAppService
) {
    /**
     * Creates a new user.
     */
    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        // For simplicity, using a fixed value for createdBy
        // In a real application, this would come from the authenticated user
        val createdBy = "system"
        
        val command = request.toCommand(createdBy)
        val userDto = createUserAppService.execute(command)
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(UserResponse.from(userDto))
    }
    
    /**
     * Gets a user by ID.
     */
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val userDto = getUserAppService.getById(id)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(UserResponse.from(userDto))
    }
    
    /**
     * Gets all users.
     */
    @GetMapping
    fun getAllUsers(): ResponseEntity<UserListResponse> {
        val userDtos = getUserAppService.getAll()
        return ResponseEntity.ok(UserListResponse.from(userDtos))
    }
    
    /**
     * Updates a user.
     */
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        // For simplicity, using a fixed value for updatedBy
        // In a real application, this would come from the authenticated user
        val updatedBy = "system"
        
        val command = request.toCommand(id, updatedBy)
        
        return try {
            val userDto = updateUserAppService.execute(command)
            ResponseEntity.ok(UserResponse.from(userDto))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Updates a user's password.
     */
    @PutMapping("/{id}/password")
    fun updatePassword(
        @PathVariable id: Long,
        @RequestBody request: UpdatePasswordRequest
    ): ResponseEntity<UserResponse> {
        // For simplicity, using a fixed value for updatedBy
        // In a real application, this would come from the authenticated user
        val updatedBy = "system"
        
        val command = request.toCommand(id, updatedBy)
        
        return try {
            val userDto = updateUserAppService.updatePassword(command)
            ResponseEntity.ok(UserResponse.from(userDto))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Deletes a user.
     */
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        val command = id.toDeleteUserCommand()
        val deleted = deleteUserAppService.execute(command)
        
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}