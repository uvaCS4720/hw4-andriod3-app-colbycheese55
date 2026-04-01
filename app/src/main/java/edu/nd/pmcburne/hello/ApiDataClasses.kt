package edu.nd.pmcburne.hello

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.ExperimentalSerializationApi


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ApiResponse(
    val lms: List<ApiLandmarkWrapper>
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ApiLandmarkWrapper(
    val id: Int,
    val name: String,
    val description: String,
    val tag_list: List<String>,
    val visual_center: ApiVsWrapper,
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ApiVsWrapper(
    val longitude: Double,
    val latitude: Double,
)


fun ApiResponse.toLandmarks(): List<Landmark> {
    return lms.map { l ->
        Landmark(
            id = l.id,
            name = l.name,
            description = l.description,
            tag_list = l.tag_list,
            latitude = l.visual_center.latitude,
            longitude = l.visual_center.longitude
        )
    }
}