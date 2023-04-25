package nl.rijksoverheid.ctr.holder.utils

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import androidx.annotation.IdRes
import androidx.navigation.Navigation
import nl.rijksoverheid.ctr.holder.R

interface CameraUtil {
    fun openScanner(activity: Activity, @IdRes destinationId: Int, onError: () -> Unit)
}

class CameraUtilImpl : CameraUtil {
    override fun openScanner(activity: Activity, @IdRes destinationId: Int, onError: () -> Unit) {
        try {
            val cameraManager =
                activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            if (cameraManager.cameraIdList.isNotEmpty()) {
                Navigation.findNavController(activity, R.id.main_nav_host_fragment)
                    .navigate(destinationId)
            } else {
                onError()
            }
        } catch (exception: CameraAccessException) {
            onError()
        }
    }
}
