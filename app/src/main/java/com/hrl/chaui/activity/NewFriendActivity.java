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

import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.R;
import com.hrl.chaui.bean.User;
import com.hrl.chaui.util.Is;
import com.hrl.chaui.util.MqttService;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewFriendActivity extends AppCompatActivity {
    MqttService.LocalBinder binder;
    ArrayList<User> users=new ArrayList<>();
    MqttService mService;
    MqttServiceConnection mConn;
    private SharedPreferences rev;
    private SharedPreferences.Editor editor;
    public TextView add;
    public SearchView mSearchView;
    public View search1,search2;
    public TextView back_arrow;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_new_friend);
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
        search1.setBackground(null);
        search2.setBackground(null);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(NewFriendActivity.this,MainActivity.class);
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
                    if(Is.isFriendByPhone(query)){
                        Intent intent = new Intent(NewFriendActivity.this, UserInfoActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("isFriend",true);
                        bundle.putInt("contact_id",Is.getIdByPhone(query));
                        intent.putExtras(bundle);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                        startActivity(intent);
                        finish();
                    }
                    else{
                        sendByPost(query);
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
        /*
        Intent intent=new Intent(NewFriendActivity.this, MqttService.class);
        mConn=new MqttServiceConnection();
        rev=getSharedPreferences("data",MODE_PRIVATE);
        editor=rev.edit();
        bindService(intent,mConn,BIND_AUTO_CREATE);
        System.out.println("*************add"+rev.getString("friReqMessage", ""));

         */

    }

    public class MqttServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder= (MqttService.LocalBinder) service;
            mService=binder.getService();
            users=mService.getFriReqMessage();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
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
                    bundle.putBoolean("isFriend",false);
                    bundle.putString("user_info", info);
                    bundle.putString("from","search");
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
