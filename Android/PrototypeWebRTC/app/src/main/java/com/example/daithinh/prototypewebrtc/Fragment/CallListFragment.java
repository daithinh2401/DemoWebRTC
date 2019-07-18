package com.example.daithinh.prototypewebrtc.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daithinh.prototypewebrtc.Activity.MainActivity;
import com.example.daithinh.prototypewebrtc.Adapter.CustomAdapter;
import com.example.daithinh.prototypewebrtc.Manager.ScreenManager;
import com.example.daithinh.prototypewebrtc.Manager.SocketManager;
import com.example.daithinh.prototypewebrtc.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CallListFragment extends Fragment implements CustomAdapter.IViewHolderClick {

    private static final String GET_USERS_MESSAGE                   = "users_from_server";
    private static final String NEW_USER_JOIN                       = "new_user_join";
    private static final String USER_HAS_LEFT                       = "user_has_left";
    private static final String USER_WANT_CONNECT                   = "wantconnect";

    Context mContext;
    View mRootView;
    RecyclerView listView;

    TextView textViewListUser;
    ArrayList<String> listUser = new ArrayList<>();
    CustomAdapter adapter;

    SocketManager mSocketManager;
    Socket socket;

    ScreenManager mScreenManager;

    LinearLayoutManager layoutManager;

    String myName = "";

    public CallListFragment() {
    }

    @SuppressLint("ValidFragment")
    public CallListFragment(Context context) {
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.call_list_fragment, container, false);
        listView = mRootView.findViewById(R.id.listView);

        mScreenManager = ScreenManager.getInstance();
        mSocketManager = SocketManager.getInstance();
        socket = mSocketManager.getSocket();

        myName = mScreenManager.mSelfName;

        textViewListUser = mRootView.findViewById(R.id.textViewListUser);
        textViewListUser.setText(Html.fromHtml("Hello " + myName + " , choose your friends in list to make video call !"));

        adapter = new CustomAdapter(listUser, this);

        layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        registerSocketCallback();
        getListUsers();
    }

    @Override
    public void onPause() {
        super.onPause();

        removeSocketCallback();
    }

    private ArrayList<String> parseListFromServer(JSONArray jsonArray) {
        ArrayList<String> list = new ArrayList<>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add((String) jsonArray.get(i));
            }
        } catch (JSONException e) {
            Log.e("TAG", "MyDemo.CallListFragment.parseListFromServer(): Failed , exception " + e.toString());
            list = null;
        }

        return list;
    }

    private void updateList(JSONArray jsonArray){
        final ArrayList<String> list = parseListFromServer(jsonArray);
        if(list != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listUser = list;
                    adapter.updateList(listUser);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private Emitter.Listener onListUserResponse = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            JSONArray jsonArray = (JSONArray) objects[0];
            updateList(jsonArray);
        }
    };

    private Emitter.Listener onNewUserJoin = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            JSONArray jsonArray = (JSONArray) objects[0];
            updateList(jsonArray);
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            JSONArray jsonArray = (JSONArray) objects[0];
            updateList(jsonArray);
        }
    };

    private Emitter.Listener onUserWantConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... objects) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) objects[0];
                    final JSONObject jsonObject = new JSONObject();
                    String otherId = "";
                    String caller = "";
                    try {
                        otherId = object.getString("id");
                        caller = object.getString("caller");
                        jsonObject.put("id" , otherId);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle("Call Video");
                    alertDialog.setMessage(caller + " want to call you, accept ?");
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE  , "YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            socket.emit("acceptconnect" , jsonObject);

                            // Open video call screen
                            mScreenManager.openScreen(ScreenManager.SCREEN_TYPE_VIDEO_CALL);
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE  , "NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            socket.emit("unacceptconnect" , jsonObject);
                        }
                    });

                    alertDialog.show();
                }
            });
        }
    };

    private void registerSocketCallback() {
        Socket socket = SocketManager.getInstance().getSocket();

        socket.on(GET_USERS_MESSAGE, onListUserResponse);
        socket.on(NEW_USER_JOIN, onNewUserJoin);
        socket.on(USER_HAS_LEFT, onUserLeft);
        socket.on(USER_WANT_CONNECT, onUserWantConnect);
    }

    private void removeSocketCallback() {
        Socket socket = SocketManager.getInstance().getSocket();

        socket.off(GET_USERS_MESSAGE, onListUserResponse);
        socket.off(NEW_USER_JOIN, onNewUserJoin);
        socket.off(USER_HAS_LEFT, onUserLeft);
        socket.off(USER_WANT_CONNECT, onUserWantConnect);
    }

    private void getListUsers() {
        JSONObject jsonObject = createObjectForGetUsers();
        if (jsonObject != null) {
            mSocketManager.sendToSocket("get_users", jsonObject);
        }
    }

    private JSONObject createObjectForGetUsers() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", myName);
        } catch (JSONException e) {
            Log.e("TAG", "MyDemo.CallListFragment.getListUsers(): Failed , exception " + e.toString());
            object = null;
        }

        return object;
    }

    @Override
    public void onItemClick(int position) {
        JSONObject object = new JSONObject();
        String otherId = listUser.get(position);
        if(!otherId.equals(myName)) {

            try {
                object.put("id", otherId);
                object.put("caller" , myName);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            socket.emit("send", object);
            mScreenManager.openScreen(ScreenManager.SCREEN_TYPE_VIDEO_CALL);
        }
        else {
            Toast.makeText(mContext, "Cannot call your self", Toast.LENGTH_SHORT).show();
        }
    }
}
