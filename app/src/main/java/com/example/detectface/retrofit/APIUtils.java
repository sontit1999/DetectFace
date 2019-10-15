package com.example.detectface.retrofit;

public class APIUtils {
    public static final String baseurl = "http://127.0.0.1:5000/api/upload/";
    public static DataClient getData(){
        return RetrofitClient.getClient(baseurl).create(DataClient.class);
    }
}
