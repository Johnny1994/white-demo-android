package com.herewhite.demo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by buhe on 2018/8/16.
 */

public class DemoAPI {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String sdkToken = "请在 https://console.herewhite.com 中注册";
    private static final String host = "https://cloudcapiv4.herewhite.com";

    OkHttpClient client = new OkHttpClient();
    Gson gson = new Gson();

    public boolean validateToken() {
        return sdkToken.length() > 200;
    }

    public interface Result {
        void success(String uuid, String roomToken);
        void fail(String message);
    }

    public void getRoom(final Result result) {
        createRoom("test room", 100, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                result.fail("网络请求错误: " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.code() == 200) {
                        JsonObject room = gson.fromJson(response.body().string(), JsonObject.class);
                        String uuid = room.getAsJsonObject("msg").getAsJsonObject("room").get("uuid").getAsString();
                        String roomToken = room.getAsJsonObject("msg").get("roomToken").getAsString();
                        result.success(uuid, roomToken);
                    } else {
                        result.fail("网络请求错误" + response.body().string());
                    }
                } catch (Throwable e) {
                    result.fail("创建房间失败" + e.toString());
                }
            }
        });
    }

    public void createRoom(String name, int limit, Callback callback) {
        Map<String, Object> roomSpec = new HashMap<>();
        roomSpec.put("name", name);
        roomSpec.put("limit", limit);
        roomSpec.put("mode", "historied");
        RequestBody body = RequestBody.create(JSON, gson.toJson(roomSpec));
        Request request = new Request.Builder()
                .url(host + "/room?token=" + sdkToken)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public void getRoomToken(String uuid, Callback callback) {
        Map<String, Object> roomSpec = new HashMap<>();
        RequestBody body = RequestBody.create(JSON, gson.toJson(roomSpec));
        Request request = new Request.Builder()
                .url(host + "/room/join?uuid=" + uuid + "&token=" + sdkToken)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    static String TEST_UUID = "test";
    static String TEST_ROOM_TOKEN = "test";
}
