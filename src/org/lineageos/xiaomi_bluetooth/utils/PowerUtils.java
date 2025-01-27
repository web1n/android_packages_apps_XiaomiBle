package org.lineageos.xiaomi_bluetooth.utils;

import android.content.Context;
import android.os.PowerManager;


public class PowerUtils {

    public static final String TAG = PowerUtils.class.getName();
    public static final boolean DEBUG = true;


    public static boolean isInteractive(Context context) {
        if (context == null) {
            return false;
        }

        return context.getSystemService(PowerManager.class).isInteractive();
    }

}
