package edu.nd.pmcburne.hello

data class Landmark(
    val id: Int,
    val name: String,
    val description: String,
    val tag_list: List<String>,
    val latitude: Double,
    val longitude: Double,
)