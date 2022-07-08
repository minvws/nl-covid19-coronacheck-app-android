package nl.rijksoverheid.ctr.holder.no_digid

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentNoDigidBinding
import nl.rijksoverheid.ctr.holder.get_events.DigiDFragment
import nl.rijksoverheid.ctr.holder.get_events.GetEventsFragmentDirections
import nl.rijksoverheid.ctr.holder.get_events.models.EventProvider
import nl.rijksoverheid.ctr.holder.get_events.models.LoginType
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.ui.create_qr.bind
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.android.ext.android.inject


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class PapFragment : DigiDFragment(R.layout.fragment_no_digid) {

    private val args: PapFragmentArgs by navArgs()
    private val infoFragmentUtil: InfoFragmentUtil by inject()

    override fun onButtonClickWithRetryAction() {
        loginWithDigiD()
    }

    override fun getFlow(): Flow {
        return when (args.originType) {
            RemoteOriginType.Recovery -> HolderFlow.Recovery
            RemoteOriginType.Test -> HolderFlow.DigidTest
            RemoteOriginType.Vaccination -> HolderFlow.Vaccination
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNoDigidBinding.bind(view)

        binding.title.text = getString(
            R.string.holder_chooseEventLocation_title, getString(
                if (args.originType is RemoteOriginType.Vaccination) {
                    R.string.holder_contactProviderHelpdesk_vaccinated
                } else {
                    R.string.holder_contactProviderHelpdesk_tested
                }
            )
        )
        binding.description.visibility = View.GONE

        binding.firstButton.bind(
            title = R.string.holder_chooseEventLocation_buttonTitle_GGD,
            subtitle = getString(R.string.holder_chooseEventLocation_buttonSubTitle_GGD),
        ) {
            loginWithDigiD()
        }

        binding.secondButton.bind(
            title = R.string.holder_chooseEventLocation_buttonTitle_other,
            subtitle = getString(R.string.holder_chooseEventLocation_buttonSubTitle_other),
        ) {
            infoFragmentUtil.presentFullScreen(
                currentFragment = this@PapFragment,
                toolbarTitle = getString(R.string.choose_provider_toolbar),
                data = InfoFragmentData.TitleDescriptionWithButton(
                    title = getString(R.string.holder_contactProviderHelpdesk_title),
                    descriptionData = DescriptionData(R.string.holder_contactProviderHelpdesk_message),
                    primaryButtonData = ButtonData.NavigationButton(
                        text = getString(R.string.general_toMyOverview),
                        navigationActionId = R.id.action_my_overview
                    )
                )
            )
        }

        digidViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })
    }

    override fun getLoginType(): LoginType {
        return LoginType.Pap
    }

    override fun onDigidLoading(loading: Boolean) {
        // TODO Disable buttons
    }

    override fun onGetEventsLoading(loading: Boolean) {
        // TODO Disable buttons
    }

    override fun getOriginTypes(): List<RemoteOriginType> {
        return listOf(RemoteOriginType.Vaccination)
    }

    override fun onNavigateToYourEvents(
        remoteProtocols: Map<RemoteProtocol, ByteArray>,
        eventProviders: List<EventProvider>
    ) {
        navigateSafety(
            GetEventsFragmentDirections.actionYourEvents(
                type = YourEventsFragmentType.RemoteProtocol3Type(
                    remoteEvents = remoteProtocols,
                    eventProviders = eventProviders
                ),
                toolbarTitle = getCopyForOriginType().toolbarTitle,
                flow = getFlow()
            )
        )
    }
}
