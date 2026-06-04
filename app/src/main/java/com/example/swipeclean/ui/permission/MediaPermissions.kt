package com.example.swipeclean.ui.permission

import android.Manifest
import android.os.Build
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus

/** Shared media read permission sets — keep onboarding + gate identical. */
fun mediaReadPermissions(): List<String> = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> listOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> listOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO
    )
    else -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.mediaAccessGranted(): Boolean =
    permissions.any { it.status is PermissionStatus.Granted }

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.shouldOpenAppSettingsForMedia(): Boolean {
    if (permissions.any { it.status is PermissionStatus.Granted }) return false
    if (permissions.isEmpty()) return false
    return permissions.all { perm ->
        val status = perm.status
        status is PermissionStatus.Denied && !status.shouldShowRationale
    }
}
