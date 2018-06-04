package com.jeff.sendmss;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduComposer;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.SendReq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static String TAG = "MMSSender";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            String currentAPN = info.getExtraInfo();
            Log.i("MMSSender", "========" + currentAPN );

            sendMMS(this);
        } catch (Exception e) {
            Log.i("MMSSender", "Error", e);
        }

    }

    public static void sendMMS(final Context context) {
        //final MMSInfo mmsInfo = new MMSInfo(context, number, subject, text, imagePath, audioPath);
        String subject = "Jefftset";
        String recipient = "15928594069";//138xxxxxxx
        final SendReq sendRequest = new SendReq();
        final EncodedStringValue[] sub = EncodedStringValue.extract(subject);
        if (sub != null && sub.length > 0) {
            sendRequest.setSubject(sub[0]);
        }
        final EncodedStringValue[] phoneNumbers = EncodedStringValue.extract(recipient);
        if (phoneNumbers != null && phoneNumbers.length > 0) {
            sendRequest.addTo(phoneNumbers[0]);
        }
        final PduBody pduBody = new PduBody();
        final PduPart partPdu = new PduPart();
        partPdu.setCharset(CharacterSets.UTF_8);//UTF_16
        partPdu.setName("jefftest".getBytes());
        partPdu.setContentType("image/png".getBytes());
        File file = new File(Environment.getExternalStorageDirectory() + "/images", "test.jpg");
        partPdu.setDataUri(FileProvider.getUriForFile(context, "com.jeff.sendmss.fileProvider", file));
/*        String furl = "file://mnt/sdcard//test.jpg";
        partPdu.setDataUri(Uri.parse(furl));*/
        pduBody.addPart(partPdu);
        sendRequest.setDate(System.currentTimeMillis() / 1000);
        sendRequest.setBody(pduBody);
        final PduComposer composer = new PduComposer(context, sendRequest);
        final byte[] bytesToSend = composer.make();
        if (bytesToSend ==null) {
            Log.i(TAG, "bytesToSend======" + null);
        }


        final List<String> list = APNManager.getSimMNC(context);
        new Thread() {
            @Override
            public void run() {
                try {
                        MMSSender.sendMMS(context,list, bytesToSend);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.i("MMSSender", "Error", e);
                }
            };
        }.start();
    }


    public void sendMMSByIntent() {
        try {
            String mobile="15928594069";
            String content="Jefftest";
            //发送彩信
            Log.i(TAG, Environment.getExternalStorageDirectory().toString());
            File file = new File(Environment.getExternalStorageDirectory() + "/images", "test.jpg");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line="";
            line = bufferedReader.readLine();
            while (line!= null) {
                Log.i(TAG,line);
                line=bufferedReader.readLine();
            }

            Uri uri=FileProvider.getUriForFile(this, "com.jeff.sendmss.fileProvider", file);   //图片路径
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra("address",mobile);              //邮件地址
            intent.putExtra("sms_body",content);            //邮件内容
            intent.putExtra(Intent.EXTRA_STREAM,uri);
            intent.setType("image/png");                    //设置类型
            MainActivity.this.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
