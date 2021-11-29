package nl.rijksoverheid.ctr.design.fragments.info

import android.content.Intent
import android.os.Parcelable
import android.view.View.NO_ID
import androidx.annotation.IdRes
import kotlinx.parcelize.Parcelize

@Parcelize
class DescriptionData(
    val htmlText: Int? = null,
    val htmlTextString: String? = null,
    val htmlLinksEnabled: Boolean = false,
    val customLinkIntent: Intent? = null,
    val htmlTextColor: Int = NO_ID,
) : Parcelable

sealed class ButtonData(open val text: String) : Parcelable {
    @Parcelize
    data class LinkButton(override val text: String, val link: String) : ButtonData(text),
        Parcelable

    @Parcelize
    data class NavigationButton(override val text: String, @IdRes val navigationActionId: Int) :
        ButtonData(text), Parcelable
}

sealed class InfoFragmentData(open val title: String, open val descriptionData: DescriptionData) :
    Parcelable {
    @Parcelize
    class TitleDescription(
        override val title: String,
        override val descriptionData: DescriptionData
    ) : InfoFragmentData(title, descriptionData)

    @Parcelize
    class TitleDescriptionWithButton(
        override val title: String,
        override val descriptionData: DescriptionData,
        val buttonData: ButtonData
    ) : InfoFragmentData(title, descriptionData)

    @Parcelize
    class TitleDescriptionWithFooter(
        override val title: String,
        override val descriptionData: DescriptionData,
        val footerText: String
    ) : InfoFragmentData(title, descriptionData)
}