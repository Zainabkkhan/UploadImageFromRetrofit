package com.example.admin.uploadimagedemo;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Admin on 9/17/2018.
 */

public interface FileUploadService
{
    @Multipart
    @POST("storage/uploadFile")
    Call<ResponseBody> upload(@Part("description") RequestBody description, @Part MultipartBody.Part file);
}
