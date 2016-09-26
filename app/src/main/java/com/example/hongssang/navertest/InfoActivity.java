package com.example.hongssang.navertest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        findViewById(R.id.info_close).setOnClickListener(this);

        Intent intent = getIntent();
        TextView name = (TextView) findViewById(R.id.info_name);
        TextView call = (TextView) findViewById(R.id.info_call);
        TextView menu = (TextView) findViewById(R.id.info_menu);
        ImageView image = (ImageView) findViewById(R.id.info_image);

        name.setText(intent.getStringExtra("name"));
        call.setText(intent.getStringExtra("call"));
        menu.setText(intent.getStringExtra("menu"));
        Uri imageUri = intent.getParcelableExtra("image");

        try{
            Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            image.setImageBitmap(image_bitmap);
        }catch (FileNotFoundException e){
            Log.e("Error", "FileNotFoundException");
            e.printStackTrace();
        }catch (IOException e){
            Log.e("Error", "IOException");
            e.printStackTrace();
        }catch (Exception e){
            Log.e("Error", "AnotherException");
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_close:
                this.finish();
                break;
        }
    }
}
