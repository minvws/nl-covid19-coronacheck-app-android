package nl.rijksoverheid.ctr.holder.no_digid

import android.content.Context
import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.could_not_create_qr.CouldNotCreateQrFragmentArgs
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.shared.models.Flow

interface NoDigidScreenDataUtil {
    fun requestDigidButton(flow: Flow): NavigationButtonData
    fun continueWithoutDigidButton(flow: Flow): NavigationButtonData
}

class NoDigidScreenDataUtilImpl(
    private val context: Context,
): NoDigidScreenDataUtil {

    private fun getString(@StringRes stringId: Int) = context.getString(stringId)

    private fun getStringArgs(@StringRes stringId: Int, args: Array<String>) =
        context.getString(stringId, *args)

    private val doesHaveBSNButton = NavigationButtonData(
        title = R.string.holder_checkForBSN_buttonTitle_doesHaveBSN,
        subtitle = getString(R.string.holder_checkForBSN_buttonSubTitle_doesHaveBSN),
        buttonClickDirection = ButtonClickDirection(
            actionId = R.id.action_could_not_create_qr,
            arguments = CouldNotCreateQrFragmentArgs(
                toolbarTitle = getString(R.string.choose_provider_toolbar),
                title = getString(R.string.holder_contactCoronaCheckHelpdesk_title),
                description = getString(R.string.holder_contactCoronaCheckHelpdesk_message),
                buttonTitle = getString(R.string.general_toMyOverview)
            ).toBundle()
        )
    )

    private fun doesNotHaveBSNButton(flow: Flow) = NavigationButtonData(
        title = R.string.holder_checkForBSN_buttonTitle_doesNotHaveBSN,
        subtitle = getString(R.string.holder_checkForBSN_buttonSubTitle_doesNotHaveBSN),
        buttonClickDirection = ButtonClickDirection(
            actionId = R.id.action_could_not_create_qr,
            arguments = CouldNotCreateQrFragmentArgs(
                toolbarTitle = getString(R.string.choose_provider_toolbar),
                title = getStringArgs(
                    R.string.holder_contactProviderHelpdesk_title, arrayOf(
                        getString(
                            if (flow == HolderFlow.Vaccination) {
                                R.string.holder_contactProviderHelpdesk_vaccinationLocation
                            } else {
                                R.string.holder_contactProviderHelpdesk_testLocation
                            }
                        )
                    )
                ),
                description = getString(R.string.holder_contactProviderHelpdesk_message),
                buttonTitle = getString(R.string.general_toMyOverview)
            ).toBundle()
        )
    )

    override fun requestDigidButton(flow: Flow) = NavigationButtonData(
        title = R.string.holder_noDigiD_buttonTitle_requestDigiD,
        icon = R.drawable.ic_digid_logo,
        externalUrl = getString(R.string.holder_noDigiD_url),
    )

    override fun continueWithoutDigidButton(flow: Flow) = NavigationButtonData(
        title = R.string.holder_noDigiD_buttonTitle_continueWithoutDigiD,
        subtitle = getString(R.string.holder_noDigiD_buttonSubTitle_continueWithoutDigiD),
        buttonClickDirection = ButtonClickDirection(
            actionId = R.id.action_no_digid,
            arguments = NoDigidFragmentArgs(
                NoDigidFragmentData(
                    title = getString(R.string.holder_checkForBSN_title),
                    description = getString(R.string.holder_checkForBSN_message),
                    firstNavigationButtonData = doesHaveBSNButton,
                    secondNavigationButtonData = doesNotHaveBSNButton(flow)
                )
            ).toBundle()
        )
    )
}
