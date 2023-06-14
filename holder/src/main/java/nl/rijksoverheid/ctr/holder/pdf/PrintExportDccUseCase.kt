package nl.rijksoverheid.ctr.holder.pdf

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

interface PrintExportDccUseCase {
    suspend fun export(): String
}

class PrintExportDccUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val moshi: Moshi
) : PrintExportDccUseCase {
    override suspend fun export(): String {
        val credentials = holderDatabase.greenCardDao().getAll()
            .filter { it.greenCardEntity.type == GreenCardType.Eu && it.credentialEntities.isNotEmpty() }
            .map { it.credentialEntities.last() }

        val printAttributes = PrintAttributes(
            european = credentials.map {
                EUPrintAttributes(
                    dcc = mobileCoreWrapper.readEuropeanCredential(it.data).getJSONObject("dcc"),
                    expirationTime = it.expirationTime,
                    qr = String(it.data)
                )
            }
        )

        return moshi.adapter(PrintAttributes::class.java).toJson(printAttributes)
    }
}
