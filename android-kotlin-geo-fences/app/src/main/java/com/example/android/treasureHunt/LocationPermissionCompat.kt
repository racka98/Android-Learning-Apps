package com.example.android.treasureHunt

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LocationPermissionCompat(private val context: Context) {

    companion object {
        private val isRunningQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

        //Title and message content for dialog box requesting user to access the permissions
        //This can also be set from a string resource
        private const val ALERT_TITLE = "Background Location Permissions Request"
        private const val ALERT_MESSAGE = "Allow background location permissions by " +
                "selecting \"Allow all the time\" so that you can be notified about Geofence areas"
    }
    private val activity = context as Activity

    /*
     * In all cases, we need to have the location permission.  On Android 10+ (Q) we need to have
     * the background permission as well.
     */
    fun requestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        val currentRequestCode = REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE or REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        if (requestCode == currentRequestCode) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                requestForegroundAndBackGroundLocationPermissions()
                return

            }
        }
    }

    /*
     *  Determines whether the app has the appropriate permissions across Android 10+ and all other
     *  Android versions.
     */
    @TargetApi(29)
    fun isForegroundAndBackgroundPermissionApproved(): Boolean {
        if (isRunningQ) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /*
     *  Requests ACCESS_FINE_LOCATION and on Android 10+ (Q) ACCESS_BACKGROUND_LOCATION.
     */
    @TargetApi(29)
    fun requestForegroundAndBackGroundLocationPermissions() {
        /*
         * Request Foreground Location permission then if it's running Android Q+
         * request background permission too
         *
         * To access Background location on Android Q+ you must first request
         * foreground location then you can show a dialog to box to allow the
         * user to be redirected to the settings page where they can allow
         * background location permission all the time
         *
         * The user will not be directed to location permission settings if you don't
         * request foreground location first
         */

        //Foreground Location Permission Request
        requestForegroundLocationPermission()

        //Background Location Permission Request
        if (!isForegroundAndBackgroundPermissionApproved()) {
            if (isRunningQ) {
                AlertDialog.Builder(context).apply {
                    setTitle(ALERT_TITLE)
                    setMessage(ALERT_MESSAGE)
                    setPositiveButton("Allow") { _, _ ->
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ),
                            REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                        )
                    }
                    setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                }.create()
                    .show()
            }
        }
    }

    //For use to request Foreground Location permissions only
    //Remove private modifier to access it outside this class
    private fun requestForegroundLocationPermission() {
        //Requesting foreground location only
        if (!isForegroundAndBackgroundPermissionApproved()) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }
}