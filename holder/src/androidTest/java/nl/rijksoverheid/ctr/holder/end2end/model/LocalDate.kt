package nl.rijksoverheid.ctr.holder.end2end.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun LocalDate.short(): String {
    return this.format(DateTimeFormatter.ISO_LOCAL_DATE)
}

fun LocalDate.written(): String {
    return this.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
}

fun LocalDate.recently(): String {
    return this.format(DateTimeFormatter.ofPattern("EEEE d MMMM"))
}

fun LocalDate.dutch(): String {
    return this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
}

fun LocalDate.offsetDays(offset: Long): LocalDate {
    return this.plusDays(offset)
}
