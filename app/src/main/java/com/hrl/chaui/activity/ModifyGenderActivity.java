package com.hrl.chaui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hrl.chaui.R;

public class ModifyGenderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_modify_gender);
        View male,female;
        TextView male_text,female_text;
        male=findViewById(R.id.male);
        female=findViewById(R.id.female);
        Button save=findViewById(R.id.save);
        male_text=findViewById(R.id.text_male);
        female_text=findViewById(R.id.text_female);
        TextView back_arrow=findViewById(R.id.back_arrow);
        SharedPreferences sp=getSharedPreferences("data",MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        Intent intent=new Intent(ModifyGenderActivity.this,ModifyActivity.class);
        Bundle bundle=new Bundle();

        if(sp.getString("user_gender","").equals("男")){
            male_text.setBackground(getResources().getDrawable(R.drawable.chosen));
            female_text.setBackground(null);
        }
        else if(sp.getString("user_gender","").equals("女")){
            female_text.setBackground(getResources().getDrawable(R.drawable.chosen));
            male_text.setBackground(null);
        }

        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                male_text.setBackground(getResources().getDrawable(R.drawable.chosen));
                female_text.setBackground(null);
                bundle.putString("user_gender","男");
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                female_text.setBackground(getResources().getDrawable(R.drawable.chosen));
                male_text.setBackground(null);
                bundle.putString("user_gender","女");
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sp.getString("user_gender","").equals(bundle.getString("user_gender"))){
                    bundle.putBoolean("isModify",true);
                    intent.putExtras(bundle);
                    editor.putString("user_gender",bundle.getString("user_gender"));
                    editor.apply();
                }
                startActivity(intent);
                finish();
            }
        });
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ModifyGenderActivity.this,ModifyActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent=new Intent(ModifyGenderActivity.this,ModifyActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }
}
