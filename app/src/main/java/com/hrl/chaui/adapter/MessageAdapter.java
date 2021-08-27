package com.hrl.chaui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hrl.chaui.R;
import com.hrl.chaui.bean.Message;
import com.hrl.chaui.bean.MsgType;
import com.hrl.chaui.bean.TextMsgBody;
import com.hrl.chaui.bean.User;
import com.hrl.chaui.fragment.MessageFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.hrl.chaui.MyApplication.contactData;

public class MessageAdapter extends BaseQuickAdapter<Message, BaseViewHolder> {

    Context context = null;

    public MessageAdapter(Context context,@Nullable List<Message> data) {
        super(R.layout.item_message,data); // 设置item样式
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, Message item) {
        setContent(helper, item);
    }

    private void setContent(BaseViewHolder helper, Message item){

        // 通话对象
        String userID = "GID_test@@@" + context.getSharedPreferences("data", Context.MODE_PRIVATE).getInt("user_id", -1);
        String otherID = item.getSenderId().equals(userID) ? item.getTargetId() : item.getSenderId();
        int otherIDInt = Integer.valueOf(otherID.split("@@@")[1]);
        String name = "陌生人";
        for(User user : contactData)  {
            if (user.getId() != null && user.getId() == otherIDInt) {
                name = user.getName();
                break;
            }
        }
        helper.setText(R.id.message_item_title, name);

        // 发送时间
        long sendTime = item.getSentTime();
        String sendTimeText = getSendTimeText(sendTime);
        helper.setText(R.id.message_item_time_text, sendTimeText);

        // 显示在消息名称下方
        String msgInfo;
        MsgType msgType = item.getMsgType();
        switch (msgType) {
            case TEXT:
                msgInfo = ((TextMsgBody)item.getBody()).getMessage();
                break;
            case  IMAGE:
                msgInfo = "[图片]";
                break;
            case AUDIO:
                msgInfo = "[语音]";
                break;
            case VIDEO:
                msgInfo = "[视频]";
                break;
            case FILE:
                msgInfo = "[文件]";
                break;
            default:
                msgInfo = "[消息]";
                break;
        }
        helper.setText(R.id.message_item_info, msgInfo);

        // 未查看的消息显示小红点
        if (item.isCheck() == false)
            helper.setVisible(R.id.message_item_redpoint, true);
        else
            helper.setVisible(R.id.message_item_redpoint, false);



    }



    private String getSendTimeText(long sendTime) {
        Date date = new Date(sendTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String dateStr = dateFormat.format(date);
        String nowStr = dateFormat.format(System.currentTimeMillis());
        String[] dateInfo = dateStr.split("-");
        String[] nowInfo = nowStr.split("-");

        String year = dateInfo[0];
        String month = dateInfo[1];
        String day = dateInfo[2];
        String hour = dateInfo[3];
        String minute = dateInfo[4];
        String second = dateInfo[5];

        String nowYear = nowInfo[0];
        String nowMonth = nowInfo[1];
        String nowDay = nowInfo[2];
        String nowHour = nowInfo[3];
        String nowMinute = nowInfo[4];
        String nowSecond = nowInfo[5];

        int nowDayInt = Integer.valueOf(nowDay);
        int nowMonthInt = Integer.valueOf(nowMonth);
        int nowYearInt = Integer.valueOf(nowYear);

        String prevDay; // 前一天
        if (nowDayInt - 1 != 0) {
            prevDay = String.valueOf(nowDayInt - 1);
        } else {
            if (nowMonthInt == 1 || nowMonthInt == 2 || nowMonthInt == 4 || nowMonthInt == 6 || nowMonthInt == 8 || nowMonthInt == 9 || nowMonthInt == 11) {
                prevDay = String.valueOf(31);
            } else if (nowMonthInt != 3) {
                prevDay = String.valueOf(30);
            } else if ((nowYearInt % 4 == 0 && nowYearInt % 100 != 0) | nowYearInt % 400 == 0) {
                prevDay = String.valueOf(29);
            } else{
                prevDay = String.valueOf(28);
            }
        }

        Log.e("time", prevDay + "  day:" + day);

        if (!year.equals(nowYear)) {
            return year + "年" + month + "月" + day + "日";
        } else if (!month.equals(nowMonth) || (!day.equals(nowDay) && !day.equals(prevDay))) {
            return month + "月" + day + "日";
        } else if (day.equals(prevDay)){
            return "昨天";
        } else {
            return hour + ":" + minute;
        }

    }

}
