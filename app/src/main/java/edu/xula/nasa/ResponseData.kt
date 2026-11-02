package edu.xula.nasa

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseData(
    @SerialName("date") val date: String,
    @SerialName("title") val title: String,
    @SerialName("credit") val credit: String,
    @SerialName("alt") val alt: String,
    @SerialName("url") val url: String,
    @SerialName("hdurl") val hdurl: String
)
