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

public class ModifyActivity extends AppCompatActivity {

    EditText Ename;
    EditText Esign;
    EditText Egender;
    EditText Eage;
    EditText Eimg;
    Button button;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private MyDBHelper dbHelper;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_modify);
        Ename=findViewById(R.id.Ename);
        Esign=findViewById(R.id.Esign);
        Egender=findViewById(R.id.Egender);
        Eage=findViewById(R.id.Eage);
        Eimg=findViewById(R.id.Eimg);
        button=findViewById(R.id.save);
        sp=getSharedPreferences("data",MODE_PRIVATE);
        editor=sp.edit();
        dbHelper=new MyDBHelper(getApplicationContext(),"DB",null,1);
        SQLiteDatabase db=dbHelper.getWritableDatabase();


        Ename.setText(sp.getString("QQname",""));
        Esign.setText(sp.getString("QQsign",""));
        Egender.setText(sp.getString("QQgender",""));
        Eage.setText(sp.getString("QQage",""));
        Eimg.setText(sp.getString("QQimg",""));
        //保存修改
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values=new ContentValues();
                values.put("QQname",Ename.getText().toString());
                values.put("QQsign",Esign.getText().toString());
                values.put("QQgender", Egender.getText().toString());
                values.put("QQage",Eage.getText().toString());
                values.put("QQimg",Eimg.getText().toString());
                db.update("QQ_Login",values,"QQname=? AND QQpwd=?",new String[]{sp.getString("QQname",""),sp.getString("QQpwd","")});
                editor.putString("QQimg",Eimg.getText().toString());
                editor.putString("QQname",Ename.getText().toString());
                editor.putString("QQsign",Esign.getText().toString());
                editor.putString("QQgender", Egender.getText().toString());
                editor.putString("QQage",Eage.getText().toString());
                editor.apply();
                Toast.makeText(ModifyActivity.this,"已保存修改",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(ModifyActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
