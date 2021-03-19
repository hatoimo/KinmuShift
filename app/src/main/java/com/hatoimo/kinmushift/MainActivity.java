package com.hatoimo.kinmushift;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private int year, month;
    private String groupNum;
    private TextView yearMonth;
    private TextView[][] weekList = new TextView[6][7];
    private TextView[][] shiftList = new TextView[6][7];

    private static final String[][] shiftPattern = {
            {"2","2","2","休","1","1","1","1","休","3","3","3","3","休","休","2"},
            {"3","休","休","2","2","2","2","休","1","1","1","1","休","3","3","3"},
            {"休","3","3","3","3","休","休","2","2","2","2","休","1","1","1","1"},
            {"1","1","1","1","休","3","3","3","3","休","休","2","2","2","2","休"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* プリファレンス取得 */
        sharedPref = getPreferences(MODE_PRIVATE);
        groupNum = sharedPref.getString("group", "A");

        /* 現在年月 */
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;

        /* ビューの設定 */
        Resources res = getResources();
        int viewId;
        TextView view;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                /* 日付ビュー */
                viewId = res.getIdentifier("week" + i + "_" + j, "id", getPackageName());
                view = (TextView)findViewById(viewId);
                weekList[i][j] = view;

                /* シフトビュー */
                viewId = res.getIdentifier("shift" + i + "_" + j, "id", getPackageName());
                view = (TextView)findViewById(viewId);
                shiftList[i][j] = view;
            }
        }

        /* 年月 */
        yearMonth = (TextView)findViewById(R.id.yearMonth);

        /* 前月ボタン押下 */
        findViewById(R.id.prevBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                month--;
                if (month <= 0) {
                    year--;
                    month = 12;
                }
                showCalendar();
            }
        });

        /* 次月ボタン押下 */
        findViewById(R.id.nextBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                month++;
                if (month > 12) {
                    year++;
                    month = 1;
                }
                showCalendar();
            }
        });

        /* グループ押下 */
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    // 選択されているラジオボタンの取得
                    RadioButton radioButton = (RadioButton) findViewById(checkedId);
                    // ラジオボタンのテキストを取得
                    groupNum = radioButton.getText().toString();
                    setRadioGroup();
                } else {
                    // 何も選択されていない場合の処理
                }
            }
        });

        /* グループ選択（初期表示） */
        setRadioGroup();
    }

    /* グループを選択する */
    private void setRadioGroup() {

        RadioGroup group = (RadioGroup)findViewById(R.id.radioGroup);
        switch (groupNum) {
            case "A": group.check(R.id.radioA);   break;
            case "B": group.check(R.id.radioB);   break;
            case "C": group.check(R.id.radioC);   break;
            case "D": group.check(R.id.radioD);   break;
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("group", String.valueOf(groupNum));
        editor.commit();

        showCalendar();
    }

    /* カレンダーを更新する */
    private void showCalendar() {

        /* 表示年月の１日 */
        Calendar cl = Calendar.getInstance();
        cl.set(year, month - 1, 1);

        int week = cl.get(Calendar.DAY_OF_WEEK) - 1;                // 1日の曜日
        int lastDay = cl.getActualMaximum(Calendar.DAY_OF_MONTH);   // 月末日

        /* 1日のシフトを取得 */
        Calendar base = Calendar.getInstance();
        base.set(2021, 4 - 1, 1);
        int diffCnt = getDiffDays(cl, base) % 16;
        if (diffCnt < 0) {
            diffCnt = 16 - (Math.abs(diffCnt) % 16);
        }

        /* 表示グループ */
        int groupPattern = 0;
        switch (groupNum) {
            case "A": groupPattern = 0;   break;
            case "B": groupPattern = 1;   break;
            case "C": groupPattern = 2;   break;
            case "D": groupPattern = 3;   break;
        }

        /* シフト表示 */
        Calendar toCal = Calendar.getInstance();
        int toDay = toCal.get(Calendar.DATE);
        int dayCnt = 1 - week;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                TextView weekView = weekList[i][j];
                TextView shiftView = shiftList[i][j];
                if (dayCnt > 0) {
                    if (dayCnt > lastDay) {
                        weekView.setText("");
                        shiftView.setText("");
                    } else {
                        weekView.setText(String.valueOf(dayCnt));
                        shiftView.setText(shiftPattern[groupPattern][diffCnt % 16]);
                        weekView.setBackgroundColor(Color.WHITE);
                        shiftView.setBackgroundColor(Color.WHITE);
                        if (toDay == dayCnt) {
                            /* 今日を黄色表示 */
                            if (year == toCal.get(Calendar.YEAR) && month == toCal.get(Calendar.MONTH) + 1) {
                                weekView.setBackgroundColor(Color.YELLOW);
                                shiftView.setBackgroundColor(Color.YELLOW);
                            }
                        }
                    }
                    diffCnt++;
                } else {
                    weekView.setText("");
                    shiftView.setText("");
                }
                dayCnt++;
            }
        }

        /* 表示年月 */
        yearMonth.setText(year + "年" + String.format("%02d", month) + "月");
    }

    /* 経過日数を取得する */
    private int getDiffDays(Calendar calendar1, Calendar calendar2) {
        //==== ミリ秒単位での差分算出 ====//
        long diffTime = calendar1.getTimeInMillis() - calendar2.getTimeInMillis();

        //==== 日単位に変換 ====//
        int MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
        int diffDays = (int)(diffTime / MILLIS_OF_DAY);

        return diffDays;
    }
}