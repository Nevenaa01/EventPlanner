package com.example.eventplanner.services;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FCMHttpClient {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    public void sendMessageToTopic(String serverKey, String topic, String jsonPayload) {
        RequestBody body = RequestBody.create(jsonPayload, JSON);
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(body)
                .addHeader("Authorization", "key=" + serverKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Handle success
                String responseData = response.body().string();
                System.out.println(responseData);
            }
        });
    }
}