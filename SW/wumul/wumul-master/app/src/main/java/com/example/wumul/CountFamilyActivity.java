package com.example.wumul;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class CountFamilyActivity extends AppCompatActivity {

    private TextView family_count_text;
    private ImageButton plus_button, minus_button;

    int familyCount = 0;


    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.count_family);


        family_count_text = findViewById(R.id.count_text);
        family_count_text.setText(familyCount+"");
        plus_button = findViewById(R.id.plus_button);
        minus_button = findViewById(R.id.minus_button);

        plus_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "onClick: btnAdd : "+v.getClass().getName());
                familyCount++;
                family_count_text.setText(familyCount+"");
            }
        });

        minus_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(familyCount>0) {
                    familyCount--;
                    family_count_text.setText(familyCount + "");
                }
            }
        });





    }

    public void onSaveFamilyCountClicked(View view){
        int familyCount = Integer.parseInt(family_count_text.getText().toString());
        Intent intent = new Intent(CountFamilyActivity.this,PersonEdit.class);
        intent.putExtra("familyCount", familyCount);

        startActivity(intent);

        // setResult 메서드를 호출하여 결과값을 저장
        //setResult(Activity.RESULT_OK, intent);

        // CountFamilyActivity 종료
        //finish();
    }

}