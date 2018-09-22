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
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.jar.Manifest;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
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
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i,"Select Picture"),PICK_IMAGE_FROM_GALLERY);

            }
        });

    }

    private void uploadFile(Uri fileUri)
    {
        RequestBody descriptionPart=RequestBody.create(MultipartBody.FORM, "Hello how are you?????");
        Log.e("Description",""+descriptionPart);
        File originalFile=FileUtils.getFile(MainActivity.this,fileUri);
        Log.e("Original File",""+originalFile);
       // File originalFile=File.

        RequestBody filePart=RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)),originalFile);
        Log.e("File Part",""+filePart);

        //  RequestBody filePart=RequestBody.create(MediaType.parse("/image*"),originalFile);
        MultipartBody.Part file=MultipartBody.Part.createFormData("file",originalFile.getName(),filePart);
        Log.e("file",""+file);

        OkHttpClient okHttpClient=new OkHttpClient.Builder()
                .readTimeout(200, TimeUnit.SECONDS)
                 .connectTimeout(10,TimeUnit.MINUTES)
                 .build();

        Retrofit.Builder builder=new Retrofit.Builder()
                .baseUrl("https://fileuploader-test.herokuapp.com/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit=builder.build();

        FileUploadService service=retrofit.create(FileUploadService.class);

        Call<ResponseBody> call = service.upload(descriptionPart, file);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
            {
                Log.e("Url","=="+call.request().url());
                try {
                    Log.e("Upload", "="+response.body().string());
                    Toast.makeText(MainActivity.this, ""+response.body().string(), Toast.LENGTH_SHORT).show();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

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
        Log.e("intent data",""+data);
        if(requestCode==PICK_IMAGE_FROM_GALLERY && resultCode==RESULT_OK && data!=null)
        {
            if(data.getClipData() !=null)
            {
                int count=data.getClipData().getItemCount();
                Log.e("Count",""+count);
                int currentItem=0;
                while (currentItem<count)
                {
                    Uri uri=data.getClipData().getItemAt(currentItem).getUri();
                    currentItem=currentItem+1;
                    Log.e("CurrentItem","="+currentItem);
                    Log.e("Uri selected","="+uri.toString());
                    uploadFile(uri);
                }
            }
            else if (data.getData() !=null)
            {
            Uri uri = data.getData();
            uploadFile(uri);
        }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "Request Access", Toast.LENGTH_SHORT).show();
                }else
                {
                    Toast.makeText(this, "Request denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
