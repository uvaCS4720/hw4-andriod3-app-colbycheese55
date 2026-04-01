package edu.nd.pmcburne.hello

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter


@Entity
data class Landmark(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String,
    val tag_list: List<String>,
    val latitude: Double,
    val longitude: Double,
)

class Converters {
    @TypeConverter
    fun fromList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}