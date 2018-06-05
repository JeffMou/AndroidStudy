package com.jeff.sendmss;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.util.Locale;

/**
 * Created by Administrator on 2018/6/5.
 */

public class MmsHttpClient {
    static final String TAG = "MMSSender";
    static final String METHOD_POST = "POST";
    static final String METHOD_GET = "GET";

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String HEADER_USER_AGENT = "User-Agent";

    // The "Accept" header value
    private static final String HEADER_VALUE_ACCEPT =
            "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";
    // The "Content-Type" header value
    private static final String HEADER_VALUE_CONTENT_TYPE_WITH_CHARSET =
            "application/vnd.wap.mms-message; charset=utf-8";
    private static final String HEADER_VALUE_CONTENT_TYPE_WITHOUT_CHARSET =
            "application/vnd.wap.mms-message";

    /*
     * Macro names
     */
    // The raw phone number
    private static final String MACRO_LINE1 = "LINE1";
    // The phone number without country code
    private static final String MACRO_LINE1NOCOUNTRYCODE = "LINE1NOCOUNTRYCODE";
    // NAI (Network Access Identifier)
    private static final String MACRO_NAI = "NAI";

    // The possible NAI system property name
    private static final String NAI_PROPERTY = "persist.radio.cdma.nai";

    private final Context mContext;
    private final TelephonyManager mTelephonyManager;

    public MmsHttpClient() {
        mContext = null;
        mTelephonyManager = null;
    }


    /**
     * Execute an MMS HTTP request, either a POST (sending) or a GET (downloading)
     *
     * @param urlString The request URL, for sending it is usually the MMSC, and for downloading
     *                  it is the message URL
     * @param pdu For POST (sending) only, the PDU to send
     * @param method HTTP method, POST for sending and GET for downloading
     * @param proxyHost The proxy host
     * @param proxyPort The proxy port
     * @param userAgent The user agent header value
     * @param uaProfUrl The UA Prof URL header value
     * @return The HTTP response body
     * @throws Exception For any failures
     */
    public static byte[] execute(String urlString, byte[] pdu, String method, String proxyHost, int proxyPort, String userAgent, String uaProfUrl)
            throws Exception {
        checkMethod(method);
        HttpURLConnection connection = null;
        try {
            userAgent = "Mozilla/5.0 (Linux; U; Android 8.0.0; en-us; HTC M10h Build/OPR1.170623.027) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.0.0 Mobile Safari/537.36";
            //Proxy proxy = Proxy.NO_PROXY;
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

            final URL url = new URL(urlString);
            // Now get the connection
            connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setDoInput(true);
            connection.setConnectTimeout(10*1000);
            // ------- COMMON HEADERS ---------
            // Header: Accept
            connection.setRequestProperty(HEADER_ACCEPT, HEADER_VALUE_ACCEPT);
            // Header: Accept-Language
            connection.setRequestProperty(
                    HEADER_ACCEPT_LANGUAGE, getCurrentAcceptLanguage(Locale.getDefault()));
            // Header: User-Agent
            Log.i(TAG, "HTTP: User-Agent=" + userAgent);
            connection.setRequestProperty(HEADER_USER_AGENT, userAgent);
            // Header: x-wap-profile
/*            final String uaProfUrlTagName = "x-wap-profile";
            if (uaProfUrl != null) {
                Log.i(TAG, "HTTP: UaProfUrl=" + uaProfUrl);
                connection.setRequestProperty(uaProfUrlTagName, uaProfUrl);
            }*/
            // Add extra headers specified by mms_config.xml's httpparams
            //addExtraHeaders(connection, mmsConfig);

            // Different stuff for GET and POST
            if (METHOD_POST.equals(method)) {
                if (pdu == null || pdu.length < 1) {
                    Log.e(TAG, "HTTP: empty pdu");
                    throw new Exception("Sending empty PDU");
                }
                connection.setDoOutput(true);
                connection.setRequestMethod(METHOD_POST);
                if (true) {
                    connection.setRequestProperty(HEADER_CONTENT_TYPE,
                            HEADER_VALUE_CONTENT_TYPE_WITH_CHARSET);
                } else {
                    connection.setRequestProperty(HEADER_CONTENT_TYPE,
                            HEADER_VALUE_CONTENT_TYPE_WITHOUT_CHARSET);
                }
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    //logHttpHeaders(connection.getRequestProperties());
                }
                connection.setFixedLengthStreamingMode(pdu.length);
                // Sending request body
                final OutputStream out =
                        new BufferedOutputStream(connection.getOutputStream());
                out.write(pdu);
                out.flush();
                out.close();
            } else if (METHOD_GET.equals(method)) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    //logHttpHeaders(connection.getRequestProperties());
                }
                connection.setRequestMethod(METHOD_GET);
            }
            // Get response
            final int responseCode = connection.getResponseCode();
            final String responseMessage = connection.getResponseMessage();
            Log.d(TAG, "HTTP: " + responseCode + " " + responseMessage);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                //logHttpHeaders(connection.getHeaderFields());
            }
            if (responseCode / 100 != 2) {
                throw new Exception("response code error");
            }
            final InputStream in = new BufferedInputStream(connection.getInputStream());
            final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            final byte[] buf = new byte[4096];
            int count = 0;
            while ((count = in.read(buf)) > 0) {
                byteOut.write(buf, 0, count);
            }
            in.close();
            final byte[] responseBody = byteOut.toByteArray();
            Log.d(TAG, "HTTP: response size="
                    + (responseBody != null ? responseBody.length : 0));
            return responseBody;
        } catch (MalformedURLException e) {
            Log.e(TAG, "HTTP: invalid URL ", e);
        } catch (ProtocolException e) {
            Log.e(TAG, "HTTP: invalid URL protocol ", e);
        } catch (IOException e) {
            Log.e(TAG, "HTTP: IO failure", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new byte[0];
    }

    private static void checkMethod(String method) throws Exception {
        if (!METHOD_GET.equals(method) && !METHOD_POST.equals(method)) {
            throw new Exception("Invalid method " + method);
        }
    }

    private static final String ACCEPT_LANG_FOR_US_LOCALE = "en-US";

    /**
     * Return the Accept-Language header.  Use the current locale plus
     * US if we are in a different locale than US.
     * This code copied from the browser's WebSettings.java
     *
     * @return Current AcceptLanguage String.
     */
    public static String getCurrentAcceptLanguage(Locale locale) {
        final StringBuilder buffer = new StringBuilder();
        addLocaleToHttpAcceptLanguage(buffer, locale);

        if (!Locale.US.equals(locale)) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(ACCEPT_LANG_FOR_US_LOCALE);
        }

        return buffer.toString();
    }

    private static void addLocaleToHttpAcceptLanguage(StringBuilder builder, Locale locale) {
        final String language = convertObsoleteLanguageCodeToNew(locale.getLanguage());
        if (language != null) {
            builder.append(language);
            final String country = locale.getCountry();
            if (country != null) {
                builder.append("-");
                builder.append(country);
            }
        }
    }

    /**
     * Convert obsolete language codes, including Hebrew/Indonesian/Yiddish,
     * to new standard.
     */
    private static String convertObsoleteLanguageCodeToNew(String langCode) {
        if (langCode == null) {
            return null;
        }
        if ("iw".equals(langCode)) {
            // Hebrew
            return "he";
        } else if ("in".equals(langCode)) {
            // Indonesian
            return "id";
        } else if ("ji".equals(langCode)) {
            // Yiddish
            return "yi";
        }
        return langCode;
    }

}
