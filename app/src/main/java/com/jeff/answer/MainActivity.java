package com.jeff.answer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    ITelephony mTelephony;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
        String[] permissions = {Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.MODIFY_PHONE_STATE};
        ActivityCompat.requestPermissions(this,permissions, 321);
        Method method = null;
        IBinder binder = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            binder = (IBinder) method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
        } catch (Exception e) {
            Log.e(TAG, "Error msg:" + e);
        }
        mTelephony = ITelephony.Stub.asInterface(binder);*/
    }

/*    class PhoneListener extends PhoneStateListener {
            private boolean flag = false;
            private String phoneNum = null;

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                // TODO Auto-generated method stub
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        try {
                            if (flag) {
                                Thread.sleep(5000);
                                mTelephony.call("com.jeff.answer", phoneNum);
                                flag = false;
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Log.i(TAG, "====================",e);
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        //来电自动接听
                        try {
                            Log.i(TAG, "====================" + "CALL_STATE_RINGING");
*//*                        TelecomManager telecomManager =
                                (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                        Method method = Class.forName("android.telecom.TelecomManager").getMethod("acceptRingingCall");
                        method.invoke(telecomManager);
                        Thread.sleep(6000);
                        Method method = Class.forName("android.telecom.TelecomManager").getMethod("endCall");
                        method.invoke(telecomManager);
                        Thread.sleep(6000);*//*
                            phoneNum = incomingNumber;
                            mTelephony.endCall();
                            flag=true;
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Log.i(TAG, "====================",e);
                        }
                        break;
                    default:
                        break;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
    }*/
}
