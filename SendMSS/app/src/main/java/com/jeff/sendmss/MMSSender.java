package com.jeff.sendmss;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.util.Log;

/**
 * @author
 * @version 创建时间：2012-2-1 上午09:32:54
 */
public class MMSSender {
    private static final String TAG = "MMSSender";
    // public static String mmscUrl = "http://mmsc.monternet.com";
    // public static String mmscProxy = "10.0.0.172";
    public static int mmsProt = 80;

    private static String HDR_VALUE_ACCEPT_LANGUAGE = "";
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

            client = new DefaultHttpClient(httpParams);

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
            APNManager.shouldChangeApnBack(context);
        }
        return new byte[0];
    }

}