package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
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
        val cachedAppConfig = cachedAppConfigUseCase.getCachedAppConfig()
        val events = holderDatabase.eventGroupDao().getAll()
        events.forEach {
            val expireDate = when (it.type) {
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

            if (it.maxIssuedAt.plusHours(expireDate.toLong()) <= OffsetDateTime.now(clock)) {
                holderDatabase.eventGroupDao().delete(it)
            }
        }
    }
}