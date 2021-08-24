package com.hrl.chaui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.R;
import com.hrl.chaui.util.MqttService;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn;
    private int code = 1;
    private TextView register;
    private CheckBox remember;
    private EditText phone;
    private EditText pwd;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        loginBtn=findViewById(R.id.loginBtn);
        register =findViewById(R.id.register);
        remember=findViewById(R.id.checkbox);
        phone=findViewById(R.id.phone);
        pwd=findViewById(R.id.pwd);

        SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
        editor=sharedPreferences.edit();

        System.out.println("this is user_name:"+sharedPreferences.getString("user_name",""));
        if(sharedPreferences.getBoolean("isCheck",false)){
            phone.setText(sharedPreferences.getString("user_phone",""));
            pwd.setText(sharedPreferences.getString("user_pwd",""));
            remember.setChecked(true);
            sendByPost(phone.getText().toString().trim(),pwd.getText().toString().trim());
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
                    if(remember.isChecked()){
                        editor.putBoolean("isCheck",true);
                        editor.apply();
                    }
                    sendByPost(phone.getText().toString().trim(),pwd.getText().toString().trim());
                }

            }
        });
    }
    private void sendByPost(String user_phone, String user_pwd) {
        JSONObject json=new JSONObject();
        json.put("user_phone",user_phone);
        json.put("user_pwd",user_pwd);
        /*
        json.put("user_gender","");
        json.put("user_sign","");
        json.put("user_img","");
        json.put("img_data","");
         */
        String path = "http://40f730q296.qicp.vip/userLogin";
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

                        editor.putString("user_gender", (String) json.get("user_gender"));
                        editor.putInt("user_id", (int) json.get("user_id"));
                        editor.putString("user_img",(String)json.get("user_img"));
                        editor.putString("user_name",(String)json.get("user_name"));
                        editor.putString("user_phone",(String)json.get("user_phone"));
                        editor.putString("user_sign",(String)json.get("user_sign"));
                        editor.putString("user_pwd",pwd.getText().toString());
                        editor.apply();

                        Intent intent2=new Intent(LoginActivity.this,MqttService.class);
                        startService(intent2);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
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


}
