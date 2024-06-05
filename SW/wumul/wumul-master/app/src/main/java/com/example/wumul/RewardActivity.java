package com.example.wumul;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RewardActivity extends AppCompatActivity {

    private ImageView waterDropFilledImageView;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private TextView textView;
    private TextView percentageTextView;

    private DatabaseReference rewardPointNode;
    private DatabaseReference rewardSumNode;

    private static final int MAX_PROGRESS = 20000; // 프로그래스바의 Max 값

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reward_layout);

        waterDropFilledImageView = findViewById(R.id.waterDropFilledImageView);
        progressBar = findViewById(R.id.progressBar);
        frameLayout = findViewById(R.id.reward_frame_layout);
        textView = findViewById(R.id.textView_ex);
        percentageTextView = findViewById(R.id.percentageTextView);

        // 프로그래스 바의 Max 값을 설정
        progressBar.setMax(MAX_PROGRESS);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            rewardPointNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("users")
                    .child(user.getUid())
                    .child("reward_point");

            rewardSumNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("users")
                    .child(user.getUid())
                    .child("reward_sum");

            waterDropFilledImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    increaseDecreasePoints();
                }
            });

            // ValueEventListener를 통해 reward_point의 변화를 감지하여 UI 업데이트
            rewardPointNode.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long rewardPointValue = snapshot.getValue(Long.class);

                    if (rewardPointValue != null) {
                        updateTextView(rewardPointValue);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // 처리 중 오류가 발생한 경우 처리
                }
            });

            // ValueEventListener를 통해 reward_sum의 변화를 감지하여 UI 업데이트
            rewardSumNode.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long rewardSumValue = snapshot.getValue(Long.class);

                    if (rewardSumValue != null) {
                        updateProgressBar(rewardSumValue);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // 처리 중 오류가 발생한 경우 처리
                }
            });
        }
    }

    private void increaseDecreasePoints() {
        // ValueEventListener를 통해 reward_point 값을 가져옴
        rewardPointNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long rewardPointValue = snapshot.getValue(Long.class);

                if (rewardPointValue != null) {
                    if (rewardPointValue >= 50) {
                        // reward_point를 50 감소
                        rewardPointNode.setValue(Math.max(0, rewardPointValue - 50));

                        // reward_sum을 50 증가
                        rewardSumNode.setValue(getCurrentRewardSum() + 50);
                    } else {
                        // 포인트가 50 미만인 경우 알림 표시
                        Toast.makeText(RewardActivity.this, "포인트가 모자랍니다. 최소 포인트: 50", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 처리 중 오류가 발생한 경우 처리
            }
        });
    }

    private long getCurrentRewardPoint() {
        // reward_point의 현재 값을 가져옴
        return textView.getText().toString().contains(":") ?
                Long.parseLong(textView.getText().toString().split(":")[1].trim()) : 0;
    }

    private long getCurrentRewardSum() {
        // reward_sum의 현재 값을 가져옴
        return progressBar.getProgress();
    }

    private void updateTextView(long rewardPointValue) {
        // 텍스트뷰 업데이트
        textView.setText("포인트: " + rewardPointValue);


        Typeface typeface = ResourcesCompat.getFont(this, R.font.cafe24surround);
        textView.setTypeface(typeface);
    }

    private void updateProgressBar(long rewardSumValue) {
        // 프로그래스 바 업데이트
        progressBar.setProgress((int) rewardSumValue);

        // 퍼센티지 텍스트 업데이트
        int percentage = (int) ((double) rewardSumValue / MAX_PROGRESS * 100);
        percentageTextView.setText(percentage + "%");
        Typeface typeface = ResourcesCompat.getFont(this, R.font.cafe24surround);
        percentageTextView.setTypeface(typeface);

        if (rewardSumValue >= MAX_PROGRESS) {
            // 프로그래스 바가 100%에 도달하면 이미지를 띄워줌
            showGiftCardImage();
            rewardSumNode.setValue(0);
        }
    }

    private void showGiftCardImage() {
        // 현재 액티비티가 종료되었는지 확인
        if (isFinishing()) {
            return;
        }

        // 기프티콘 이미지를 설정
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.rewardreal_resize); // 이미지 리소스를 적절한 이미지로 변경

        // 이미지를 감싸는 레이아웃 생성
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(imageView);

        // AlertDialog 생성 및 레이아웃 설정
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.cafe24surround);
        builder.setMessage("절약 성공! 기프티콘 이미지를 띄웁니다\n\n이미지는 저장되지 않으니 캡쳐 후 창을 종료해주세요.");
        builder.setView(layout);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // 현재 액티비티가 종료되었는지 최종 확인
        if (!isFinishing()) {
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

}
