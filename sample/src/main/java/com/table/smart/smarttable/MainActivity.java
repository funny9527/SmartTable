package com.table.smart.smarttable;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.table.smart.ui.SmartTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private SmartTable smartTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smartTable = findViewById(R.id.table);

        findViewById(R.id.next).setOnClickListener(this);
        findViewById(R.id.previous).setOnClickListener(this);

        test();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                smartTable.nextPage();
                break;
            case R.id.previous:
                smartTable.previous();
                break;
        }
    }

    private void test() {
        String[][] columnValues = new String[][] {
                {"abcde", "非大煞都是",                                  "1234",        "非得花时间咖啡哈的科技示范户的卡萨肌肤", "多"},
                {"a",     "非是",                                       "12345678910", "非",                                "多福多寿肌肤"},
                {"abc",   "非大煞风景的时刻发挥的精神焕发开始的机会附件都是",  "1",           "非得花时间咖啡哈的科技示范户的卡",      "多多"},
                {"abc",   "非大煞",                                      "12",          "非得花时间咖啡哈的科技示",             "多"},
                {"abc",   "非大神焕发开始的机会附件都是",                   "123",         "非得花时间咖啡户的卡",                 "多多"},
                {"abc",   "非大煞风景的时刻发挥的精会附件都是",              "1234",        "非得花时间户的卡",                    "多福多"},
                {"abc",   "非大煞风景的时刻发挥的精发开始的机会附件都是",      "12345",      "非得花时间咖卡",                       "多福多fsdffgfdgdfgdf"},
                {"abcde", "非大煞都是",                                  "1234",        "非得花时间咖啡哈的科技示范户的卡萨肌肤", "多"},
                {"a",     "非是",                                       "12345678910", "非",                                "多福多寿肌肤"},
                {"abc",   "非大煞风景的时刻发挥的精神焕发开始的机会附件都是",  "1",           "非得花时间咖啡哈的科技示范户的卡",      "多多"},
                {"abc",   "非大煞",                                      "12",          "非得花时间咖啡哈的科技示",             "多"},
                {"abc",   "非大神焕发开始的机会附件都是",                   "123",         "非得花时间咖啡户的卡",                 "多多"},
                {"abc",   "非大煞风景的时刻发挥的精会附件都是",              "1234",        "非得花时间户的卡",                    "多福多"}
        };

        List<List<String>> list = new ArrayList<>();

        int size = columnValues.length;
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < columnValues.length; i++) {
                List item = new ArrayList();
                item.add((size * j + i) + "");
                for (int m = 1; m < columnValues[i].length; m++) {
                    item.add(columnValues[i][m]);
                }

                list.add(item);
            }
        }

        Log.d("view", " list = " + list.size());
        String[] title = new String[] {"序号", "姓名", "电话", "地区", "备注"};

        smartTable.setTextColor(Color.BLACK);
        smartTable.setTitleColor(Color.BLUE);
        smartTable.setLineColor(Color.RED);
        smartTable.setPadding(10);
        smartTable.setRowHeight(60);
        smartTable.setTextSize(30);
        smartTable.setBorder(1);
        smartTable.setMaxCellWidth(600);
        smartTable.setCopyColor(Color.GREEN);
        smartTable.setShowCopy(true);

        smartTable.setData(list, title);
    }
}
