package com.apk.koshub.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apk.koshub.models.FilterState

class HomeFilterViewModel : ViewModel() {

    val filterState = MutableLiveData(FilterState())

    fun updateFilter(newState: FilterState) {
        filterState.value = newState
    }

    fun reset() {
        filterState.value = FilterState()
    }
}
