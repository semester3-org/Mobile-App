package com.apk.koshub.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.koshub.api.ApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    val notifications = MutableLiveData<List<NotificationItem>>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMsg = MutableLiveData<String?>()

    private var userIdUsed = -1
    private var isPolling = false

    fun loadNotifications(userId: Int) {
        userIdUsed = userId
        fetchOnce()
        startPolling()
    }

    private fun fetchOnce() {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.getNotifications(userIdUsed)
                notifications.postValue(res.data ?: emptyList())
            } catch (e: Exception) {
                e.printStackTrace()
                errorMsg.postValue(e.localizedMessage)
            }
        }
    }

    private fun startPolling() {
        if (isPolling) return
        isPolling = true

        viewModelScope.launch {
            while (true) {
                delay(2000) // cek notif setiap 5 detik
                fetchOnce()
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        val list = notifications.value?.toMutableList() ?: return
        val idx = list.indexOfFirst { it.id == notificationId }
        if (idx >= 0) {
            val item = list[idx]
            list[idx] = item.copy(is_read = 1)
            notifications.postValue(list)

            viewModelScope.launch {
                try {
                    ApiClient.api.markNotificationRead(notificationId)
                } catch (_: Exception) {}
            }
        }
    }
    fun markAllRead(userId: Int) {
        val list = notifications.value?.map { it.copy(is_read = 1) } ?: return
        notifications.postValue(list)

        viewModelScope.launch {
            try {
                ApiClient.api.markAllRead(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
