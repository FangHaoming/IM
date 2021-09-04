package com.hrl.chaui.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class ViewUtils {

    public static int GRIDVIEW_ITEM = 0x20123456;

    /**
     * 设置GridView中item固定垂直和横行间隔宽度item， 然后自适应一行多少个，每个item会自动改大小适应。
     *
     * @param convertView  item的View
     * @param parent    getview 中的 parent
     * @param itemWithDp  希望item的大致宽度
     * @param isSquare  item是否是正方形
     */
    public static void setGridViewItemWith(View convertView, ViewGroup parent, int itemWithDp, int hSpacingDp,
                                           int vSpacingDp, boolean isSquare) {
        if (parent instanceof GridView && null == convertView.getTag(GRIDVIEW_ITEM)) {
            if (0 == parent.getWidth()) {
                return;
            }
            GridView gv = (GridView) parent;
            float density = gv.getContext().getResources().getDisplayMetrics().density;
            if (null == gv.getTag(GRIDVIEW_ITEM)) { // GridView只设置一次
                int parentWith = gv.getWidth() - gv.getPaddingLeft() - gv.getPaddingRight();
                int count = (int) (parentWith / ((itemWithDp + hSpacingDp) * density));

                gv.setVerticalSpacing((int) (density * vSpacingDp));
                gv.setHorizontalSpacing((int) (density * hSpacingDp));
                if (count <= 0) {
                    count = 1;
                }
                gv.setNumColumns(count);
                gv.setTag(GRIDVIEW_ITEM, count);
            }

            if (null == convertView.getTag(GRIDVIEW_ITEM)) { // 一个convertView只设置一次
                int parentWith = gv.getWidth() - gv.getPaddingLeft() - gv.getPaddingRight();
                int count = (int) gv.getTag(GRIDVIEW_ITEM);
                int itemWith = (int) ((parentWith - (density * hSpacingDp) * (count - 1)) / count); // item宽度
                ViewGroup.LayoutParams l = convertView.getLayoutParams();
                l.width = itemWith;
                if (isSquare) {
                    l.height = itemWith;
                }
                convertView.setLayoutParams(l);
            }
        }
    }
}