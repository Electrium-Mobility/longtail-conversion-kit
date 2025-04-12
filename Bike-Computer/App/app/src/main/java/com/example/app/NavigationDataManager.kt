package com.example.app

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

//Singleton object to hold navigation data and pass it to BLE service
object NavigationDataManager {
    const val TAG = "NotificationService"
    const val MAPS_PACKAGE = "com.google.android.apps.maps"

    val _directionDistance = MutableStateFlow<String?>(null)
    val _directionText = MutableStateFlow<String?>(null)
    val _directionIcon = MutableStateFlow<Bitmap?>(null)
    val _etaInDuration = MutableStateFlow<String?>(null)
    val _etaInDistance = MutableStateFlow<String?>(null)
    val _etaInTime = MutableStateFlow<String?>(null)
    val _iconType = MutableStateFlow<String?>(null)

    //Use StateFlow so these are immutable and updated when above member vals are updated
    //Direction data to send
    val directionDistance: StateFlow<String?> = _directionDistance.asStateFlow()
    val directionText: StateFlow<String?> = _directionText.asStateFlow()
    val iconType: StateFlow<String?> = _iconType.asStateFlow()

    val directionIcon: StateFlow<Bitmap?> = _directionIcon.asStateFlow()

    //ETA data to send
    val etaInDuration: StateFlow<String?> = _etaInDuration.asStateFlow()
    val etaInDistance: StateFlow<String?> = _etaInDistance.asStateFlow()
    val etaInTime: StateFlow<String?> = _etaInTime.asStateFlow()

    data class NavigationUpdate(
        val directionText: String?,
        val directionDistance: String?,
        val iconType: String?,
        val etaDuration: String?,
        val etaDistance: String?,
        val etaTime: String?
    )

    //Keep track of when any nav data is updated
    val navigationDataChanged = combine(
        directionDistance, directionText, iconType,
        etaInDuration, etaInDistance, etaInTime
    ) { values ->
        NavigationUpdate(
            values[0], values[1], values[2], values[3], values[4], values[5]
        )
    }

    //Functions to set data values and extract information
    fun setDirectionDistance(value: String?) {
        if (value != "Starting navigation..." && value != null) {
            _directionDistance.value = value.trim().replaceFirstChar { it.uppercaseChar() }
            Log.d(TAG, "Direction distance: ${_directionDistance.value}")
        }
    }
    fun setDirectionText(value: String?) {
        if (value != null) {
            _directionText.value = value.trim()
            Log.d(TAG, "Direction Text: ${_directionText.value}")
        }
    }
    fun setDirectionIcon(value: Bitmap?) {
        _directionIcon.value = value
    }
    fun setIconType(value: String?) {
        if (value != null) {
            _iconType.value = value
            Log.d(TAG, "Icon type: ${_iconType.value}")
        }
    }
    fun setEtaInDuration(value: String?) {
        if (value != null) {
            _etaInDuration.value = value.trim()
            Log.d(TAG, "ETA in duration: ${_etaInDuration.value}")
        }
    }
    fun setEtaInDistance(value: String?) {
        if (value != null) {
            _etaInDistance.value = value.trim()
            Log.d(TAG, "ETA in distance: ${_etaInDistance.value}")
        }
    }
    fun setEtaInTime(value: String?) {
        if (value != null) {
            _etaInTime.value = value.trim().removeSuffix(" ETA")
            Log.d(TAG, "ETA in time: ${_etaInTime.value}")
        }
    }
}