package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteCouplingResponse(
    val status: RemoteCouplingStatus
)