package nl.rijksoverheid.ctr.design.fragments.info

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class DescriptionData(
    val htmlText: Int? = null,
    val htmlTextString: String? = null,
    val htmlLinksEnabled: Boolean = false,
    val customLinkIntent: Intent? = null
) : Parcelable

sealed class ButtonData(open val text: String) : Parcelable {
    @Parcelize
    data class LinkButton(override val text: String, val link: String) : ButtonData(text),
        Parcelable

    @Parcelize
    data class NavigationButton(
        override val text: String,
        @IdRes val navigationActionId: Int,
        val navigationArguments: Bundle? = null
    ) :
        ButtonData(text), Parcelable
}

sealed class InfoFragmentData(open val title: String, open val descriptionData: DescriptionData) :
    Parcelable {
    @Parcelize
    data class TitleDescription(
        override val title: String,
        override val descriptionData: DescriptionData
    ) : InfoFragmentData(title, descriptionData)

    @Parcelize
    data class TitleDescriptionWithButton(
        override val title: String,
        override val descriptionData: DescriptionData,
        val secondaryButtonData: ButtonData? = null,
        val primaryButtonData: ButtonData? = null
    ) : InfoFragmentData(title, descriptionData)

    @Parcelize
    data class TitleDescriptionWithFooter(
        override val title: String,
        override val descriptionData: DescriptionData,
        val footerText: String
    ) : InfoFragmentData(title, descriptionData)
}
