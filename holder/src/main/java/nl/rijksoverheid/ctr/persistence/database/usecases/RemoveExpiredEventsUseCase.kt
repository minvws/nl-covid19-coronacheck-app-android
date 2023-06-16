package nl.rijksoverheid.ctr.persistence.database.usecases

import java.time.Clock
import java.time.OffsetDateTime
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity

interface RemoveExpiredEventsUseCase {
    suspend fun execute(events: List<EventGroupEntity>)
}

class RemoveExpiredEventsUseCaseImpl(
    private val clock: Clock,
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val holderDatabase: HolderDatabase
) : RemoveExpiredEventsUseCase {

    override suspend fun execute(events: List<EventGroupEntity>) {
        if (featureFlagUseCase.isInArchiveMode()) {
            return
        }

        events.forEach {
            if (it.expiryDate != null && it.expiryDate <= OffsetDateTime.now(clock)) {
                holderDatabase.eventGroupDao().delete(it)
            }
        }
    }
}
