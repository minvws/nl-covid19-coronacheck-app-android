package nl.rijksoverheid.ctr.holder.get_events.models

import android.os.Parcelable
import java.time.OffsetDateTime

abstract class RemoteEvent(open val unique: String?, open val type: String?) : Parcelable {

    companion object {
        const val TYPE_VACCINATION = "vaccination"
        const val TYPE_NEGATIVE_TEST = "negativetest"
        const val TYPE_POSITIVE_TEST = "positivetest"
        const val TYPE_RECOVERY = "recovery"
        const val TYPE_TEST = "test"

        fun getRemoteEventClassFromType(type: String): Class<out RemoteEvent> {
            return when (type) {
                "positivetest" -> RemoteEventPositiveTest::class.java
                "recovery" -> RemoteEventRecovery::class.java
                "negativetest" -> RemoteEventNegativeTest::class.java
                "test" -> RemoteEventNegativeTest::class.java
                "vaccination" -> RemoteEventVaccination::class.java
                else -> RemoteEvent::class.java
            }
        }
    }

    abstract fun getDate(): OffsetDateTime?
}
