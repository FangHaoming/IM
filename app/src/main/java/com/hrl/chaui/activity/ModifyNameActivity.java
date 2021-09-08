package com.hrl.chaui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
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

import com.hrl.chaui.R;

public class ModifyNameActivity extends AppCompatActivity {
    Bundle bundle;
    Intent intent;
    Intent intent_back;
    Bundle bundle_back;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_modify_name);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.top_bottom));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        EditText Edit=findViewById(R.id.Edit);
        Button save=findViewById(R.id.save);
        intent=getIntent();
        bundle=intent.getExtras();
        bundle_back=new Bundle();
        TextView back_arrow=findViewById(R.id.back_arrow);
        SharedPreferences userId=getSharedPreferences("data_userID",MODE_PRIVATE); //存用户登录ID
        SharedPreferences sp=getSharedPreferences("data_"+userId.getInt("user_id",-1),MODE_PRIVATE); //根据ID获取用户数据文件
        SharedPreferences.Editor editor=sp.edit();
        if(bundle.getString("from").equals("friend")){
            Edit.setText(bundle.getString("friend_note"));
        }else if(bundle.getString("from").equals("group")){
            if(bundle.getString("which").equals("group_name")){
                Edit.setText(bundle.getString("group_name"));
            }else{
                Edit.setText(bundle.getString("nickname"));
            }
        }else if(bundle.getString("from").equals("me")){
            Edit.setText(sp.getString("user_name",""));
        }
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Edit.getText().length()==0) {
                    Toast.makeText(ModifyNameActivity.this, "名称不能为空", Toast.LENGTH_SHORT).show();
                }
                else{
                    bundle_back.putBoolean("isModify", false);
                    if(bundle.getString("from").equals("friend")){ //设置好友备注
                        intent_back= new Intent(ModifyNameActivity.this, UserInfoActivity.class);
                        if (!bundle.getString("friend_note").equals(Edit.getText().toString())) { //TODO 判空
                            bundle_back.putBoolean("isModify", true);
                        }
                        bundle_back.putString("friend_note", Edit.getText().toString());
                        bundle_back.putString("who","friend");  //以此来判断UserInfoActivity加载的布局
                        bundle_back.putString("from","modifyName"); //以此来判断UserInfoActivity返回的Activity
                    }
                    else if(bundle.getString("from").equals("group")){
                        intent_back= new Intent(ModifyNameActivity.this, GroupInfoActivity.class);
                        bundle_back.putInt("group_id",bundle.getInt("group_id"));
                        if(bundle.getString("which").equals("nickname")){ //我在群的昵称
                            if (!bundle.getString("nickname").equals(Edit.getText().toString())) { //TODO判空
                                bundle_back.putBoolean("isModify", true);
                            }
                            bundle_back.putString("nickname", Edit.getText().toString());
                        }
                        else if(bundle.getString("which").equals("group_name")){ //群聊名称
                            if (!bundle.getString("group_name").equals(Edit.getText().toString())) {
                                bundle_back.putBoolean("isModify", true);
                            }
                            bundle_back.putString("group_name", Edit.getText().toString());
                        }

                    }
                    else if(bundle.getString("from").equals("me")){ //修改个人信息
                        intent_back= new Intent(ModifyNameActivity.this, ModifyActivity.class);
                        if (!bundle.getString("user_name").equals(Edit.getText().toString())) {
                            bundle_back.putBoolean("isModify", true);
                        }
                        bundle_back.putString("user_name", Edit.getText().toString());
                    }
                    intent_back.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent_back.putExtras(bundle_back);
                    startActivity(intent_back);
                    finish();
                }
            }
        });
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            back();
        }
        return true;
    }

    private void back(){
        Intent intent = null;
        if (bundle.getString("from").equals("group")) {
            intent = new Intent(ModifyNameActivity.this, GroupInfoActivity.class);
        } else if (bundle.getString("from").equals("friend")) {
            intent = new Intent(ModifyNameActivity.this, UserInfoActivity.class);
        } else if (bundle.getString("from").equals("me")) {
            intent = new Intent(ModifyNameActivity.this, NewFriendActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(0, R.anim.slide_right_out);
        finish();
    }
}
