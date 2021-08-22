package com.hrl.chaui.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
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
    private TextView title,t_mine,t_message,t_contact;
    private View.OnClickListener listener;
    private int currentID=0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.top_bottom));
        window.setNavigationBarColor(getResources().getColor(R.color.top_bottom));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void initView(){
        t_contact=findViewById(R.id.t_contact);
        t_message=findViewById(R.id.t_message);
        t_mine=findViewById(R.id.t_mine);
        viewPager=findViewById(R.id.vp);
        message=findViewById(R.id.message);
        contact=findViewById(R.id.contact);
        mine=findViewById(R.id.mine);
        title=findViewById(R.id.title);
        message.setSelected(true);
        current=message;
        title.setText("消息");
        t_message.setTextColor(this.getResources().getColor(R.color.navigation_text_selected));
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
            case 0:
                title.setText("消息");
                t_message.setTextColor(this.getResources().getColor(R.color.navigation_text_selected));
                t_contact.setTextColor(this.getResources().getColor(R.color.navigation_text_normal));
                t_mine.setTextColor(this.getResources().getColor(R.color.navigation_text_normal));
                viewPager.setCurrentItem(0);
                message.setSelected(true);
                currentID=0;
                break;
            case R.id.contact:
            case 1:
                title.setText("通讯录");
                t_contact.setTextColor(this.getResources().getColor(R.color.navigation_text_selected));
                t_message.setTextColor(this.getResources().getColor(R.color.navigation_text_normal));
                t_mine.setTextColor(this.getResources().getColor(R.color.navigation_text_normal));
                viewPager.setCurrentItem(1);
                contact.setSelected(true);
                currentID=1;
                break;
            case R.id.mine:
            case 2:
                t_mine.setTextColor(this.getResources().getColor(R.color.navigation_text_selected));
                t_message.setTextColor(this.getResources().getColor(R.color.navigation_text_normal));
                t_contact.setTextColor(this.getResources().getColor(R.color.navigation_text_normal));
                title.setText("我");
                viewPager.setCurrentItem(2);
                mine.setSelected(true);
                currentID=2;
                break;
            default:break;
        }

    }

}
