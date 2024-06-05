package com.example.wumul;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UserInfoActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private LinearLayout parentLayout;
    String currentDate = getCurrentDate();
    String monthDay = currentDate.substring(5);
    String[] dateParts = monthDay.split("-");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info);

        parentLayout = findViewById(R.id.parent_layout);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        String key = getIntent().getStringExtra("key");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            mDatabase.child(uid).child("family_members").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Long sinkValue = dataSnapshot.child("monthly_usage").child(dateParts[0]).child(dateParts[1]).child("sink").getValue(Long.class);
                        Long showerValue = dataSnapshot.child("monthly_usage").child(dateParts[0]).child(dateParts[1]).child("shower").getValue(Long.class);

                        if (sinkValue == null || showerValue == null) {
                            // sinkValue 또는 showerValue가 null인 경우 처리할 내용
                            return;
                        }

                        double sumValue = sinkValue + showerValue;

                        // mL을 L로 변환하여 소수점 자리수 설정
                        double sumValueInLiters = sumValue / 1000.0;
                        String formattedValue;
                        if (sumValueInLiters >= 10) {
                            formattedValue = String.format("%4.1f", sumValueInLiters);  // 소수점 한 자리까지 포맷팅
                        } else {
                            formattedValue = String.format("%4.2f", sumValueInLiters);  // 소수점 세 자리까지 포맷팅
                        }

                        Typeface typeface = ResourcesCompat.getFont(UserInfoActivity.this, R.font.cafe24surround);

                        TextView keyTextView = new TextView(UserInfoActivity.this);
                        LinearLayout.LayoutParams keyTextViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        keyTextViewParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                        keyTextView.setLayoutParams(keyTextViewParams);
                        keyTextView.setText(key + " 님은 현재");
                        keyTextView.setTextSize(40);
                        keyTextView.setTypeface(typeface);
                        parentLayout.addView(keyTextView);

                        TextView usedShowerTextView = new TextView(UserInfoActivity.this);
                        LinearLayout.LayoutParams usedShowerTextViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        usedShowerTextViewParams.setMargins(150, 50, 50, 0);
                        usedShowerTextView.setLayoutParams(usedShowerTextViewParams);
                        usedShowerTextView.setBackgroundResource(R.drawable.button2_pattern);
                        usedShowerTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.shower_resize, 0, 0, 0);
                        usedShowerTextView.setCompoundDrawablePadding(30);
                        usedShowerTextView.setText("샤워기에서 \n" + String.format("%.3f", showerValue / 1000.0).replaceAll("0*$", "").replaceAll("\\.$", "") + " L 만큼 사용했어요");
                        usedShowerTextView.setTextSize(20);
                        usedShowerTextView.setGravity(Gravity.CENTER);
                        usedShowerTextView.setTypeface(typeface);
                        parentLayout.addView(usedShowerTextView);

                        TextView usedWashstandTextView = new TextView(UserInfoActivity.this);
                        LinearLayout.LayoutParams usedWashstandTextViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        usedWashstandTextViewParams.setMargins(100, 50, 100, 0);
                        usedWashstandTextView.setLayoutParams(usedWashstandTextViewParams);
                        usedWashstandTextView.setBackgroundResource(R.drawable.button_pattern);
                        usedWashstandTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.washstand_resize, 0, 0, 0);
                        usedWashstandTextView.setCompoundDrawablePadding(30);
                        usedWashstandTextView.setText("세면대에서 \n" + String.format("%.3f", sinkValue / 1000.0).replaceAll("0*$", "").replaceAll("\\.$", "") + " L 만큼 사용했어요");
                        usedWashstandTextView.setTextSize(20);
                        usedWashstandTextView.setGravity(Gravity.CENTER);
                        usedWashstandTextView.setTypeface(typeface);
                        parentLayout.addView(usedWashstandTextView);

                        TextView usedTotalTextView = new TextView(UserInfoActivity.this);
                        LinearLayout.LayoutParams usedTotalTextViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        usedTotalTextViewParams.setMargins(50, 50, 150, 0);
                        usedTotalTextView.setLayoutParams(usedTotalTextViewParams);
                        usedTotalTextView.setBackgroundResource(R.drawable.button1_pattern);
                        usedTotalTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.waterdrop_resize, 0, 0, 0);
                        usedTotalTextView.setCompoundDrawablePadding(30);
                        usedTotalTextView.setText("총 사용량: " + formattedValue + " L");
                        usedTotalTextView.setTextSize(20);
                        usedTotalTextView.setGravity(Gravity.CENTER);
                        usedTotalTextView.setTypeface(typeface);
                        parentLayout.addView(usedTotalTextView);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // 처리 중 오류가 발생한 경우 여기에서 처리합니다.
                    // 예: 데이터베이스 읽기 실패
                }
            });
        }
    }

    private String getCurrentDate() {
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(currentDate);
    }
}