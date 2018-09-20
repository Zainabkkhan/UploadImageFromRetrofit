package com.example.admin.uploadimagedemo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.net.URI;
import java.util.jar.Manifest;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int MY_PERMISSION_REQUEST = 100; // onActivityResult request

    private int PICK_IMAGE_FROM_GALLERY=1;
    // code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);

        }
        Button btn=findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent i=new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i,"Select Picture"),PICK_IMAGE_FROM_GALLERY);

            }
        });
//        setContentView(button);

    }

//    private void showChooser()
//    {
//        // Use the GET_CONTENT intent from the utility class
//        Intent target = FileUtils.createGetContentIntent();
//        // Create the chooser Intent
//        Intent intent = Intent.createChooser(target, getString(R.string.chooser_title));
//        try
//        {
//            startActivityForResult(intent, MY_PERMISSION_REQUEST);
//        } catch (ActivityNotFoundException e) {
//            // The reason for the existence of aFileChooser
//        }
//    }

    private void uploadFile(Uri fileUri)
    {
        RequestBody descriptionPart=RequestBody.create(MultipartBody.FORM, "Hello how are you?????");
        File originalFile=FileUtils.getFile(MainActivity.this,fileUri);
        RequestBody filePart=RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)),originalFile);
        MultipartBody.Part file=MultipartBody.Part.createFormData("Photo",originalFile.getName(),filePart);

        Retrofit.Builder builder=new Retrofit.Builder().baseUrl("https://file-uploaders.herokuapp.com").addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit=builder.build();
        FileUploadService service=retrofit.create(FileUploadService.class);

        Call<ResponseBody> call = service.upload(descriptionPart, file);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
            {
                Log.e("Url","=="+call.request().url());
                Log.e("Upload", "success"+response.body());
                Toast.makeText(MainActivity.this, ""+response.body(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_FROM_GALLERY && resultCode==RESULT_OK && data!=null && data.getData() !=null)
        {
            Uri uri= data.getData();
            uploadFile(uri);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Request Access", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Request denied", Toast.LENGTH_SHORT).show();

                }
                break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
