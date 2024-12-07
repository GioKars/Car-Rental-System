package com.example.car_rent;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("upload_image.php")
    Call<ResponseBody> uploadImage(
            @Part MultipartBody.Part image,
            @Part("userId") RequestBody userId,
            @Part("folderName") RequestBody folderName);  // Add folderName as a part
}


