package com.example.todoapp.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

val TOKEN = "8ed304e2e839cd3b2e0052328e390383b557e2cc"
private const val BASE_URL = "https://beta.todoist.com/API/v8/"

interface TodoApiService {
    @GET("tasks")
    fun getTasks(): Deferred<List<Task>>

    @POST("tasks")
    @Headers("Content-Type: application/json")
    fun createTasks(@Body task: Task): Deferred<Task?>

    @DELETE("tasks/{id}")
    fun deleteTasks(@Path("id") id: String):Deferred<Response<ResponseBody>>

    @POST("tasks/{id}/close")
    fun closeTasks(@Path("id") id: String): Deferred<Response<ResponseBody>>

    @POST("tasks/{id}/reopen")
    fun reopenTask(@Path("id") id:String): Deferred<Response<ResponseBody>>
}


object TodoApiCall {

    val okHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $TOKEN")
                    .build()
                chain.proceed(newRequest)
            }.build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(BASE_URL)
        .build()

    val retrofitService : TodoApiService by lazy {
        retrofit.create(TodoApiService::class.java)
    }

}