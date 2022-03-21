package nl.rijksoverheid.ctr.persistence.database.migration

import nl.rijksoverheid.ctr.persistence.PersistenceManager

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
