package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import android.os.Parcelable
import java.time.OffsetDateTime

abstract class RemoteEvent(open val type: String?): Parcelable {
    abstract fun getDate(): OffsetDateTime?
}