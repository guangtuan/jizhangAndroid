package tech.igrant.jizhang.framework

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GSONExt {

    class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {

        override fun write(out: JsonWriter, value: LocalDate) {
            out.value(DateTimeFormatter.ISO_LOCAL_DATE.format(value))
        }

        override fun read(input: JsonReader): LocalDate = LocalDate.parse(input.nextString())
    }

    class LocalDateTimeTypeAdapter : TypeAdapter<LocalDateTime>() {

        override fun write(out: JsonWriter, value: LocalDateTime) {
            out.value(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value))
        }

        override fun read(input: JsonReader): LocalDateTime = LocalDateTime.parse(input.nextString())
    }

    companion object {

        private val gsonBuilder = GsonBuilder()
                .setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
                .registerTypeAdapter(
                        LocalDateTime::class.java,
                        LocalDateTimeTypeAdapter().nullSafe()
                )
                .registerTypeAdapter(
                        LocalDate::class.java,
                        LocalDateTypeAdapter().nullSafe()
                )

        fun gsonConverterFactory(): GsonConverterFactory {
            return GsonConverterFactory.create(gsonBuilder.create())
        }

        fun gson(): Gson {
            return gsonBuilder.create()
        }

    }

}