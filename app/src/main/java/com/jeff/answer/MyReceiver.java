package com.jeff.answer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "AnswerCall";
    private static String phoneNum = null;
    private static Boolean isCall = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
        } else {
            Method method;
            IBinder binder = null;
            try {
                method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
                binder = (IBinder) method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
            } catch (Exception e) {
                Log.e(TAG, "Error msg:" + e);
            }
            ITelephony mTelephony = ITelephony.Stub.asInterface(binder);

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            switch (telephonyManager.getCallState()) {
                case CALL_STATE_RINGING:
                    phoneNum = intent.getStringExtra("incoming_number");
                    Log.i(TAG, "====================" + phoneNum);
                    try {
                        isCall = true;
                        mTelephony.endCall();
                    } catch (RemoteException e) {
                        Log.i(TAG, "====================",e);
                    }
                    break;
                case CALL_STATE_IDLE:
                    if (isCall) {
                        try {
                            Thread.sleep(10000);
                            Log.i(TAG, "====================" + phoneNum);
                            mTelephony.call("com.jeff.answer", phoneNum);
                        } catch (Exception e) {
                            Log.i(TAG, "====================",e);
                        }
                        isCall = false;
                    }

                    break;
                default:
                    break;
            }
        }
    }
}
