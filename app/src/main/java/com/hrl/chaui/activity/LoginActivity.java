package com.hrl.chaui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.R;
import com.hrl.chaui.util.AppManager;
import com.hrl.chaui.util.EditIsCanUseBtnUtils;
import com.hrl.chaui.util.MqttService;
import com.hrl.chaui.util.http;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.hrl.chaui.MyApplication.modifyUser;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn;
    private int code = 1;
    private TextView register;
    private CheckBox remember;
    private CheckBox auto_login;
    private EditText phone;
    private EditText pwd;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private SharedPreferences.Editor editor_userId;
    Intent intent2;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        AppManager.addActivity(this);

        loginBtn=findViewById(R.id.loginBtn);
        register =findViewById(R.id.register);
        remember=findViewById(R.id.checkbox);
        phone=findViewById(R.id.phone);
        pwd=findViewById(R.id.pwd);
        auto_login=findViewById(R.id.checkbox_auto);

        register.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        register.getPaint().setAntiAlias(true);

        SharedPreferences userId=getSharedPreferences("data_userID",MODE_PRIVATE); //存用户登录ID
        SharedPreferences sp=getSharedPreferences("data_"+userId.getInt("user_id",-1),MODE_PRIVATE); //根据ID获取用户数据文件
        editor=sp.edit();
        editor_userId=userId.edit();

        phone.setText(sp.getString("user_phone",""));
        if(sp.getBoolean("isRemember",false)){
            pwd.setText(sp.getString("user_pwd",""));
            loginBtn.setEnabled(true);
            remember.setChecked(true);
        }
        else{
            EditIsCanUseBtnUtils.getInstance()
                    .addContext(this)
                    .addEdittext(phone)
                    .addEdittext(pwd)
                    .setBtn(loginBtn)
                    .build();
        }
        if(sp.getBoolean("isAuto",false)){
            auto_login.setChecked(true);
        }
        //注册按钮
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        //登录按钮
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(phone.getText().length()<11){
                    Toast.makeText(LoginActivity.this,"请输入正确的手机号",Toast.LENGTH_SHORT).show();
                }
                else if(pwd.getText().toString().trim().equals("")){
                    Toast.makeText(LoginActivity.this,"请输入密码",Toast.LENGTH_SHORT).show();
                }
                else{
                    sendByPost(phone.getText().toString().trim(),pwd.getText().toString().trim());
                }

            }
        });
    }
    private void sendByPost(String user_phone, String user_pwd) {
        JSONObject json1=new JSONObject();
        json1.put("user_phone",user_phone);
        json1.put("user_pwd",user_pwd);
        /*
        json.put("user_gender","");
        json.put("user_sign","");
        json.put("user_img","");
        json.put("img_data","");
         */
        String path = getResources().getString(R.string.request_local)+"/userLogin";
        OkHttpClient client = new OkHttpClient();
        final FormBody formBody = new FormBody.Builder()
                .add("json", json1.toJSONString())
                .build();
        System.out.println("*********"+json1.toJSONString());
        Request request = new Request.Builder()
                .url(path)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(LoginActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info=response.body().string();
                JSONObject json= JSON.parseObject(info);
                System.out.println("**********info"+info);
                switch (Integer.parseInt(json.get("status").toString())){
                    case 2:
                        intent2=new Intent(LoginActivity.this, MqttService.class);
                        startService(intent2);
                        http.sendByPost(LoginActivity.this,json.getInteger("user_id"));

                        SharedPreferences sp=getSharedPreferences("data_"+json.getInteger("user_id"),MODE_PRIVATE); //根据ID获取用户数据文件
                        editor=sp.edit();
                        if(remember.isChecked()){
                            editor.putBoolean("isRemember",true);
                        }
                        else{
                            editor.putBoolean("isRemember",false);
                        }
                        if(auto_login.isChecked()){
                            editor.putBoolean("isAuto",true);
                        }
                        else{
                            editor.putBoolean("isAuto",false);
                        }
                        modifyUser.setUser_id(json.getInteger("user_id"));
                        modifyUser.setUser_name(json.getString("user_name"));
                        modifyUser.setUser_gender(json.getString("user_gender"));
                        modifyUser.setUser_pwd(user_pwd);
                        modifyUser.setUser_phone(json.getString("user_phone"));
                        modifyUser.setUser_img(json.getString("user_img"));
                        editor.putString("user_gender", (String) json.get("user_gender"));
                        editor.putInt("user_id", json.getInteger("user_id"));
                        editor.putString("user_img",(String)json.get("user_img"));
                        editor.putString("user_name",(String)json.get("user_name"));
                        editor.putString("user_phone",(String)json.get("user_phone"));
                        editor.putString("user_sign",(String)json.get("user_sign"));
                        editor.putString("user_pwd",user_pwd);
                        editor_userId.putInt("user_id",json.getInteger("user_id"));
                        editor_userId.apply();
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        Looper.prepare();
                        Toast.makeText(LoginActivity.this, "登录成功!", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        break;
                    case 1:
                        Looper.prepare();
                        Toast.makeText(LoginActivity.this, "密码错误!", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        break;
                    case 0:
                        Looper.prepare();
                        Toast.makeText(LoginActivity.this, "该账户不存在!", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        break;
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            AppManager.AppExit(this);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent2);
    }
}
