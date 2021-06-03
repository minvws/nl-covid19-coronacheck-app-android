package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType

/**
 * Checks the database if any of these origins exists
 */
interface HasOriginUseCase {
    suspend fun hasOrigin(originType: OriginType): Boolean
}

class HasOriginUseCaseImpl(private val database: HolderDatabase): HasOriginUseCase {
    override suspend fun hasOrigin(originType: OriginType): Boolean {
        return withContext(Dispatchers.IO) {
            database.originDao().getAll().map { it.type }.contains(originType)
        }
    }
}