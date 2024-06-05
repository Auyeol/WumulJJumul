package com.example.wumul;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private LinearLayout mFamilyMembersLayout;
    private BarChart mDailyBarChart;
    private String uid;
    private DataSnapshot snapshot;  // 추가

    String currentDate = getCurrentDate();
    String monthDay = currentDate.substring(5);
    String[] dateParts = monthDay.split("-");
    List<String> familyMemberLabels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        mFamilyMembersLayout = findViewById(R.id.graph_textView_layout);
        mDailyBarChart = findViewById(R.id.daily_bar_chart);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mFamilyMembersLayout.removeAllViews();
                GraphActivity.this.snapshot = snapshot; // snapshot을 초기화
                showMultiGraphForFamilyMembers(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showMultiGraphForFamilyMembers(DataSnapshot snapshot) {
        BarChart barChart = findViewById(R.id.daily_bar_chart);
        barChart.clear();

        List<BarEntry> showerEntries = new ArrayList<>();
        List<BarEntry> sinkEntries = new ArrayList<>();
        List<BarEntry> sumEntries = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            int index = 0;
            float barWidth = 0.2f;
            float barSpace = 0.02f;
            float groupSpace = 0.3f;

            for (DataSnapshot familyMemberSnapshot : snapshot.child(uid).child("family_members").getChildren()) {
                String key = familyMemberSnapshot.getKey();
                familyMemberLabels.add(key);

                DataSnapshot monthlyUsageSnapshot = familyMemberSnapshot.child("monthly_usage").child(dateParts[0]).child(dateParts[1]);

                long showerValue = getValueFromSnapshot(monthlyUsageSnapshot.child("shower"));
                long sinkValue = getValueFromSnapshot(monthlyUsageSnapshot.child("sink"));
                long sumValue = getValueFromSnapshot(monthlyUsageSnapshot.child("sum"));

                showerEntries.add(new BarEntry(index, showerValue));
                sinkEntries.add(new BarEntry(index, sinkValue));
                sumEntries.add(new BarEntry(index, sumValue));

                index++;
            }

            BarDataSet showerDataSet = createBarDataSet(showerEntries, "Shower", Color.rgb(255, 102, 102));
            BarDataSet sinkDataSet = createBarDataSet(sinkEntries, "Sink", Color.rgb(102, 178, 255));
            BarDataSet sumDataSet = createBarDataSet(sumEntries, "Sum", Color.rgb(102, 255, 178));

            List<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(showerDataSet);
            dataSets.add(sinkDataSet);
            dataSets.add(sumDataSet);

            BarData barData = new BarData(dataSets);
            barChart.setData(barData);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(familyMemberLabels));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setLabelRotationAngle(45);

            barData.setBarWidth(barWidth);
            float groupWidth = barData.getGroupWidth(groupSpace, barSpace) * familyMemberLabels.size();
            barChart.getXAxis().setAxisMinimum(0);
            barChart.getXAxis().setAxisMaximum(0 + groupWidth);
            barChart.getXAxis().setCenterAxisLabels(true);

            barChart.groupBars(0, groupSpace, barSpace);
            barChart.invalidate();

            for (String key : familyMemberLabels) {
                LinearLayout itemLayout = createItemLayout(key, snapshot.child(uid).child("family_members").child(key));
                mFamilyMembersLayout.addView(itemLayout);
            }


//            // 동적으로 버튼 생성 및 추가
//            Button dynamicButton = new Button(this);
//            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT);
//            dynamicButton.setLayoutParams(buttonParams);
//            dynamicButton.setText("Dynamic Button");
//            mFamilyMembersLayout.addView(dynamicButton);
        }
    }


    private BarDataSet createBarDataSet(List<BarEntry> entries, String label, int color) {
        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        return dataSet;
    }

    private LinearLayout createItemLayout(String key, DataSnapshot dataSnapshot) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;

        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setLayoutParams(layoutParams);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button familyButton = new Button(this);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.weight = 1;
        familyButton.setLayoutParams(buttonParams);

        familyButton.setBackgroundResource(R.drawable.button3_pattern);
        familyButton.setTextColor(getResources().getColor(R.color.white));
        familyButton.setTextSize(getResources().getDimension(R.dimen.button_text_size_small)); // 작은 텍스트 크기 설정
        Typeface typeface = ResourcesCompat.getFont(this, R.font.cafe24surround);
        familyButton.setTypeface(typeface);

        // key를 버튼에 setText
        familyButton.setText(key);

        // 버튼 클릭 리스너 설정
        familyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // key를 사용하여 원하는 작업 수행
                String selectedKey = ((Button) view).getText().toString();
                Log.d("SelectedKey", selectedKey);
                showLineGraphForFamilyMember(selectedKey);
                showMonthlyGraphForFamilyMember(selectedKey);
            }
        });

        // itemLayout에 버튼 추가
        itemLayout.addView(familyButton);

        return itemLayout;
    }

    private void showLineGraphForFamilyMember(String familyMemberKey) {
        if (snapshot == null) {
            Log.d("Snapshot", "Snapshot is null");
            Toast.makeText(this, "Data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        LineChart dailyLineChart = findViewById(R.id.daily_line_chart);
        dailyLineChart.clear();

        List<Entry> showerEntries = new ArrayList<>();
        List<Entry> sinkEntries = new ArrayList<>();
        List<Entry> sumEntries = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // 해당 월의 데이터를 가져옴
            DataSnapshot monthlyUsageSnapshot = snapshot.child(uid).child("family_members").child(familyMemberKey).child("monthly_usage").child(dateParts[0]);

            // 해당 월의 마지막 날짜를 계산
            int lastDayOfMonth = getLastDayOfMonth(Integer.parseInt(dateParts[0]));

            // 1일부터 마지막 날짜까지의 데이터가 있는지 확인하고, 없으면 0으로 처리
            for (int day = 1; day <= lastDayOfMonth; day++) {
                String dayKey = String.format("%02d", day);  // 1일부터 9일까지는 01, 02, ..., 30까지 2자리로 표현
                DataSnapshot daySnapshot = monthlyUsageSnapshot.child(dayKey);

                long showerValue = getValueFromSnapshot(daySnapshot.child("shower"));
                long sinkValue = getValueFromSnapshot(daySnapshot.child("sink"));
                long sumValue = getValueFromSnapshot(daySnapshot.child("sum"));

                // Entry에 날짜와 값을 추가
                showerEntries.add(new Entry(day, showerValue));
                sinkEntries.add(new Entry(day, sinkValue));
                sumEntries.add(new Entry(day, sumValue));
            }

            LineDataSet showerDataSet = createLineDataSet(showerEntries, "샤워 사용량", Color.rgb(255, 102, 102));
            LineDataSet sinkDataSet = createLineDataSet(sinkEntries, "세면대 사용량", Color.rgb(102, 178, 255));
            LineDataSet sumDataSet = createLineDataSet(sumEntries, "총 사용량", Color.rgb(102, 255, 178));

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(showerDataSet);
            dataSets.add(sinkDataSet);
            dataSets.add(sumDataSet);

            LineData lineData = new LineData(dataSets);
            dailyLineChart.setData(lineData);

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            float screenWidth = displayMetrics.widthPixels;

            // X축 설정
            XAxis xAxis = dailyLineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);  // 1일 간격으로 표시
            xAxis.setLabelCount(lastDayOfMonth);   // 라벨 갯수를 해당 월의 마지막 날짜로 설정
            xAxis.setGranularityEnabled(true);
            xAxis.setLabelRotationAngle(45); // 라벨 회전


            configureLineDataSet(showerDataSet, Color.rgb(255, 102, 102));
            configureLineDataSet(sinkDataSet, Color.rgb(102, 178, 255));
            configureLineDataSet(sumDataSet, Color.rgb(102, 255, 178));

            // Y축 설정
            YAxis leftAxis = dailyLineChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f);

            // 그래프 보여주기
            dailyLineChart.invalidate();
            dailyLineChart.animateX(1000); // 1초 동안 X축 방향으로 애니메이션

        } else {
            // snapshot이 null인 경우에 대한 처리
            Toast.makeText(this, "Data not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMonthlyGraphForFamilyMember(String familyMemberKey) {
        if (snapshot == null) {
            Log.d("Snapshot", "Snapshot is null");
            Toast.makeText(this, "Data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        LineChart monthlyLineChart = findViewById(R.id.monthly_line_chart);
        monthlyLineChart.clear();

        List<Entry> showerEntries = new ArrayList<>();
        List<Entry> sinkEntries = new ArrayList<>();
        List<Entry> sumEntries = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // 해당 월의 데이터를 가져옴
            DataSnapshot monthlyUsageSnapshot = snapshot.child(uid).child("family_members").child(familyMemberKey).child("monthly_usage");

            // 마지막 날짜를 가져오지 않고, 해당 월의 데이터만 가져옴
            for (DataSnapshot daySnapshot : monthlyUsageSnapshot.getChildren()) {
                String dayKey = daySnapshot.getKey();
                long showerValue = getValueFromSnapshot(daySnapshot.child("month_shower"));
                long sinkValue = getValueFromSnapshot(daySnapshot.child("month_sink"));
                long sumValue = getValueFromSnapshot(daySnapshot.child("month_sum"));

                // Entry에 날짜와 값을 추가
                showerEntries.add(new Entry(Integer.parseInt(dayKey), showerValue));
                sinkEntries.add(new Entry(Integer.parseInt(dayKey), sinkValue));
                sumEntries.add(new Entry(Integer.parseInt(dayKey), sumValue));
            }

            LineDataSet showerDataSet = createLineDataSet(showerEntries, "샤워 사용량", Color.rgb(255, 102, 102));
            LineDataSet sinkDataSet = createLineDataSet(sinkEntries, "세면대 사용량", Color.rgb(102, 178, 255));
            LineDataSet sumDataSet = createLineDataSet(sumEntries, "총 사용량", Color.rgb(102, 255, 178));

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(showerDataSet);
            dataSets.add(sinkDataSet);
            dataSets.add(sumDataSet);

            LineData lineData = new LineData(dataSets);
            monthlyLineChart.setData(lineData);

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            float screenWidth = displayMetrics.widthPixels;

            // X축 설정
            XAxis xAxis = monthlyLineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);  // 1일 간격으로 표시
            xAxis.setLabelCount((int) Math.round(monthlyUsageSnapshot.getChildrenCount()));
            xAxis.setGranularityEnabled(true);
            xAxis.setLabelRotationAngle(45); // 라벨 회전

            configureLineDataSet(showerDataSet, Color.rgb(255, 102, 102));
            configureLineDataSet(sinkDataSet, Color.rgb(102, 178, 255));
            configureLineDataSet(sumDataSet, Color.rgb(102, 255, 178));

            // Y축 설정
            YAxis leftAxis = monthlyLineChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f);

            // 그래프 보여주기
            monthlyLineChart.invalidate();
            monthlyLineChart.animateX(1000); // 1초 동안 X축 방향으로 애니메이션
        } else {
//             snapshot이 null인 경우에 대한 처리
            Toast.makeText(this, "Data not available", Toast.LENGTH_SHORT).show();
        }
    }


    // 해당 월의 마지막 날짜를 반환하는 메소드
    private int getLastDayOfMonth(int yearMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, yearMonth / 100);  // 년도 설정
        calendar.set(Calendar.MONTH, yearMonth % 100 - 1);  // 월 설정 (0-based, 1을 빼야 함)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);  // 해당 월의 마지막 날짜 반환
    }


    private void configureLineDataSet(LineDataSet dataSet, int color) {
        dataSet.setLineWidth(2f); // 라인 두께
        dataSet.setCircleRadius(4f); // 점의 크기
        dataSet.setCircleColor(color); // 점의 색상
        dataSet.setDrawValues(true); // 값 표시
        dataSet.setValueTextSize(10f); // 값 표시 크기
        dataSet.setColor(color); // 라인 색상

        // 추가: 그래프에 그림자 효과
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(50); // 투명도 (0-255)
        dataSet.setFillColor(color); // 채우기 색상
    }


    private LineDataSet createLineDataSet(List<Entry> entries, String label, int color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setDrawCircles(false); // 점 표시 안 함
        dataSet.setDrawValues(true); // 값 표시
        dataSet.setDrawIcons(false); // 아이콘 표시 안 함
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 꺽은선 그래프 형태
        dataSet.setCubicIntensity(0.2f); // 꺾이는 정도
        dataSet.setLineWidth(2f); // 선 두께

        return dataSet;
    }




    private long getValueFromSnapshot(DataSnapshot snapshot) {
        return snapshot.exists() ? snapshot.getValue(Long.class) : 0;
    }

    // 현재 날짜를 가져오는 메소드
    private String getCurrentDate() {
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(currentDate);
    }
}
