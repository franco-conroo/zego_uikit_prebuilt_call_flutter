package com.zegocloud.uikit.call_plugin.voip;

import android.content.ComponentName;
import android.content.Context;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;

public class PhoneAccountHelper {
    private static final String TAG = "PhoneAccountHelper";
    static final String PHONE_ACCOUNT_ID = "zego_voip_account";

    public static PhoneAccountHandle getPhoneAccountHandle(Context context) {
        ComponentName componentName = new ComponentName(context, VoipConnectionService.class);
        return new PhoneAccountHandle(componentName, PHONE_ACCOUNT_ID);
    }

    public static void registerPhoneAccount(Context context) {
        try {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager == null) {
                Log.e(TAG, "TelecomManager is null");
                return;
            }
            PhoneAccountHandle handle = getPhoneAccountHandle(context);
            PhoneAccount phoneAccount = PhoneAccount.builder(handle, "Zego VoIP")
                    .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
                    .build();
            telecomManager.registerPhoneAccount(phoneAccount);
            Log.d(TAG, "PhoneAccount registered");
        } catch (Exception e) {
            Log.e(TAG, "Failed to register PhoneAccount: " + e.getMessage());
        }
    }
}
