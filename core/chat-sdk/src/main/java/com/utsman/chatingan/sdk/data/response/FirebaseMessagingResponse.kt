package com.utsman.chatingan.sdk.data.response


import com.google.gson.annotations.SerializedName

internal data class FirebaseMessagingResponse(
    @SerializedName("canonical_ids")
    val canonicalIds: Int? = null,
    @SerializedName("failure")
    val failure: Int? = null,
    @SerializedName("multicast_id")
    val multicastId: Long? = null,
    @SerializedName("results")
    val results: List<Result?>? = null,
    @SerializedName("success")
    val success: Int? = null
) {
    data class Result(
        @SerializedName("message_id")
        val messageId: String? = null,
        @SerializedName("error")
        val error: String? = null
    )
}