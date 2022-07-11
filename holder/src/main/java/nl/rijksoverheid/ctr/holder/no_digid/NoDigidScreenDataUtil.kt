package nl.rijksoverheid.ctr.holder.no_digid

import android.content.Context
import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface NoDigidScreenDataUtil {
    fun requestDigidButton(): NoDigidNavigationButtonData
    fun continueWithoutDigidButton(originType: RemoteOriginType): NoDigidNavigationButtonData
}

class NoDigidScreenDataUtilImpl(
    private val context: Context,
) : NoDigidScreenDataUtil {

    private fun getString(@StringRes stringId: Int) = context.getString(stringId)

    private fun getStringArgs(@StringRes stringId: Int, args: Array<String>) =
        context.getString(stringId, *args)

    private val doesHaveBSNButton = NoDigidNavigationButtonData.Info(
        title = R.string.holder_checkForBSN_buttonTitle_doesHaveBSN,
        subtitle = getString(R.string.holder_checkForBSN_buttonSubTitle_doesHaveBSN),
        infoFragmentData = InfoFragmentData.TitleDescriptionWithButton(
            title = getString(R.string.holder_contactCoronaCheckHelpdesk_title),
            descriptionData = DescriptionData(R.string.holder_contactCoronaCheckHelpdesk_message),
            primaryButtonData = ButtonData.NavigationButton(
                text = getString(R.string.general_toMyOverview),
                navigationActionId = R.id.action_my_overview
            )
        )
    )

    private fun doesNotHaveBSNButton() = NoDigidNavigationButtonData.Ggd(
        title = R.string.holder_checkForBSN_buttonTitle_doesNotHaveBSN,
        subtitle = getString(R.string.holder_checkForBSN_buttonSubTitle_doesNotHaveBSN_testFlow)
    )

    override fun requestDigidButton() = NoDigidNavigationButtonData.Link(
        title = R.string.holder_noDigiD_buttonTitle_requestDigiD,
        icon = R.drawable.ic_digid_logo,
        externalUrl = getString(R.string.holder_noDigiD_url),
    )

    override fun continueWithoutDigidButton(originType: RemoteOriginType) = NoDigidNavigationButtonData.NoDigid(
        title = R.string.holder_noDigiD_buttonTitle_continueWithoutDigiD,
        subtitle = getString(R.string.holder_noDigiD_buttonSubTitle_continueWithoutDigiD),
        noDigidFragmentData = NoDigidFragmentData(
            title = getString(R.string.holder_checkForBSN_title),
            description = getString(R.string.holder_checkForBSN_message),
            firstNavigationButtonData = doesHaveBSNButton,
            secondNavigationButtonData = doesNotHaveBSNButton(),
            originType = originType
        )
    )
}
