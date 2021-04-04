package tech.igrant.jizhang.framework

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import tech.igrant.jizhang.login.TokenManager


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

        fun init(appDataSource: TokenManager.AppDataSource) {
            retrofit = Retrofit.Builder()
                    .baseUrl("http://jizhang.app")
                    .addConverterFactory(GSONExt.gsonConverterFactory())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(client(appDataSource.token, appDataSource.email))
                    .build()
        }
    }

}