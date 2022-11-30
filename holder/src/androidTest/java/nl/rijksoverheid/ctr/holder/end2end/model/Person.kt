package nl.rijksoverheid.ctr.holder.end2end.model

import java.time.LocalDate

data class Person(
    val bsn: String = "",
    val name: String = "van Geer, Corrie",
    val birthDate: LocalDate = LocalDate.of(1960, 1, 1)
)
