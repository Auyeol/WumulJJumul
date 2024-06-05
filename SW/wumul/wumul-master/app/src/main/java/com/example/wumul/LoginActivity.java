package com.example.wumul;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String TAG = "LoginActivity";


    private DatabaseReference mDatabase;
// ...

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        findViewById(R.id.login_button).setOnClickListener(onClickListener);
        findViewById(R.id.goto_signup).setOnClickListener(onClickListener);
        findViewById(R.id.goto_passwordReset).setOnClickListener(onClickListener);


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.login_button:
                    Log.w(TAG,"로그인 버튼 클릭");
                    Login();
                    break;
                case R.id.goto_signup:
                    gotoActivity(SignupActivity.class);
                    Log.d(TAG, "버튼 클릭됨");
                    break;
                case R.id.goto_passwordReset:
                    gotoActivity(PasswordResetActivity.class);
                    break;
            }
        }
    };

    public void Login() {
        String email = ((EditText) findViewById(R.id.user_ID)).getText().toString();
        String password = ((EditText) findViewById(R.id.user_PASSWARD)).getText().toString();


        if (email.length() > 0 && password.length() > 0) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "로그,,,로그인 성공");
                                Toast.makeText(getApplicationContext(),"로그인에 성공했습니다.",Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                                if(user != null) {
                                    String uid = user.getUid();
                                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                    mDatabase.child("users").child(uid).child("family_members").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                // Firebase Realtime Database에 데이터가 존재하는 경우
                                                gotoActivity(MainActivity.class);
                                                finish();
                                            } else {
                                                // Firebase Realtime Database에 데이터가 존재하지 않는 경우
                                                gotoActivity(CountFamilyActivity.class);
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    gotoActivity(CountFamilyActivity.class);
                                }
                                /*mDatabase.child("users").child(uid).child("family_members").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            // Firebase Realtime Database에 데이터가 존재하는 경우
                                            gotoActivity(MainActivity.class);
                                            finish();
                                        } else {
                                            // Firebase Realtime Database에 데이터가 존재하지 않는 경우
                                            gotoActivity(CountFamilyActivity.class);
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });*/

                                /*mDatabase.child("users").child("username").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e("firebase", "Error getting data", task.getException());
                                            gotoActivity(CountFamilyActivity.class);
                                        }
                                        else {
                                            Log.w("firebase","데이터 가져오기 성공");
                                            gotoActivity(MainActivity.class);
                                        }
                                    }

                                });*/

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "로그,,,로그인 실패", task.getException());
                                Toast.makeText(LoginActivity.this, "로그인에 실패했습니다.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else{
            Toast.makeText(getApplicationContext(),"이메일 또는 비밀번호를 입력해주세요.",Toast.LENGTH_SHORT).show();
        }
    }




    private void gotoActivity(Class c){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

//    private void gotoSignupActivity(){
//        Intent intent = new Intent(this,SignupActivity.class);
//        startActivity(intent);
//    }
    private void gotoMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        // 카운트패밀리 설정한 경우 메인으로 바로 넘어갈 코드 추가해야함
    }
//    private void gotoPasswordResetActivity(){
//        Intent intent = new Intent(this,PasswordResetActivity.class);
//        startActivity(intent);
//    }
}