package com.example.wumul;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PasswordResetActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "PasswordResetActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passwordreset_layout);

        findViewById(R.id.send_button).setOnClickListener(onClickListener);
        mAuth = FirebaseAuth.getInstance();

    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            currentUser.reload();
//        }
//    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.send_button:
                    Log.w(TAG,"비밀번호 초기화 버튼 클릭");
                    sendEmail();
                    break;
            }
        }
    };

    public void sendEmail() {
        String email = ((EditText)findViewById(R.id.user_ID)).getText().toString();

        if (email.length() > 0) {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "로그,,,이메일 전송 완료");
                                Toast.makeText(PasswordResetActivity.this, "이메일 전송 완료", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(PasswordResetActivity.this, "이메일 전송 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }else{
            Toast.makeText(getApplicationContext(),"이메일을 입력해주세요.",Toast.LENGTH_SHORT).show();
        }
    }




//    private void gotoSignupActivity(){
//        Intent intent = new Intent(this,SignupActivity.class);
//        startActivity(intent);
//    }
//    private void gotoMainActivity(){
//        Intent intent = new Intent(this,CountFamilyActivity.class);
//        startActivity(intent);
//    }
}