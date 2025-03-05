package org.lineageos.xiaomi_bluetooth.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.util.Log

object PermissionUtils {

    private val TAG = PermissionUtils::class.java.simpleName
    private const val DEBUG = true

    fun Context.getMissingRuntimePermissions(): Array<String> {
        return runCatching {
            packageManager
                .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions
        }.onFailure {
            Log.e(TAG, "getMissingRuntimePermissions: ", it)
        }.getOrNull()?.filter { permission ->
            isRuntimePermission(permission) && checkSelfPermissionGranted(permission)
        }?.also {
            if (DEBUG) Log.d(TAG, "getMissingRuntimePermissions: ${it.joinToString()}");
        }?.toTypedArray() ?: arrayOf()
    }

    private fun Context.checkSelfPermissionGranted(permissionName: String): Boolean {
        return checkSelfPermission(permissionName) != PackageManager.PERMISSION_GRANTED
    }

    private fun Context.isRuntimePermission(permissionName: String): Boolean {
        return runCatching {
            (packageManager.getPermissionInfo(permissionName, 0).protection
                    == PermissionInfo.PROTECTION_DANGEROUS)
        }.getOrDefault(false)
    }

}
