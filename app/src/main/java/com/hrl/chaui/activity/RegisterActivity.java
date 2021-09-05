package com.hrl.chaui.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.R;
import com.hrl.chaui.util.EditIsCanUseBtnUtils;
import com.hrl.chaui.util.MyDBHelper;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class RegisterActivity extends AppCompatActivity {

    private View back_arrow;
    private EditText pwd;
    private EditText confirm_pwd;
    private EditText name;
    private EditText phone;
    private Button regiBtn;
    public String user_name;
    public String user_pwd;
    public String user_confirm_pwd;
    public String user_phone;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        pwd=findViewById(R.id.pwd);
        confirm_pwd=findViewById(R.id.confirm_pwd);
        phone=findViewById(R.id.phone);
        regiBtn=findViewById(R.id.registerBtn);
        name=findViewById(R.id.name);
        back_arrow=findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(0, R.anim.slide_right_out);
                finish();
            }
        });
        EditIsCanUseBtnUtils.getInstance()
                .addContext(this)
                .setBtn(regiBtn)
                .addEdittext(pwd)
                .addEdittext(confirm_pwd)
                .addEdittext(name)
                .addEdittext(phone)
                .build();


        MyDBHelper dbHelper=new MyDBHelper(getApplicationContext(),"DB",null,1);
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        //确定
        regiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isUserNameAndPwdValid()){
                    sendByPost(user_phone,user_pwd,user_name);
                }
            }
        });
    }
    private void sendByPost(String user_phone, String user_pwd,String user_name) {
        JSONObject json=new JSONObject();
        json.put("user_phone",user_phone);
        json.put("user_pwd",user_pwd);
        json.put("user_name",user_name);
        String path = getResources().getString(R.string.request_local)+"/userRegister";
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
                Toast.makeText(RegisterActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info=response.body().string();
                JSONObject json= JSON.parseObject(info);
                System.out.println("**********info"+info);
                switch (Objects.requireNonNull(json.get("msg")).toString()) {
                    case "register success":
                        Looper.prepare();
                        Toast.makeText(RegisterActivity.this, "注册成功!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        Looper.loop();
                        break;
                    case "register failed":
                        Looper.prepare();
                        Toast.makeText(RegisterActivity.this, "注册失败!", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        break;
                    case "already exist":
                        Looper.prepare();
                        Toast.makeText(RegisterActivity.this, "该账号已存在!", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        break;
                }
            }
        });
    }

    public boolean isUserNameAndPwdValid() {
        user_name=name.getText().toString().trim();
        user_pwd=pwd.getText().toString().trim();
        user_confirm_pwd=confirm_pwd.getText().toString().trim();
        user_phone=phone.getText().toString().trim();
        if (user_name.equals("")) {
            Toast.makeText(this, "用户名不能为空",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if(user_phone.equals("")){
            Toast.makeText(this, "手机号不能为空",
                    Toast.LENGTH_SHORT).show();
            return false;
        }else if (user_pwd.equals("")) {
                Toast.makeText(this,"密码不能为空",
                        Toast.LENGTH_SHORT).show();
                return false;
        }else if(user_confirm_pwd.equals("")) {
            Toast.makeText(this, "请再次输入密码",
                    Toast.LENGTH_SHORT).show();
            return false;
        }else if(!user_pwd.equals(user_confirm_pwd)) {
            Toast.makeText(this, "两次密码不一样", Toast.LENGTH_SHORT).show();
            return false;
        }else if(user_phone.length()<11) {
            Toast.makeText(this, "请输入正确的手机号",
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
