package nl.rijksoverheid.ctr.holder.get_events.models

import android.os.Parcelable
import java.time.OffsetDateTime

abstract class RemoteEvent(open val unique: String?, open val type: String?) : Parcelable {
    abstract fun getDate(): OffsetDateTime?
}
