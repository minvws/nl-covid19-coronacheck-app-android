package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import java.time.Clock
import java.time.OffsetDateTime

interface RemoveExpiredEventsUseCase {
    suspend fun execute()
}

class RemoveExpiredEventsUseCaseImpl(
    private val clock: Clock,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val holderDatabase: HolderDatabase
): RemoveExpiredEventsUseCase {

    override suspend fun execute() {
        val events = holderDatabase.eventGroupDao().getAll()
        events.forEach {
            val expireHours =
                if (it.providerIdentifier == RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC) {
                    // If this is a dcc scanned event, the event validity is equals to the maxIssuedAt
                    0
                } else {
                    // If it's not, we have a bit of control and can add validity to it from remote config
                    getValidityHoursForOriginType(
                        originType = it.type
                    )
                }

            if (it.maxIssuedAt.plusHours(expireHours.toLong()) <= OffsetDateTime.now(clock)) {
                holderDatabase.eventGroupDao().delete(it)
            }
        }
    }

    private fun getValidityHoursForOriginType(originType: OriginType): Int {
        val cachedAppConfig = cachedAppConfigUseCase.getCachedAppConfig()
        return when (originType) {
            is OriginType.Vaccination -> {
                cachedAppConfig.vaccinationEventValidity
            }
            is OriginType.Test -> {
                cachedAppConfig.testEventValidity
            }
            is OriginType.Recovery -> {
                cachedAppConfig.recoveryEventValidity
            }
        }
    }
}