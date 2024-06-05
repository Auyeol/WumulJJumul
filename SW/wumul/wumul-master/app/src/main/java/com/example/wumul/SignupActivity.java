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

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "SignupActivity";


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);

        findViewById(R.id.create_account_button).setOnClickListener(onClickListener);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }


    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            switch(v.getId()){
                case R.id.create_account_button:
                    signUp();
                    Log.d(TAG,"회원가입 버튼 클릭됨");
                    break;

            }
        }
    };

    public void signUp(){
        String email = ((EditText)findViewById(R.id.create_user_ID)).getText().toString();
        String password = ((EditText)findViewById(R.id.create_user_PASSWARD)).getText().toString();
        String checkpassword = ((EditText)findViewById(R.id.check_user_PASSWARD)).getText().toString();

        if(email.length()>0 && password.length()>0 && checkpassword.length()>0){
            if(password.equals(checkpassword)) {

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "로그,,,로그인 성공");
                                    Toast.makeText(SignupActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    gotoActivity(LoginActivity.class);
//                       updateUI(user);
                                    // 로그인 성공
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "로그,,,로그인 실패", task.getException());
                                    Toast.makeText(SignupActivity.this, "회원가입에 실패하였습니다. 다른 이메일 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();

//                       updateUI(null);
                                    // 실패했을 때
                                }
                            }
                        });
            }else{
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
    }else{
            Toast.makeText(this, "이메일 또는 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
        }
    }



    private void gotoActivity(Class c){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
