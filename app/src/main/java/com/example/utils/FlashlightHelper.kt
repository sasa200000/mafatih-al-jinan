package com.example.utils

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class FlashlightHelper(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraId: String? = null
    var isFlashSupported: Boolean = false
        private set

    init {
        try {
            cameraManager?.let { manager ->
                for (id in manager.cameraIdList) {
                    val characteristics = manager.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraId = id
                        isFlashSupported = true
                        break
                    }
                }
            }
        } catch (_: Exception) {
            isFlashSupported = false
        }
    }

    fun setTorchMode(enabled: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager?.setTorchMode(id, enabled)
        } catch (_: CameraAccessException) {
            // Camera in use or unavailable
        } catch (_: Exception) {
            // Device specific error
        }
    }
}
