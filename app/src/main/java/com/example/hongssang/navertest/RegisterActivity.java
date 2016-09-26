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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    final int REQ_CODE_SELECT_IMAGE = 100;
    double xvalue, yvalue;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViewById(R.id.register_register).setOnClickListener(this);
        findViewById(R.id.register_close).setOnClickListener(this);
        findViewById(R.id.register_image).setOnClickListener(this);

        setResult(0);
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.register_close:
                this.finish();
                break;
            case R.id.register_register:
                Log.v("REGISTER", "clicked");
                Intent resultIntent = new Intent();
                resultIntent.putExtra("name", ((TextView)findViewById(R.id.register_name)).getText().toString());
                resultIntent.putExtra("call", ((TextView)findViewById(R.id.register_call)).getText().toString());
                resultIntent.putExtra("menu", ((TextView)findViewById(R.id.register_menu)).getText().toString());
                resultIntent.putExtra("image", imageUri);
                setResult(1, resultIntent);
                finish();
                break;
            case R.id.register_image:
                Log.v("IMAGE", "clicked");
                Intent reqImgIntent = new Intent(Intent.ACTION_PICK);
                reqImgIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                reqImgIntent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(reqImgIntent, REQ_CODE_SELECT_IMAGE);
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == Activity.RESULT_OK){
            try{
                imageUri = data.getData();

                Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ImageView image = (ImageView) findViewById(R.id.register_image);

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
    }
}
