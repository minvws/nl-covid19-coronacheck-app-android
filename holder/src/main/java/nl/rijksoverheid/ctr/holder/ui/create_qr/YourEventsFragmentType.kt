package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

sealed class YourEventsFragmentType : Parcelable {
    @Parcelize
    data class TestResult2(val remoteTestResult: RemoteTestResult2, val rawResponse: ByteArray) :
        YourEventsFragmentType(), Parcelable {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TestResult2

            if (remoteTestResult != other.remoteTestResult) return false
            if (!rawResponse.contentEquals(other.rawResponse)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = remoteTestResult.hashCode()
            result = 31 * result + rawResponse.contentHashCode()
            return result
        }
    }

    sealed class RemoteProtocol3Type(open val remoteEvents: Map<RemoteProtocol3, ByteArray>):
        YourEventsFragmentType(), Parcelable {
        @Parcelize
        data class Vaccinations(override val remoteEvents: Map<RemoteProtocol3, ByteArray>) :
            RemoteProtocol3Type(remoteEvents), Parcelable

        @Parcelize
        data class NegativeTests(override val remoteEvents: Map<RemoteProtocol3, ByteArray>) :
            RemoteProtocol3Type(remoteEvents), Parcelable

        @Parcelize
        data class PositiveTestsAndRecoveries(override val remoteEvents: Map<RemoteProtocol3, ByteArray>) :
            RemoteProtocol3Type(remoteEvents), Parcelable
    }

}
