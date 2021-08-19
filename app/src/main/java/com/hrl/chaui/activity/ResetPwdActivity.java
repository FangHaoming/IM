package com.hrl.chaui.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hrl.chaui.R;
import com.hrl.chaui.util.MyDBHelper;


public class ResetPwdActivity extends AppCompatActivity {

    private EditText in;
    private EditText rein;
    private Button asure;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_resetpwd);
        in=findViewById(R.id.in);
        rein=findViewById(R.id.rein);
        asure=findViewById(R.id.asure);
        SharedPreferences sp=getSharedPreferences("data",MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        MyDBHelper dbHelper=new MyDBHelper(getApplicationContext(),"DB",null,1);
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        //保存修改（密码）
        asure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_in=in.getText().toString();
                String str_rein=rein.getText().toString();
                if(str_in.equals(str_rein)){
                    ContentValues values=new ContentValues();
                    values.put("QQpwd",str_in);
                    db.update("QQ_Login",values,"QQname=? AND QQpwd=?",new String[]{sp.getString("QQname",""),sp.getString("QQpwd","")});
                    editor.putString("QQpwd",str_in);
                    editor.apply();
                    Toast.makeText(ResetPwdActivity.this,"修改成功",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(ResetPwdActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else Toast.makeText(ResetPwdActivity.this,"两次输入不一致!",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
