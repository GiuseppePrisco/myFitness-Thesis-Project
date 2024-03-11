package com.example.greetingcard.network

import com.example.greetingcard.model.ExerciseData
import com.example.greetingcard.model.RepsData
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/*
class MyInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request()
            .newBuilder()
            //builder.header("X-RapidAPI-Key","d3267b2896mshd772518450ec91fp17c425jsn1f0dabcd852a")
            //builder.header("X-RapidAPI-Host","exercisedb.p.rapidapi.com")
            .addHeader("X-RapidAPI-Key","d3267b2896mshd772518450ec91fp17c425jsn1f0dabcd852a")
            .addHeader("X-RapidAPI-Host","exercisedb.p.rapidapi.com")
            .build()
        return chain.proceed(builder)
    }
}

private val client = OkHttpClient.Builder().apply {
    addInterceptor(MyInterceptor())
}.build()
 */

private const val EXTERNAL_BASE_URL =
    "https://exercisedb.p.rapidapi.com"
    //"https://android-kotlin-fun-mars-server.appspot.com"

private const val MY_BASE_URL =
    "https://giuseppeprisco.pythonanywhere.com"



private val client = OkHttpClient.Builder().apply {
    addInterceptor(
        Interceptor { chain ->
            val builder = chain.request().newBuilder()
                .addHeader("X-RapidAPI-Key","d3267b2896mshd772518450ec91fp17c425jsn1f0dabcd852a")
                //.addHeader("X-RapidAPI-Key","6ebfec3fe1mshbf6cfda7e264efbp122304jsn623f88fba3a4")
                .addHeader("X-RapidAPI-Host","exercisedb.p.rapidapi.com")
            return@Interceptor chain.proceed(builder.build())
        }
    )
}.build()


private val externalCloudService = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(EXTERNAL_BASE_URL)
    .client(client)
    .build()


private val myCloudService = Retrofit.Builder()
    //.addConverterFactory(GsonConverterFactory.create())
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(MY_BASE_URL)
    .build()

/*
val headerMap = mutableMapOf<String, String>(Pair("X-RapidAPI-Key","d3267b2896mshd772518450ec91fp17c425jsn1f0dabcd852a"),
    Pair("X-RapidAPI-Host","exercisedb.p.rapidapi.com")
    )
 */

interface ExerciseApiService {
    /*
    @Headers(
        "X-RapidAPI-Key: d3267b2896mshd772518450ec91fp17c425jsn1f0dabcd852a",
        "X-RapidAPI-Host: exercisedb.p.rapidapi.com"
    )
    */
    @GET("exercises/{path1}/{path2}?limit=100")
    suspend fun getData(@Path("path1") path1: String, @Path("path2") path2: String): List<ExerciseData>
    //suspend fun getData(@HeaderMap headerMap: Map<String, String>, @Path("path") path: String): List<ExerciseData>
    // suspend fun getPhotos(@Path("path") path: String, @QueryMap filters: Map<String, String>): List<ExerciseData>
}


interface RepsApiService {
    @GET("reps")
    suspend fun getData(): List<RepsData>

    @POST("reps")
    suspend fun postData(@Body repsList: RepsData): String
    //suspend fun postData(@Body repsList: List<RepsData>)

}


object ExerciseApi {
    val externalRetrofitService: ExerciseApiService by lazy {
        externalCloudService.create(ExerciseApiService::class.java)
    }
}

object RepsApi {
    val myRetrofitService: RepsApiService by lazy {
        myCloudService.create(RepsApiService::class.java)
    }
}


// ############################################################# //
// code to upload the images to the external server

// TODO change to the appropriate link
private const val MY_SERVER_URL =
    "https://myfitness.azurewebsites.net"

private val myClient = OkHttpClient.Builder().apply {
    addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
}.build()

private val myServerService = Retrofit.Builder()
    .baseUrl(MY_SERVER_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .client(myClient)
    .build()

interface PoseApiService {
    @Multipart
    @POST("/upload")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<ResponseBody>
}

object PoseApi {
    val retrofitService: PoseApiService by lazy {
        myServerService.create(PoseApiService::class.java)
    }
}



/*
suspend fun getExerciseData(parameters: String): String {

    val client = OkHttpClient()

    /*
    val request = Request.Builder()
        .url("https://exercisedb.p.rapidapi.com/"+parameters)
        .get()
        .addHeader("X-RapidAPI-Key", "6ebfec3fe1mshbf6cfda7e264efbp122304jsn623f88fba3a4")
        .addHeader("X-RapidAPI-Host", "exercisedb.p.rapidapi.com")
        .build()
    */

    val request = Request.Builder()
        .url("https://android-kotlin-fun-mars-server.appspot.com")
        .get()
        .build()

    val response = client.newCall(request).execute()

    return response.body?.string() ?: "pippo"
}


class ExerciseApi(parameters: String) {

    companion object {
        suspend fun getExerciseData(parameters: String): String {

            val client = OkHttpClient()

            /*
            val request = Request.Builder()
                .url("https://exercisedb.p.rapidapi.com/"+parameters)
                .get()
                .addHeader("X-RapidAPI-Key", "6ebfec3fe1mshbf6cfda7e264efbp122304jsn623f88fba3a4")
                .addHeader("X-RapidAPI-Host", "exercisedb.p.rapidapi.com")
                .build()
            */

            val request = Request.Builder()
                .url("https://android-kotlin-fun-mars-server.appspot.com")
                .get()
                .build()

            val response = client.newCall(request).execute()

            return response.body?.string() ?: "pippo"
        }
    }

}

 */