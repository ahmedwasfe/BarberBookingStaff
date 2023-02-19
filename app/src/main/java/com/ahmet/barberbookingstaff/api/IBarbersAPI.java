package com.ahmet.barberbookingstaff.api;

import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.model.CheckBarber;
import com.ahmet.barberbookingstaff.model.CheckSalon;
import com.ahmet.barberbookingstaff.model.Salon;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface IBarbersAPI {

    /// SALON
    // check is salon exists
    @FormUrlEncoded
    @POST("salon/checkSalonExists.php")
    Call<CheckSalon> checkSalonExists(@Field("email") String email);

    // create new salon
    @FormUrlEncoded
    @POST("salon/createNewSalon.php")
    Call<Salon> createNewSalon(@Field("email") String email,
                               @Field("salonName") String salonName,
                               @Field("phone") String phone,
                               @Field("city") String city,
                               @Field("salonType") String salonType,
                               @Field("latitude") double latitude,
                               @Field("longitude") double longitude,
                               @Field("salonStatus") int salonStatus);

    // get Salon information
    @FormUrlEncoded
    @POST("salon/getSalonInfo.php")
    Call<Salon> getSalonInfo(@Field("email") String salonEmail);

    @GET("salon/getAllSalons.php")
    Call<List<Salon>> getAllSalons();

    @FormUrlEncoded
    @POST("salon/getSalonByNameAndEmail.php")
    Call<CheckSalon> getSalonByNameOrEmail(@Field("email") String email,
                                           @Field("salonName") String salonName);


    //// BARBER


    @FormUrlEncoded
    @POST("barber/checkBarberExists.php")
    Call<CheckBarber> checkBarberExists(@Field("phone") String phone);

    @FormUrlEncoded
    @POST("barber/checkBarberUsername.php")
    Call<CheckBarber> checkBarberUsername(@Field("username") String username);

    @FormUrlEncoded
    @POST("barber/createNewBarber.php")
    Call<Barber> createNewBarber(@Field("phone") String phone,
                                 @Field("name") String name,
                                 @Field("username") String username,
                                 @Field("password") String password,
                                 @Field("image") String image,
                                 @Field("gender") int gender,
                                 @Field("salon") String salonEmail,
                                 @Field("rate") long rate,
                                 @Field("available") int available);

    @FormUrlEncoded
    @POST("barber/loginBarber.php")
    Call<Barber> loginBarber(@Field("username") String username,
                             @Field("password") String password);

    @FormUrlEncoded
    @POST("barber/updatePassword.php")
    Call<Barber> updatePassword(@Field("phone") String phoneNumber,
                                @Field("password") String password);
}
