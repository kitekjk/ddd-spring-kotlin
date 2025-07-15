package com.example.domain.model.user

/**
 * Repository interface for User aggregate.
 * Defines operations for persisting and retrieving User entities.
 */
interface UserRepository {
    /**
     * Saves a user to the repository.
     * If the user has no ID, a new user is created with an auto-generated ID.
     * If the user has an ID, the existing user is updated.
     */
    fun save(user: User): User
    
    /**
     * Finds a user by ID.
     * @return the user if found, null otherwise
     */
    fun findById(id: UserId): User?
    
    /**
     * Finds a user by login ID.
     * @return the user if found, null otherwise
     */
    fun findByLoginId(loginId: String): User?
    
    /**
     * Finds a user by email.
     * @return the user if found, null otherwise
     */
    fun findByEmail(email: String): User?
    
    /**
     * Finds all users in the repository.
     * @return a list of all users
     */
    fun findAll(): List<User>
    
    /**
     * Deletes a user from the repository.
     */
    fun delete(user: User)
    
    /**
     * Deletes a user by ID.
     * @return true if the user was deleted, false if the user was not found
     */
    fun deleteById(id: UserId): Boolean
}