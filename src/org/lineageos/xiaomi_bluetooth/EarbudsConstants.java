package org.lineageos.xiaomi_bluetooth;

import android.os.ParcelUuid;


public class EarbudsConstants {

    public static final int MANUFACTURER_ID_XIAOMI = 0x038F;
    public static final ParcelUuid UUID_XIAOMI_FAST_CONNECT
            = ParcelUuid.fromString("0000FD2D-0000-1000-8000-00805f9b34fb");
    public static final int XIAOMI_MMA_DATA_LENGTH = 22;

    public static final int EARBUDS_CHARGING_BIT_MASK = 128;
    public static final int EARBUDS_BATTERY_LEVEL_MASK = 0x7F;

    public static final int SCAN_REPORT_DELAY = 1000;

}
