package com.fastkantin.finalproject.data.repository

import androidx.lifecycle.LiveData
import com.fastkantin.finalproject.data.dao.UserDao
import com.fastkantin.finalproject.data.entity.User

class UserRepository(private val userDao: UserDao) {

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    suspend fun register(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    fun getUserByIdLiveData(userId: Int): LiveData<User> {
        return userDao.getUserByIdLiveData(userId)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
}
