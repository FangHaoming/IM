package com.hrl.chaui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hrl.chaui.R;

public class ModifySignActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_modify_sign);
        EditText Edit=findViewById(R.id.Edit);
        Button save=findViewById(R.id.save);
        TextView back_arrow=findViewById(R.id.back_arrow);
        SharedPreferences sp=getSharedPreferences("data",MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        if(!sp.getString("user_sign","").equals("")){
            Edit.setText(sp.getString("user_sign",""));
        }
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ModifySignActivity.this,ModifyActivity.class);
                if(!sp.getString("user_sign","").equals(Edit.getText().toString())){
                    Bundle bundle=new Bundle();
                    bundle.putString("user_sign",Edit.getText().toString());
                    bundle.putBoolean("isModify",true);
                    editor.putString("user_sign",Edit.getText().toString());
                    editor.apply();
                    intent.putExtras(bundle);
                }
                startActivity(intent);
                finish();
            }
        });
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ModifySignActivity.this,ModifyActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent=new Intent(ModifySignActivity.this,ModifyActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }
}
