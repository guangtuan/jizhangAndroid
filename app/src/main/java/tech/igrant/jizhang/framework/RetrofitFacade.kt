package tech.igrant.jizhang.framework

import android.util.Log
import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import tech.igrant.jizhang.login.TokenManager
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class RetrofitFacade private constructor() {

    companion object {
        private lateinit var retrofit: Retrofit

        fun get(): Retrofit {
            return retrofit
        }

        private fun client(): OkHttpClient {
            return OkHttpClient.Builder()
                .apply {
                    this.addInterceptor(HttpLoggingInterceptor().apply {
                        this.level = HttpLoggingInterceptor.Level.BODY
                    })
                }
                .build()
        }

        private fun client(token: String, email: String): OkHttpClient {
            return OkHttpClient.Builder()
                .apply {
                    this.addInterceptor { chain ->
                        Log.i("token", token)
                        chain.request().headers().names()
                            .forEach { name -> Log.i("header key", name) }
                        val newRequest = chain.request().newBuilder()
                            .header("token", token)
                            .header("email", email)
                            .build()
                        chain.proceed(newRequest)
                    }
                    this.addInterceptor(HttpLoggingInterceptor().apply {
                        this.level = HttpLoggingInterceptor.Level.BODY
                    })
                }
                .build()
        }

        fun tmp(): Retrofit {
            return Retrofit.Builder()
                .baseUrl("http://jizhang.app")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(client())
                .build()
        }

        class LocalDateTimeTypeAdapter : TypeAdapter<LocalDateTime>() {

            companion object {
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            }

            override fun write(out: JsonWriter, value: LocalDateTime) {
                out.value(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value))
            }

            override fun read(input: JsonReader): LocalDateTime =
                LocalDateTime.parse(input.nextString())
        }

        fun init(appDataSource: TokenManager.AppDataSource) {
            val gsonConverterFactory = GsonConverterFactory.create(
                GsonBuilder()
                    .setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
                    .registerTypeAdapter(
                        LocalDateTime::class.java,
                        object : JsonDeserializer<LocalDateTime> {
                            override fun deserialize(
                                json: JsonElement,
                                type: Type,
                                jsonDeserializationContext: JsonDeserializationContext
                            ): LocalDateTime = LocalDateTime.parse(json.asString)
                        })
                    .registerTypeAdapter(
                        LocalDate::class.java,
                        object : TypeAdapter<LocalDate>() {
                            override fun read(input: JsonReader): LocalDate {
                                return LocalDate.parse(input.nextString())
                            }

                            override fun write(out: JsonWriter, value: LocalDate?) {
                                out.value(DateTimeFormatter.ISO_LOCAL_DATE.format(value))
                            }
                        }
                    )
                    .create()
            )
            retrofit = Retrofit.Builder()
                .baseUrl("http://jizhang.app")
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(client(appDataSource.token, appDataSource.email))
                .build()
        }
    }

}