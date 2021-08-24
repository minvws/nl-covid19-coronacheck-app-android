package nl.rijksoverheid.ctr.shared.error

import java.io.Serializable

data class ErrorResultFragmentData(
    val title: String,
    val description: String,
    val buttonTitle: String,
    val urlData: UrlData? = null,
    val buttonCallback: () -> Unit): Serializable {

    data class UrlData(
        val urlButtonTitle: String,
        val urlButtonUrl: String
    )
}