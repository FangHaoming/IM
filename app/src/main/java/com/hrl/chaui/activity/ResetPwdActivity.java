package com.hrl.chaui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.R;
import com.hrl.chaui.fragment.MineFragment;
import com.hrl.chaui.util.EditIsCanUseBtnUtils;

import static com.hrl.chaui.MyApplication.modifyUser;


public class ResetPwdActivity extends AppCompatActivity {

    private EditText in;
    private EditText rein;
    private Button asure;
    private TextView back_arrow;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_resetpwd);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.top_bottom));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        in=findViewById(R.id.in);
        rein=findViewById(R.id.rein);
        asure=findViewById(R.id.asure);
        back_arrow=findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ResetPwdActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        EditIsCanUseBtnUtils.getInstance()
                .addContext(this)
                .addEdittext(in)
                .addEdittext(rein)
                .setBtn(asure)
                .build();
        SharedPreferences userId=getSharedPreferences("data_userID",MODE_PRIVATE); //???????????????ID
        SharedPreferences sp=getSharedPreferences("data_"+userId.getInt("user_id",-1),MODE_PRIVATE); //??????ID????????????????????????
        SharedPreferences.Editor editor=sp.edit();
        //????????????????????????
        asure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_in=in.getText().toString();
                String str_rein=rein.getText().toString();
                if(str_in.equals(str_rein)){
                    modifyUser.setUser_pwd(str_in);
                    Intent intent=new Intent(ResetPwdActivity.this, MineFragment.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isModify", true);
                    bundle.putString("json",JSONObject.toJSONString(modifyUser));
                    intent.putExtras(bundle);
                    setResult(Activity.RESULT_OK,intent);
                    finish();
                }
                else Toast.makeText(ResetPwdActivity.this,"?????????????????????!",Toast.LENGTH_SHORT).show();
            }
        });
    }


}
