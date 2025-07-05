package com.fastkantin.finalproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastkantin.finalproject.data.database.FastKantinDatabase
import com.fastkantin.finalproject.data.entity.User
import com.fastkantin.finalproject.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository

    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> = _registerResult

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> = _updateResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        val database = FastKantinDatabase.getDatabase(application)
        userRepository = UserRepository(database.userDao())
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = userRepository.login(email, password)
                _loginResult.value = user
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _loginResult.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(user: User) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if email already exists
                val existingUser = userRepository.getUserByEmail(user.email)
                if (existingUser != null) {
                    _errorMessage.value = "Email sudah terdaftar"
                    _registerResult.value = false
                } else {
                    val result = userRepository.register(user)
                    _registerResult.value = result > 0
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _registerResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getUserById(userId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                _currentUser.value = user
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error loading user data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getUserByIdLiveData(userId: Int): LiveData<User> {
        return userRepository.getUserByIdLiveData(userId)
    }

    fun updateUser(user: User) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if email is already used by another user
                val existingUser = userRepository.getUserByEmail(user.email)
                if (existingUser != null && existingUser.user_id != user.user_id) {
                    _errorMessage.value = "Email sudah digunakan oleh user lain"
                    _updateResult.value = false
                } else {
                    userRepository.updateUser(user)
                    _updateResult.value = true
                    _currentUser.value = user
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error updating user data"
                _updateResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
}
