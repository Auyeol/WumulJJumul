package com.example.wumul;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PersonEdit extends AppCompatActivity {
    private int familyCount;
    LinearLayout editPerson;
    private Button confirmButton;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    ArrayList<String> strList;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.persondialog);

        strList = new ArrayList<String>();
        confirmButton = findViewById(R.id.confirm_button);
        editPerson = findViewById(R.id.Person);
        Intent intent = getIntent();

        familyCount = intent.getIntExtra("familyCount", 0);

        for(int i = 0; i < familyCount; i++) {
            // Create an EditText for the family member's name
            EditText etName = new EditText(getApplicationContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            etName.setLayoutParams(params);
            etName.setHint("구성원" + (i+1));
            etName.setId(i);
            editPerson.addView(etName);
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(familyCount > 0) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    String uid = user.getUid();
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();


                    mDatabase.child("flag").setValue(0);
                    mDatabase.child("head").setValue(0);
                    mDatabase.child("sink").setValue(0);

                    for (int i = 0; i < familyCount; i++) {
                        EditText et = (EditText) editPerson.getChildAt(i);
                        String text = et.getText().toString();
                        Log.d("PersonEdit", "EditText " + i + ": " + text);


                        if (user != null) {
                            String pathString = text;
                            DatabaseReference userNode = mDatabase.child("users").child(uid);
                            DatabaseReference familyMemberNode = userNode.child("family_members").child(pathString);
                            // 현재 월과 일을 가져옵니다.

                            String currentMonth = getCurrentMonth();
                            String currentDay = getCurrentDay();

                            DatabaseReference rewardPointNode = userNode.child("reward_point");
                            rewardPointNode.setValue(0);
                            DatabaseReference rewardSumNode = userNode.child("reward_sum");
                            rewardSumNode.setValue(0);

                            // Create monthly nodes (1월~12월)
                            for (int month = 1; month <= 12; month++) {
                                DatabaseReference monthlyNode = familyMemberNode
                                        .child("monthly_usage")
                                        .child(String.format("%02d", month));

                                Map<String, Object> data = new HashMap<>();
                                data.put("month_sink", 0);
                                data.put("month_shower", 0);
                                data.put("month_sum", 0);

                                monthlyNode.setValue(data);
                            // 한달 동안의 총 사용량
                                // Create daily nodes (1일~30일)
                                for (int day = 1; day <= 30; day++) {
                                    DatabaseReference dailyNode = monthlyNode
                                            .child(String.format("%02d", day));

                                    Map<String, Object> dailyData = new HashMap<>();
                                    dailyData.put("sink", 0);
                                    dailyData.put("shower", 0);
                                    dailyData.put("sum", 0);

                                    dailyNode.setValue(dailyData);
                                }
                            }

                            Toast.makeText(getApplicationContext()," 정보 저장됨", Toast.LENGTH_SHORT).show();
                        }
                    }

// Create flag, head, sink nodes



                    // Redirect to MainActivity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                } else {
                    Toast.makeText(getApplicationContext(),"EditText 박스 만들기 버튼을 눌러 박스를 만들어 주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private String getCurrentMonth() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        return monthFormat.format(Calendar.getInstance().getTime());
    }

    private String getCurrentDay() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
        return dayFormat.format(Calendar.getInstance().getTime());
    }

}
