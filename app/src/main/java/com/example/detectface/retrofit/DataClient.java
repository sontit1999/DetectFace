package com.example.detectface.retrofit;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface DataClient {
    @Multipart
    @POST
    Call<String> UploadPhoto(@Part MultipartBody.Part photo);
}
