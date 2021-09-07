package com.hrl.chaui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.R;
import com.hrl.chaui.adapter.NewFriendAdapter;
import com.hrl.chaui.fragment.ContactFragment;
import com.hrl.chaui.util.Is;
import com.hrl.chaui.util.MqttService;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.hrl.chaui.MyApplication.friendRequest;
import static com.hrl.chaui.MyApplication.modifyUser;

public class NewFriendActivity extends AppCompatActivity {
    MqttServiceConnection connection;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    public TextView add;
    public SearchView mSearchView;
    public View search1,search2;
    public TextView back_arrow;
    private RecyclerView mRv;
    private NewFriendAdapter mAdapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_new_friend);
        SharedPreferences userId=getSharedPreferences("data_userID",MODE_PRIVATE); //存用户登录ID
        sp=getSharedPreferences("data_"+userId.getInt("user_id",-1),MODE_PRIVATE); //根据ID获取用户数据文件
        editor=sp.edit();
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.top_bottom));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        mSearchView=findViewById(R.id.search);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSubmitButtonEnabled(true);
        search1=findViewById(R.id.search_plate);
        search2=findViewById(R.id.submit_area);
        back_arrow=findViewById(R.id.back_arrow);
        mRv=findViewById(R.id.rv);
        search1.setBackground(null);
        search2.setBackground(null);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(NewFriendActivity.this, ContactFragment.class);
                startActivity(intent);
                finish();
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (mSearchView != null) {
                    // 得到输入管理对象
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        // 这将让键盘在所有的情况下都被隐藏，但是一般我们在点击搜索按钮后，输入法都会乖乖的自动隐藏的。
                        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0); // 输入法如果是显示状态，那么就隐藏输入法                    }
                        mSearchView.clearFocus(); // 不获取焦点
                    }
                    if(query.length()==11){
                        if(Is.isFriendByPhone(query)){
                            Intent intent = new Intent(NewFriendActivity.this, UserInfoActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("who","friend");
                            bundle.putString("from","search");
                            bundle.putInt("contact_id",Is.getIdByPhone(query));
                            intent.putExtras(bundle);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                            startActivity(intent);
                        }
                        else{
                            sendByPost(query);
                        }
                    }
                    else{
                        Toast.makeText(NewFriendActivity.this,"请输入正确的手机号!",Toast.LENGTH_SHORT).show();
                    }


                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.setIconified(false);
            }
        });

        Intent mqttServiceIntent = new Intent(NewFriendActivity.this, MqttService.class);
        startService(mqttServiceIntent);
        connection = new MqttServiceConnection();
        bindService(mqttServiceIntent, connection, Context.BIND_AUTO_CREATE);


    }

    private class MqttServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MqttService.LocalBinder localBinder = (MqttService.LocalBinder) service;
            friendRequest=localBinder.getService().getFriReqMessage();
            Log.i("friendRequest user", Arrays.toString(friendRequest.toArray()));
            mAdapter= new NewFriendAdapter(NewFriendActivity.this,friendRequest);
            mRv.setLayoutManager(new LinearLayoutManager(NewFriendActivity.this));
            mRv.setAdapter(mAdapter);
            mAdapter.setDatas(friendRequest);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("friendRequest in New",sp.getString("friReqMessage",""));
    }

    private void sendByPost(String user_phone) {
        JSONObject json=new JSONObject();
        json.put("user_phone",user_phone);
        String path = getResources().getString(R.string.request_local)+"/userSearch";
        OkHttpClient client = new OkHttpClient();
        final FormBody formBody = new FormBody.Builder()
                .add("json", json.toJSONString())
                .build();
        System.out.println("*********"+json.toJSONString());
        Request request = new Request.Builder()
                .url(path)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(NewFriendActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info=response.body().string();
                if(info.equals("null")){
                    Looper.prepare();
                    Toast.makeText(NewFriendActivity.this,"找不到该用户",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                else{
                    Intent intent = new Intent(NewFriendActivity.this, UserInfoActivity.class);
                    Bundle bundle = new Bundle();
                    if(user_phone.equals(modifyUser.getUser_phone())){
                        bundle.putString("who","me");
                    }else{
                        bundle.putString("who","stranger");
                    }
                    bundle.putString("user_info", info);
                    bundle.putString("from","search");
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
