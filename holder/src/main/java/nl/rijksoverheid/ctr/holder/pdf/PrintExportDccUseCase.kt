package nl.rijksoverheid.ctr.holder.pdf

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.json.JSONObject

interface PrintExportDccUseCase {
    suspend fun export(): String
}

class PrintExportDccUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val moshi: Moshi
): PrintExportDccUseCase {
    override suspend fun export(): String {
        val credentials = holderDatabase.greenCardDao().getAll()
            .filter { it.greenCardEntity.type == GreenCardType.Eu && it.credentialEntities.isNotEmpty() }
            .map { it.credentialEntities.last() }
        println("Found ${credentials.size} credentials")
        val printAttributes = PrintAttributes(
            european = credentials.map {
                EUPrintAttributes(
                    dcc = mobileCoreWrapper.readEuropeanCredential(it.data),
                    expirationTime = it.expirationTime,
                    qr = String(it.data)
                )
            }
        )
        println("First qr: ${printAttributes.european.first().qr}")

        println("Return: ${moshi.adapter(PrintAttributes::class.java).toJson(printAttributes)}")

        return moshi.adapter(PrintAttributes::class.java).toJson(printAttributes)
    }
}
