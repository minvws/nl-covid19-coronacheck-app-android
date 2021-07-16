package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import timber.log.Timber
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface CredentialUtil {
    fun getActiveCredential(entities: List<CredentialEntity>): CredentialEntity?
    fun isExpiring(credentialRenewalDays: Long, credential: CredentialEntity): Boolean
}

class CredentialUtilImpl(private val clock: Clock): CredentialUtil {
    override fun getActiveCredential(entities: List<CredentialEntity>): CredentialEntity? {

        // All credentials that fall into the expiration window
        val credentialsInWindow = entities.filter {
            it.validFrom.isBefore(
                OffsetDateTime.now(clock)
            ) && it.expirationTime.isAfter(
                OffsetDateTime.now(
                    clock
                )
            )
        }

        // Return the credential with the longest expiration time if it exists
        return credentialsInWindow.maxByOrNull {
            it.expirationTime.toEpochSecond() - OffsetDateTime.now(clock)
                .toEpochSecond()
        }
    }

    override fun isExpiring(credentialRenewalDays: Long, credential: CredentialEntity): Boolean {
        val now = OffsetDateTime.now(clock)
        return credential.expirationTime.minusDays(credentialRenewalDays).isBefore(now)
    }
}