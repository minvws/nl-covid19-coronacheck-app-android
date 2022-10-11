package nl.rijksoverheid.ctr.shared.ext

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V?>.filterNotNullValues() = filterValues { it != null } as Map<K, V>
