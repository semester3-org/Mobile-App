package com.apk.koshub.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.koshub.api.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    val notifications = MutableLiveData<List<NotificationItem>>()
    val errorMsg = MutableLiveData<String?>()

    private var userIdUsed = -1
    private var pollingJob: Job? = null

    fun loadNotifications(userId: Int) {
        userIdUsed = userId
        fetchOnce()
        startPolling()
    }

    private fun fetchOnce() {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.getNotifications(userIdUsed)

                if (res.isOk()) {
                    notifications.postValue(res.data ?: emptyList())
                    errorMsg.postValue(null)
                } else {
                    notifications.postValue(emptyList())
                    errorMsg.postValue(res.message ?: "Response success=false (atau field success/status mismatch)")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                errorMsg.postValue(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private fun startPolling() {
        if (pollingJob != null) return
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                fetchOnce()
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun markAsRead(notificationId: Int) {
        val list = notifications.value?.toMutableList() ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == notificationId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(is_read = 1)
            notifications.postValue(list)
        }

        viewModelScope.launch {
            try { ApiClient.api.markNotificationRead(notificationId) } catch (_: Exception) {}
        }
    }

    fun markAllRead(userId: Int) {
        val list = notifications.value?.map { it.copy(is_read = 1) } ?: emptyList()
        notifications.postValue(list)

        viewModelScope.launch {
            try { ApiClient.api.markAllRead(userId) } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
