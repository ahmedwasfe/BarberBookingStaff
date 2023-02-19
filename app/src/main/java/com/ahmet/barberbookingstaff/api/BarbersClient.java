package com.ahmet.barberbookingstaff.api;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class BarbersClient {

    private static Retrofit retrofit;

    static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(new Interceptor() {
                @NotNull
                @Override
                public Response intercept(@NotNull Chain chain) throws IOException {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("X-Requested-With", "XMLHttpRequest")
                            //.addHeader("Authorization", "Bearer " + )
                            .build();
                    return chain.proceed(newRequest);
                }
            }).build();

    public static Retrofit getClient(String baseUrl) {
        return retrofit == null ?
                retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build()
                : retrofit;
    }
}
