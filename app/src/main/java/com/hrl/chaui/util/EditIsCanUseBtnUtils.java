package com.hrl.chaui.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class EditIsCanUseBtnUtils {

    private List<EditText> editTextList = new ArrayList<>();
    private Button btn;
    private Context context;

    public static EditIsCanUseBtnUtils getInstance(){
        return new EditIsCanUseBtnUtils();
    }
    public EditIsCanUseBtnUtils setBtn(Button btn){
        this.btn = btn;
        btn.setEnabled(false);
        return this;
    }
    public EditIsCanUseBtnUtils addEdittext(EditText editText){
        editTextList.add(editText);
        return this;
    }
    public EditIsCanUseBtnUtils addText(String text){
        btn.setText(text);
        return this;
    }
    public EditIsCanUseBtnUtils addContext(Context context){
        this.context = context;
        return this;
    }
    public void build(){
        setWatcher();
    }
    private void setWatcher() {
        for (int i = 0; i < editTextList.size(); i++) {
            editTextList.get(i).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0){
                        setBtnUnavailable();
                        return;
                    }
                    boolean tag = false;
                    for (int i1 = 0; i1 < editTextList.size(); i1++) {
                        if (editTextList.get(i1).getText().length()!=0){
                            tag = true;
                        }else {
                            tag = false;
                            break;
                        }
                    }
                    if (tag){
                        setBtnAvailable();
                    }else {
                        setBtnUnavailable();
                    }
                }
            });
        }
    }
    private void setBtnAvailable(){
        btn.setEnabled(true);
    }
    private void setBtnUnavailable(){
        btn.setEnabled(false);
    }
}