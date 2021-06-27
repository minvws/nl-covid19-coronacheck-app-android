package nl.rijksoverheid.ctr.holder.ui.create_qr.models

abstract class RemoteProtocol(
    open val providerIdentifier: String,
    open val protocolVersion: String,
    open val status: Status
) {

    enum class Status(val apiStatus: String) {
        UNKNOWN(""),
        PENDING("pending"),
        INVALID_TOKEN("invalid_token"),
        VERIFICATION_REQUIRED("verification_required"),
        COMPLETE("complete");

        companion object {
            fun fromValue(value: String?): Status {
                return values().firstOrNull { it.apiStatus == value } ?: UNKNOWN
            }
        }
    }

    abstract fun hasEvents(): Boolean
}