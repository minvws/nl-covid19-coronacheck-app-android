/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.utils

import android.app.Instrumentation
import androidx.preference.PreferenceManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.api.json.DisclosurePolicyJsonAdapter
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.models.ConfigResponse
import nl.rijksoverheid.ctr.appconfig.repositories.ConfigRepository
import nl.rijksoverheid.ctr.holder.end2end.res.TestKeys
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import okhttp3.Headers
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

fun overrideModules(modules: List<Module>, instrumentation: Instrumentation) {
    loadKoinModules(listOf(module {
        factory {
            PreferenceManager.getDefaultSharedPreferences(instrumentation.targetContext)
        }
    }, module {
        factory<ConfigRepository> {
            object : ConfigRepository {
                override suspend fun getConfig(): ConfigResponse {
                    val moshi = Moshi.Builder()
                        .add(DisclosurePolicyJsonAdapter()).build()
                    val jsonAdapter: JsonAdapter<HolderConfig> =
                        moshi.adapter(HolderConfig::class.java)
                    val holderConfigString = jsonAdapter.toJson(
                        HolderConfig.default(
                            disclosurePolicy = DisclosurePolicy.ZeroG,
                            configTTL = 1_000_000_000,
                            backendTLSCertificates = listOf(
                                TestKeys.backendTLSCertificate1,
                                TestKeys.backendTLSCertificate2
                            ),
                            hpkCodes = listOf(
                                AppConfig.HpkCode(
                                    code = "2924528",
                                    name = "Comirnaty (Pfizer)",
                                    displayName = "PFIZER INJVLST 0,3ML",
                                    vp = "1119349007",
                                    mp = "EU/1/20/1528",
                                    ma = "ORG-100030215"
                                ), AppConfig.HpkCode(
                                    code = "3017842",
                                    name = "Comirnaty (Pfizer)",
                                    displayName = "PFIZER ORIG/OMICRON BA.1 INJ 0,3ML",
                                    vp = "1119349007",
                                    mp = "EU/1/20/1528",
                                    ma = "ORG-100030215"
                                ), AppConfig.HpkCode(
                                    code = "3017885",
                                    name = "Comirnaty (Pfizer)",
                                    displayName = "PFIZER ORIG/OMICRON B4-5 INJ 0,3ML",
                                    vp = "1119349007",
                                    mp = "EU/1/20/1528",
                                    ma = "ORG-100030215"
                                ), AppConfig.HpkCode(
                                    code = "2924536",
                                    name = "Spikevax (Moderna)",
                                    displayName = "MODERNA INJVLST 0,5ML",
                                    vp = "1119349007",
                                    mp = "EU/1/20/1507",
                                    ma = "ORG-100031184"
                                ), AppConfig.HpkCode(
                                    code = "2934701",
                                    name = "Jcovden (Janssen)",
                                    displayName = "JANSSEN INJVLST 0,5ML",
                                    vp = "J07BX03",
                                    mp = "EU/1/20/1525",
                                    ma = "ORG-100001417"
                                )
                            ),
                            euBrands = listOf(
                                AppConfig.Code(
                                    code = "EU/1/20/1507",
                                    name = "Spikevax (Moderna)"
                                ), AppConfig.Code(
                                    code = "EU/1/20/1528",
                                    name = "Comirnaty (Pfizer)"
                                ), AppConfig.Code(
                                    code = "EU/1/20/1525",
                                    name = "Jcovden (Janssen)"
                                )
                            ),
                            euVaccinations = listOf(
                                AppConfig.Code(
                                    code = "1119349007",
                                    name = "SARS-CoV-2 mRNA vaccine"
                                ),
                                AppConfig.Code(
                                    code = "J07BX03",
                                    name = "covid-19 vaccines"
                                )
                            ),
                            euManufacturers = listOf(
                                AppConfig.Code(
                                    code = "ORG-100030215",
                                    name = "Biontech Manufacturing GmbH"
                                ),
                                AppConfig.Code(
                                    code = "ORG-100001417",
                                    name = "Janssen-Cilag International"
                                ),
                                AppConfig.Code(
                                    code = "ORG-100031184",
                                    name = "Moderna Biotech Spain S.L."
                                )
                            ),
                            euTestTypes = listOf(
                                AppConfig.Code(
                                    code = "LP6464-4",
                                    name = "PCR (NAAT)"
                                ),
                                AppConfig.Code(
                                    code = "LP217198-3",
                                    name = "Sneltest (RAT)"
                                )
                            ),
                            euTestNames = listOf(
                                AppConfig.Code(
                                    code = "RAT Manufacturer",
                                    name = "RAT Name"
                                )
                            )
                        )
                    )
                    return ConfigResponse(
                        body = holderConfigString,
                        headers = Headers.headersOf()
                    )
                }

                override suspend fun getPublicKeys(): String {
                    return TestKeys.publicKeys
                }
            }
        }
    }) + modules)
}
