package nl.rijksoverheid.ctr.shared.ext

fun <K, V> Map<K, V?>.filterNotNullValues() = filterValues { it != null } as Map<K, V>
