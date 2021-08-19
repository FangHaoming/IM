package com.hrl.chaui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.hrl.chaui.R;
import com.hrl.chaui.adapter.FragmentAdapter;
import com.hrl.chaui.fragment.ContactFragment;
import com.hrl.chaui.fragment.MessageFragment;
import com.hrl.chaui.fragment.MineFragment;


import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity {

    private List<Fragment> fragments=new ArrayList<Fragment>();
    private ViewPager viewPager;
    private ImageView message,contact,mine,current;
    private TextView title;
    private View.OnClickListener listener;
    private int currentID=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        initView();
        fragments.add(new MessageFragment());
        fragments.add(new ContactFragment());
        fragments.add(new MineFragment());
        FragmentAdapter adapter=new FragmentAdapter(getSupportFragmentManager(),fragments);
        viewPager.setAdapter(adapter);

    }

    private void initView(){
        viewPager=findViewById(R.id.vp);
        message=findViewById(R.id.message);
        contact=findViewById(R.id.contact);
        mine=findViewById(R.id.mine);
        title=findViewById(R.id.title);
        message.setSelected(true);
        current=message;
        title.setText("消息");
        listener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change(v.getId());
            }
        };
        message.setOnClickListener(listener);
        contact.setOnClickListener(listener);
        mine.setOnClickListener(listener);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                change(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void change(int id){
        switch(currentID){
            case 0:
                message.setSelected(false);
                break;
            case 1:
                contact.setSelected(false);
                break;
            case 2:
                mine.setSelected(false);
                break;
            default:break;

        }
        switch(id){
            case R.id.message:
                title.setText("消息");
                viewPager.setCurrentItem(0);
            case 0:
                message.setSelected(true);
                currentID=0;
                break;
            case R.id.contact:
                title.setText("好友");
                viewPager.setCurrentItem(1);
            case 1:
                contact.setSelected(true);
                currentID=1;
                break;
            case R.id.mine:
                title.setText("个人中心");
                viewPager.setCurrentItem(2);
            case 2:
                mine.setSelected(true);
                currentID=2;
                break;
            default:break;
        }

    }

}
