package com.example.android.skladovypomocnik;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Api {

    //    String BASE_URL = "http://skladovypomocnik.cz/getdbversion.php/";
    String BASE_URL = "http://skladovypomocnik.cz/";

    @GET("getdbversion.php")
    Call<DatabaseVersion> getDatabaseVersionInfo();


}
