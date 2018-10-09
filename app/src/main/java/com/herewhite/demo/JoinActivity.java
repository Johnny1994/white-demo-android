package com.herewhite.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteBroadView;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.herewhite.demo.MainActivity.EVENT_NAME;

public class JoinActivity extends AppCompatActivity {

    WhiteBroadView whiteBroadView;
    Gson gson = new Gson();
    DemoAPI demoAPI = new DemoAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        Intent intent = getIntent();
        final String uuid = intent.getStringExtra("uuid");

        whiteBroadView = (WhiteBroadView) findViewById(R.id.joinWhite);
        this.demoAPI.joinRoom(uuid, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.i("white", res);
                JsonObject room = gson.fromJson(res, JsonObject.class);
                String roomToken = room.getAsJsonObject("msg").get("roomToken").getAsString();
                Log.i("white", roomToken);
                joinRoom(uuid, roomToken);
            }
        });

    }

    private void joinRoom(String uuid, String roomToken) {
        WhiteSdk whiteSdk = new WhiteSdk(
                whiteBroadView,
                JoinActivity.this,
                new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1));
        whiteSdk.addRoomCallbacks(new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                showToast(phase.name());
                // handle room phase
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
//                showToast(gson.toJson(modifyState));
            }
        });
        whiteSdk.joinRoom(new RoomParams(uuid, roomToken), new Promise<Room>() {
            @Override
            public void then(Room room) {
                room.addMagixEventListener(EVENT_NAME, new EventListener() {
                    @Override
                    public void onEvent(EventEntry eventEntry) {
                        showToast(gson.toJson(eventEntry));
                    }
                });
//                GlobalState globalState = new GlobalState();
//                globalState.setCurrentSceneIndex(1);
//                room.setGlobalState(globalState);
//
//                room.getBroadcastState(new Promise<BroadcastState>() {
//                    @Override
//                    public void then(BroadcastState broadcastState) {
//                        showToast(broadcastState.getMode());
//                    }
//
//                    @Override
//                    public void catchEx(SDKError t) {
//                        showToast(t.getMessage());
//                    }
//                });

            }

            @Override
            public void catchEx(SDKError t) {
                showToast(t.getMessage());
            }
        });
    }

    void showToast(Object o) {
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }


}
