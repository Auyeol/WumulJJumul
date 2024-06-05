package com.example.wumul;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.itangqi.waveloadingview.WaveLoadingView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main_Activity";
    private static final int MAX_CAPACITY = 110;
    WaveLoadingView waveLoadingView;
    private DatabaseReference mDatabase;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FrameLayout frameLayout;
    Long familyUsedSum = Long.valueOf(0);

    String currentDate = getCurrentDate();
    String monthDay = currentDate.substring(5); // 연도를 제외하고 월, 일만을 가져옵니다.
    String[] dateParts = monthDay.split("-");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = findViewById(R.id.frame_layout);

        waveLoadingView = findViewById(R.id.waveLoadingView);
        waveLoadingView.setAnimDuration(3000);
        waveLoadingView.setProgressValue(0);

        Intent intent = getIntent();
        int familyCount = intent.getIntExtra("familyCount", 0);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        String key = getIntent().getStringExtra("key");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            String uid = mAuth.getCurrentUser().getUid();
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Typeface typeface = ResourcesCompat.getFont(MainActivity.this, R.font.cafe24surround);

                    DataSnapshot familyMembersSnapshot = snapshot.child(uid).child("family_members");
                    long familyMembersCount = familyMembersSnapshot.getChildrenCount();

                    long totalCapacity = familyMembersCount * MAX_CAPACITY;
                    long totalUsage = 0;

                    frameLayout.removeAllViews();
                    for (DataSnapshot childSnapshot : familyMembersSnapshot.getChildren()) {
                        String key = childSnapshot.getKey();
                        DataSnapshot data = childSnapshot.child("monthly_usage").child(dateParts[0]).child(dateParts[1]);  // 수정된 부분

                        Long showerValue = data.child("shower").getValue(Long.class);
                        Long sinkValue = data.child("sink").getValue(Long.class);
                        Long sumValue = data.child("sum").getValue(Long.class);

                        if (showerValue != null) {
                            totalUsage += showerValue;
                        }

                        if (sinkValue != null) {
                            totalUsage += sinkValue;
                        }

                        if (sumValue != null) {
                            familyUsedSum += sumValue;
                        }
                    }

                    double totalUsageLiters = (double) totalUsage / 1000.0;
                    double percent = (totalUsageLiters / (double) totalCapacity) * 100;
                    double roundedPercent = Math.round(percent * 10) / 10.0;
                    int displayPercent = (int)(percent);
                    waveLoadingView.startAnimation();
                    waveLoadingView.setProgressValue(displayPercent);
                    waveLoadingView.setCenterTitle(roundedPercent + "%");

                    if (roundedPercent > 100.0) {
                        double excessPercent = roundedPercent - 100.0;
                        double roundExcessPercent = Math.round(excessPercent * 10) / 10.0;
                        int displayExcessPercent = (int) roundExcessPercent;

                        int centerTitleStrokeColor = ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_dark);
                        int centerTitleColor = ContextCompat.getColor(MainActivity.this, android.R.color.white);
                        waveLoadingView.setCenterTitleStrokeColor(centerTitleStrokeColor);
                        waveLoadingView.setCenterTitleSize(centerTitleColor);
                        int redColor = Color.parseColor("#FF6E6E");
                        waveLoadingView.setWaveColor(redColor);

                        waveLoadingView.setBorderColor(Color.parseColor("#FF4646"));

                        waveLoadingView.setProgressValue(displayExcessPercent);
                        waveLoadingView.setCenterTitle(roundExcessPercent + "% 초과");
                    }
                    frameLayout.addView(waveLoadingView);

                    ImageView imageView = new ImageView(MainActivity.this);
                    imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
                    imageView.setImageResource(R.drawable.waterfamily_resize);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    FrameLayout.LayoutParams imageLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    imageLayoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                    imageLayoutParams.topMargin = toolbar.getHeight();
                    imageView.setLayoutParams(imageLayoutParams);
                    frameLayout.addView(imageView);

                    TextView userSumTextView = new TextView(MainActivity.this);
                    userSumTextView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
                    userSumTextView.setText("우리 가족의 하루 총 사용량 \n" + String.valueOf(totalUsageLiters) + " L");
                    userSumTextView.setTextSize(30);
                    userSumTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
                    userSumTextView.setTypeface(typeface, Typeface.BOLD);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.gravity = Gravity.CENTER;
                    userSumTextView.setLayoutParams(layoutParams);
                    frameLayout.addView(userSumTextView);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // 처리 중 오류가 발생한 경우 여기에서 처리합니다.
                }
            });
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.menu_wateruse:
                        gotoActivity(MainActivity.class);
                        break;
                    case R.id.menu_people:
                        gotoActivity(UsedFamily.class);
                        break;
                    case R.id.menu_tips:
                        gotoActivity(TipActivity.class);
                        break;
                    case R.id.check_use:
                        gotoActivity(CheckUse.class);
                        break;
                    case R.id.check_graph:
                        gotoActivity(GraphActivity.class);
                        break;
                    case R.id.show_badge:
                        gotoActivity(BadgeActivity.class);
                        break;
                    case R.id.show_reward:
                        gotoActivity(RewardActivity.class);
                        break;
                    case R.id.nav_logout:
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        gotoActivity(LoginActivity.class);
                        finish();
                        break;

                }

                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                updateRewardPointIfDayChanged();

                return true;


            }
        });

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void gotoActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private String getUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }
    private String getCurrentDate() {
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(currentDate);
    }
    private void updateRewardPointIfDayChanged() {
        // SharedPreferences를 사용하여 마지막으로 업데이트한 날짜 저장
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String lastUpdateDate = sharedPreferences.getString("lastUpdateDate", "");

        // 현재 날짜 가져오기
        String currentDate = getCurrentDate();

        // 마지막 업데이트 날짜와 현재 날짜 비교
        if (!lastUpdateDate.equals(currentDate)) {
            // 하루가 지났으므로 업데이트 수행
            updateRewardPoints();

            // 업데이트한 날짜 저장
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("lastUpdateDate", currentDate);
            editor.apply();
        }
    }

    private void updateRewardPoints() {
        // reward_point 업데이트 로직 추가

        // Firebase Realtime Database에서 사용량 데이터 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userNode = mDatabase.child(uid);
            DatabaseReference rewardPointNode = userNode.child("reward_point");

            // 여기에서 사용량 데이터를 가져와 reward_point를 계산하고 Firebase에 업데이트합니다.
            // 사용량 데이터를 가지고 reward_point를 계산하고 Firebase에 업데이트합니다.

            // 예시: 사용량 데이터에서 reward_point 계산
            int calculatedRewardPoint = calculateRewardPoints();

            // Firebase에 reward_point 업데이트
            rewardPointNode.setValue(calculatedRewardPoint);
            Toast.makeText(this, "Reward Point가 업데이트되었습니다. 새로운 Reward Point: " + calculatedRewardPoint, Toast.LENGTH_SHORT).show();

            // 계산된 reward_point 값을 사용하여 필요한 작업을 수행

        }
    }

    // reward_point 계산 로직
    private int calculateRewardPoints() {
        // 사용량 데이터를 기반으로 reward_point를 계산
        // 필요한 계산 로직을 추가합니다.

        int rewardPoints = 0;

        // 사용량 데이터를 이용하여 rewardPoints 계산

        return rewardPoints;
    }

}