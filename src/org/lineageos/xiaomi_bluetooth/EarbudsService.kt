package org.lineageos.xiaomi_bluetooth

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.IntentCompat
import org.lineageos.xiaomi_bluetooth.utils.ATUtils
import org.lineageos.xiaomi_bluetooth.utils.BluetoothUtils.getBluetoothAdapter

class EarbudsService : Service() {

    private var bluetoothHeadset: BluetoothHeadset? = null

    private val profileListener: BluetoothProfile.ServiceListener =
        object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile != BluetoothProfile.HEADSET) {
                    return
                }

                if (DEBUG) Log.d(TAG, "Bluetooth headset connected: $proxy")
                bluetoothHeadset = proxy as BluetoothHeadset
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile != BluetoothProfile.HEADSET) {
                    return
                }

                if (DEBUG) Log.d(TAG, "Bluetooth headset disconnected")
                bluetoothHeadset = null
            }
        }

    private val bluetoothStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == null) return
            if (DEBUG) Log.d(TAG, "onReceive: ${intent.action}")
            val device = IntentCompat.getParcelableExtra(
                intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
            )
            if (device == null) {
                if (DEBUG) Log.d(TAG, "onReceive: Received intent with null device")
                return
            }
            if (!device.isConnected) {
                if (DEBUG) Log.d(TAG, "onReceive: Device is not connected")
                return
            }
            if (bluetoothHeadset == null) {
                Log.w(TAG, "onReceive: bluetoothHeadset is null")
                return
            }

            when (intent.action) {
                BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT ->
                    handleATCommand(device, intent)

                else -> Log.w(TAG, "unknown action: ${intent.action}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (DEBUG) Log.d(TAG, "onCreate")

        initializeProfileProxy()
        startBluetoothStateListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (DEBUG) Log.d(TAG, "onDestroy")

        closeProfileProxy()
        stopBluetoothStateListening()
    }

    override fun onBind(intent: Intent) = null

    @SuppressLint("MissingPermission")
    private fun handleATCommand(device: BluetoothDevice, intent: Intent) {
        val args = IntentCompat.getParcelableExtra(
            intent,
            BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS,
            Array<Any>::class.java
        )
        if ((args?.size ?: 0) > 1) {
            Log.w(TAG, "handleATCommand: Not valid args size: ${args?.size}, send update")

            if (bluetoothHeadset != null) {
                ATUtils.sendUpdateATCommand(bluetoothHeadset!!, device)
            }
            return
        }

        runCatching {
            ATUtils.parseATCommandIntent(intent)
        }.onSuccess { earbuds ->
            earbuds?.updateDeviceTypeMetadata()
            earbuds?.updateDeviceBatteryMetadata()
        }.onFailure {
            Log.e(TAG, "handleATCommand: Unable to parse at command intent", it)
        }
    }

    private fun initializeProfileProxy() {
        getBluetoothAdapter().getProfileProxy(this, profileListener, BluetoothProfile.HEADSET)
    }

    private fun closeProfileProxy() {
        bluetoothHeadset?.run {
            getBluetoothAdapter().closeProfileProxy(BluetoothProfile.HEADSET, this)
            bluetoothHeadset = null
        }
    }

    private fun startBluetoothStateListening() {
        if (DEBUG) Log.d(TAG, "startBluetoothStateListening")

        val filter = IntentFilter().apply {
            // Xiaomi AT event
            addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)
            addCategory(
                BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY
                        + "." + ATUtils.MANUFACTURER_ID_XIAOMI
            )
        }

        if (DEBUG) Log.d(TAG, "registering bluetooth state receiver")
        registerReceiver(bluetoothStateReceiver, filter)
    }

    private fun stopBluetoothStateListening() {
        if (DEBUG) Log.d(TAG, "stopBluetoothStateListening")

        unregisterReceiver(bluetoothStateReceiver)
    }

    companion object {
        private val TAG = EarbudsService::class.java.simpleName
        private const val DEBUG = true
    }
}
