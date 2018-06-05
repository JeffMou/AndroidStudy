package com.jeff.sendmss;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import static android.R.attr.host;

/**
 * @author
 * @version 创建时间：2012-2-1 上午09:32:54
 */
public class MMSSender {
    private static final String TAG = "MMSSender";
    // public static String mmscUrl = "http://mmsc.monternet.com";
    // public static String mmscProxy = "10.0.0.172";
    public static int mmsProt = 80;

    private static String HDR_VALUE_ACCEPT_LANGUAGE = HTTP.UTF_8;;
    private static final String HDR_KEY_ACCEPT = "Accept";
    private static final String HDR_KEY_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String HDR_VALUE_ACCEPT = "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";

    public static byte[] sendMMS(Context context, List<String> list, byte[] pdu)
            throws IOException {
        Log.i(TAG, "进入sendMMS方法");
        // HDR_VALUE_ACCEPT_LANGUAGE = getHttpAcceptLanguage();
        HDR_VALUE_ACCEPT_LANGUAGE = HTTP.UTF_8;

        String mmsUrl = (String) list.get(0);
        String mmsProxy = (String) list.get(1);
        Log.i(TAG, mmsUrl);
        Log.i(TAG, mmsProxy);
        if (mmsUrl == null) {
            throw new IllegalArgumentException("URL must not be null.");
        }
        HttpClient client = null;

        try {
            // Make sure to use a proxy which supports CONNECT.
            // client = HttpConnector.buileClient(context);

            HttpHost httpHost = new HttpHost(mmsProxy, mmsProt);
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);



            HttpPost post = new HttpPost(mmsUrl);
            // mms PUD START
            ByteArrayEntity entity = new ByteArrayEntity(pdu);
            entity.setContentType("application/vnd.wap.mms-message");
            post.setEntity(entity);
            post.addHeader(HDR_KEY_ACCEPT, HDR_VALUE_ACCEPT);
            post.addHeader(HDR_KEY_ACCEPT_LANGUAGE, HDR_VALUE_ACCEPT_LANGUAGE);
            post.addHeader(
                    "user-agent",
                    "Mozilla/5.0(Linux;U;Android 2.1-update1;zh-cn;ZTE-C_N600/ZTE-C_N600V1.0.0B02;240*320;CTC/2.0)AppleWebkit/530.17(KHTML,like Gecko) Version/4.0 Mobile Safari/530.17");
            // mms PUD END
            HttpParams params = client.getParams();
            HttpProtocolParams.setContentCharset(params, "UTF-8");

            Log.i(TAG, "准备执行发送");

            // PlainSocketFactory localPlainSocketFactory =
            // PlainSocketFactory.getSocketFactory();
            client = new DefaultHttpClient(httpParams);
            HttpResponse response = client.execute(post);

            Log.i(TAG, "执行发送结束， 等回执。。");

            StatusLine status = response.getStatusLine();
            Log.d(TAG, "status " + status.getStatusCode());
            if (status.getStatusCode() != 200) { // HTTP 200 表服务器成功返回网页
                Log.d(TAG, "!200");
                throw new IOException("HTTP error: " + status.getReasonPhrase());
            }
            HttpEntity resentity = response.getEntity();
            byte[] body = null;
            if (resentity != null) {
                try {
                    if (resentity.getContentLength() > 0) {
                        body = new byte[(int) resentity.getContentLength()];
                        DataInputStream dis = new DataInputStream(
                                resentity.getContent());
                        try {
                            dis.readFully(body);
                        } finally {
                            try {
                                dis.close();
                            } catch (IOException e) {
                                Log.e(TAG,
                                        "Error closing input stream: "
                                                + e.getMessage());
                            }
                        }
                    }
                } finally {
                    if (entity != null) {
                        entity.consumeContent();
                    }
                }
            }
            Log.d(TAG, "result:" + new String(body));

            Log.i(TAG, "成功！！" + new String(body));

            return body;
        } catch (IllegalStateException e) {
            Log.e(TAG, "", e);
            // handleHttpConnectionException(e, mmscUrl);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "", e);
            // handleHttpConnectionException(e, mmscUrl);
        } catch (SocketException e) {
            Log.e(TAG, "", e);
            // handleHttpConnectionException(e, mmscUrl);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            // handleHttpConnectionException(e, mmscUrl);
        } finally {
            if (client != null) {
                // client.;
            }
        }
        return new byte[0];
    }

    public static byte[] sendMMSNew(Context context, List<String> list, byte[] pdu)
            throws IOException {
        Log.i(TAG, "进入sendMMS方法");
        // HDR_VALUE_ACCEPT_LANGUAGE = getHttpAcceptLanguage();
        HDR_VALUE_ACCEPT_LANGUAGE = HTTP.UTF_8;

        String mmsUrl = (String) list.get(0);
        String mmsProxy = (String) list.get(1);
        Log.i(TAG, mmsUrl);
        Log.i(TAG, mmsProxy);
        if (mmsUrl == null) {
            throw new IllegalArgumentException("URL must not be null.");
        }




        try {
            Properties systemProperties = System.getProperties();
            systemProperties.setProperty("http.proxyHost", mmsProxy);
            systemProperties.setProperty("http.proxyPort", mmsPort);
            systemProperties.setProperty("http.keepAlive", "false");


            URL url = new URL(mmsUrl);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setConnectTimeout(10000);
            postConnection.setRequestMethod("POST"); // 以post请求方式提交
            postConnection.setRequestProperty("Content-Type", "application/vnd.wap.mms-message");
            postConnection.setRequestProperty("Content-Length", Integer.toString(3000));
            postConnection.setDoInput(true); // 读取数据
            postConnection.setDoOutput(true); // 向服务器写数据
            postConnection.connect();

            if (postConnection.getContentLength() >= 0) {

            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        return new byte[0];
    }




    //public static String mmscUrl = "http://mmsc.monternet.com";
    //public static String mmscUrl = "http://mmsc.myuni.com.cn";
    public static String mmscUrl = "http://mmsc.vnet.mobi";
    //public static String mmscUrl = "http://www.baidu.com";
    public static String mmsProxy = "10.0.0.172";
    public static String mmsPort = "80";

    //private static String HDR_VALUE_ACCEPT_LANGUAGE = "";

    private static final String HDR__KEY_ACCEPT = "Accept";
    //private static final String HDR_KEY_ACCEPT_LANGUAGE = "Accept-Language";

    //private static final String HDR_VALUE_ACCEPT ="*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";
    public static byte[] sendMMMS(Context context, byte[] pdu) throws Exception
    {
        //HDR_AVLUE_ACCEPT_LANGUAGE = getHttpAcceptLanguage();

        if (mmscUrl == null)
        {
            throw new IllegalAccessException("URL must not be null");
        }

        HttpClient client = null;
        try{
            //client = AndroidHttpClient.newInstance("Android-Mms/2.0");
            //client = HttpConnector.buileClient(context);
            URI hostUrl = new URI(mmscUrl);
            HttpHost target = new HttpHost(
                    hostUrl.getHost(), hostUrl.getPort(),
                    HttpHost.DEFAULT_SCHEME_NAME);

            client = AndroidHttpClient.newInstance("Android-Mms/2.0");

            HttpPost post = new HttpPost(mmscUrl);
            ByteArrayEntity entity = new ByteArrayEntity(pdu);
            entity.setContentType("application/vnd.wap.mms-message");
            post.setEntity(entity);
            post.addHeader(HDR__KEY_ACCEPT,HDR_VALUE_ACCEPT);
            post.addHeader(HDR_KEY_ACCEPT_LANGUAGE, HDR_VALUE_ACCEPT_LANGUAGE);

            HttpParams params = client.getParams();
            HttpProtocolParams.setContentCharset(params, "UTF-8");

            ConnRouteParams.setDefaultProxy(
                    params, new HttpHost(mmsProxy, 80));
            //req.setParams(params);
            HttpResponse response = client.execute(target,post);

            StatusLine status = response.getStatusLine();
            System.out.println("status : " + status.getStatusCode());
            if (status.getStatusCode() != 200)
            {
                Log.i(TAG, "!200");
                throw new IOException("HTTP error: " + status.getReasonPhrase());
            }

            HttpEntity resentity = response.getEntity();
            byte[] body = null;

            if (resentity != null)
            {
                try{
                    if (resentity.getContentLength() <= 0)
                    {
                        body  =  new byte[(int)resentity.getContentLength()];
                        DataInputStream dis = new DataInputStream(resentity.getContent());

                        try{
                            dis.readFully(body);
                        }finally{
                            try{
                                dis.close();
                            }catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }

                }finally
                {
                    if (entity != null)
                        entity.consumeContent();
                }
            }
            System.out.println("result : "+ new String(body));
            return body;

        }catch (Exception e)
        {
            Log.e(TAG, "", e);
            e.printStackTrace();
        }
        return new byte[0];
    }



    public static String sendPost(Context context, List<String> list, byte[] pdu){

        try {
            //建立连接
            String mmsUrl = (String) list.get(0);
            String mmsProxy = (String) list.get(1);

            URL url = new URL(mmsUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            //设置连接属性
            connection.setDoOutput(true); //使用URL连接进行输出
            connection.setDoInput(true); //使用URL连接进行输入
            connection.setUseCaches(false); //忽略缓存
            connection.setRequestMethod("POST"); //设置URL请求方法

            Properties systemProperties = System.getProperties();
            systemProperties.setProperty("http.proxyHost", mmsProxy);
            systemProperties.setProperty("http.proxyPort", mmsPort);
            systemProperties.setProperty("http.keepAlive", "false");

            connection.setRequestProperty("Content-length", "" + pdu.length);
            connection.setRequestProperty("Content-Type", "application/vnd.wap.mms-message");
            connection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            connection.setRequestProperty("Charset", "UTF-8");

            connection.setConnectTimeout(10000);
            //connection.setReadTimeout(10000);

            //建立输出流,并写入数据
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(pdu);
            outputStream.close();

            //获取响应状态
            int responseCode = connection.getResponseCode();

            if (HttpURLConnection.HTTP_OK == responseCode) { //连接成功
                //当正确响应时处理数据
                StringBuffer buffer = new StringBuffer();
                String readLine;
                BufferedReader responseReader;
                //处理响应流
                responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((readLine = responseReader.readLine()) != null) {
                    buffer.append(readLine).append("\n");
                }
                responseReader.close();
                Log.d(TAG, buffer.toString());
                return buffer.toString();//成功
            }
        }catch (Exception e){
            Log.e(TAG, "", e);
            e.printStackTrace();
        }
        return "Fail";//失败

    }

}