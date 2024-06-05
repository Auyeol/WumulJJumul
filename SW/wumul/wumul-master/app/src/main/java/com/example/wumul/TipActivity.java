package com.example.wumul;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.squareup.picasso.Picasso;

public class TipActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.use_tip);

        int[] cardViewIds = {
                R.id.cardview1,
                R.id.cardview2,
                R.id.cardview3,
                R.id.cardview4,
                R.id.cardview5
        };

        int[] thumbnailIds = {
                R.id.thumbnail_1,
                R.id.thumbnail_2,
                R.id.thumbnail_3,
                R.id.thumbnail_4,
                R.id.thumbnail_5
        };

        String[] thumbnailUrls = {
                "https://b3065280.smushcdn.com/3065280/wp-content/uploads/2020/03/GSC_BP_MH_Water_Saving-Tips_20200321_1.jpg?lossy=0&strip=0&webp=1",
                "https://www.wikihow.com/images/thumb/7/7e/Save-Water-Step-1-Version-6.jpg/v4-728px-Save-Water-Step-1-Version-6.jpg.webp",
                "https://img.youtube.com/vi/KNvrjPZ0imQ/0.jpg",
                "https://img.youtube.com/vi/cZa6G0PT9r0/0.jpg",
                "https://img.youtube.com/vi/KSGbyO7S6H4/0.jpg"
        };

        for (int i = 0; i < cardViewIds.length; i++) {
            CardView cardView = findViewById(cardViewIds[i]);
            ImageView thumbnailImageView = findViewById(thumbnailIds[i]);

            String thumbnailUrl = thumbnailUrls[i];

            Picasso.get().load(thumbnailUrl).into(thumbnailImageView);

            final int position = i; // 클릭 이벤트에서 사용할 위치 정보


            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String blogUrl = getBlogUrl(position); // 위치에 따라 다른 블로그 URL 가져오기
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(blogUrl));
                    startActivity(intent);
                }
            });


        }
    }
    private String getBlogUrl(int position) {
        String blogUrl;

        switch (position) {
            case 0:
                blogUrl = "https://gscaltexmediahub.com/campaign/life-energy-water-saving-tips/";
                break;
            case 1:
                blogUrl = "https://ko.wikihow.com/%EB%AC%BC-%EC%A0%88%EC%95%BD%ED%95%98%EB%8A%94-%EB%B2%95";
                break;
            case 2:
                blogUrl = "https://www.youtube.com/watch?v=KNvrjPZ0imQ";
                break;
            case 3:
                blogUrl = "https://www.youtube.com/watch?v=cZa6G0PT9r0";
                break;
            case 4:
                blogUrl = "https://www.youtube.com/watch?v=KSGbyO7S6H4";
                break;
            default:
                blogUrl = ""; // 기본값으로 빈 문자열 반환
                break;
        }

        return blogUrl;
    }

}


