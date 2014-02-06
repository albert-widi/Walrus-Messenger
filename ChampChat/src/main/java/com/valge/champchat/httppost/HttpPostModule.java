package com.valge.champchat.httppost;

import com.valge.champchat.util.HttpPostUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Albert Widiatmoko on 07/12/13.
 */
public class HttpPostModule {
    //do http post
    final HttpClient httpClient;
    final HttpPost httpPost;
    JSONObject json;
    JSONObject jsonResponse = null;

    public HttpPostModule() {
        HttpParams httpParams = new BasicHttpParams();
        //set timeout connect
        int timeOutConnection = 2500;
        HttpConnectionParams.setConnectionTimeout(httpParams, timeOutConnection);
        //set timeout socket
        int timeOutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParams, timeOutSocket);

        httpClient = new DefaultHttpClient(httpParams);
        httpPost = new HttpPost(HttpPostUtil.postURL);
        json = new JSONObject();
    }

    public JSONObject echatHttpPost(String action, String[] data, String[] dataName) {
        try {
            int dataLength = data.length;
            int dataNameLength = dataName.length;

            if(dataLength != dataNameLength) {
                jsonResponse = new JSONObject();
                jsonResponse.put("message", "ERROR");
            }
            else {
                json.put("action", action);
                for(int i = 0; i < dataLength; i++) {
                    json.put(dataName[i], data[i]);
                }

                List<NameValuePair> pairs = new ArrayList<NameValuePair>(dataLength);
                pairs.add(new BasicNameValuePair("data", json.toString()));

                httpPost.setEntity(new UrlEncodedFormEntity(pairs));

                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity resEntity = response.getEntity();

                String stringResponse = EntityUtils.toString(resEntity);
                System.out.println("String response :" + stringResponse);
                jsonResponse = new JSONObject(stringResponse);
                System.out.println("JSON : " + jsonResponse.toString());
            }
        }
        catch (Exception e) {
            System.out.println("Masuk ke exception");
            //e.printStackTrace();
            if(jsonResponse != null) {
                try {
                    jsonResponse.put("message", "ERROR");
                    return jsonResponse;
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            else {
                try {
                    jsonResponse = new JSONObject();
                    jsonResponse.put("message", "ERROR");
                    return jsonResponse;
                }
                catch(Exception exx) {
                    exx.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        return jsonResponse;
    }
}
