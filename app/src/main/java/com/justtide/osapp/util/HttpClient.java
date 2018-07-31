package com.justtide.osapp.util;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient  {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final int HTTP_OK = 200;
    private static OkHttpClient client;

    private static OkHttpClient getHttpClient() {
        return new OkHttpClient
                .Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public static Response post(String url, String json) throws IOException {
        if (client == null) {
            client = getHttpClient();
        }
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return client.newCall(request).execute();
    }



    public static Response get(String url) throws IOException {

        if (client == null) {
            client = getHttpClient();
        }

        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();

        return response;
    }


}
