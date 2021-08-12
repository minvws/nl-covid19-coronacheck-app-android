package nl.rijksoverheid.ctr.holder.persistence.database.migration

import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface TestResultsMigrationManager {
    suspend fun removeOldCredential()
}

class TestResultsMigrationManagerImpl(
    private val persistenceManager: PersistenceManager,
) : TestResultsMigrationManager {
    override suspend fun removeOldCredential() {
        val existingCredentials = persistenceManager.getCredentials()
        if (existingCredentials != null) {
            persistenceManager.deleteCredentials()
        }
    }
}
