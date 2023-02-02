package nl.rijksoverheid.ctr.holder.no_digid

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import java.time.DayOfWeek
import java.time.format.TextStyle
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.DialogFragmentData
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
import nl.rijksoverheid.ctr.holder.ui.create_qr.setEnabled
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.shared.ext.locale
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.utils.Accessibility.makeIndeterminateAccessible
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class PapFragment : DigiDFragment(R.layout.fragment_no_digid) {

    private var _binding: FragmentNoDigidBinding? = null
    private val binding: FragmentNoDigidBinding get() = _binding!!
    private val args: PapFragmentArgs by navArgs()
    private val infoFragmentUtil: InfoFragmentUtil by inject()
    private val holderFeatureFlagUseCase: HolderFeatureFlagUseCase by inject()
    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()

    companion object {
        fun openDaysString(context: Context, contactInformation: AppConfig.ContactInformation): String {
            val startDay = contactInformation.startDay
            val endDay = contactInformation.endDay

            if (startDay == 1 && endDay == 7) {
                return context.getString(R.string.holder_contactCoronaCheckHelpdesk_message_every_day)
            }

            val startDayOfWeek = DayOfWeek.of(contactInformation.startDay).getDisplayName(TextStyle.FULL, context.locale())
            val endDayOfWeek = DayOfWeek.of(contactInformation.endDay).getDisplayName(TextStyle.FULL, context.locale())

            return context.getString(R.string.holder_contactCoronaCheckHelpdesk_message_until, startDayOfWeek, endDayOfWeek)
        }
    }

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

        _binding = FragmentNoDigidBinding.bind(view)

        if (args.originType is RemoteOriginType.Vaccination) {
            binding.title.text = getString(R.string.holder_chooseEventLocation_title)
            binding.description.visibility = View.GONE
            binding.firstButton.bind(
                title = R.string.holder_chooseEventLocation_buttonTitle_GGD,
                subtitle = getString(R.string.holder_chooseEventLocation_buttonSubTitle_GGD)
            ) {
                loginWithDigiD()
            }

            binding.secondButton.bind(
                title = R.string.holder_chooseEventLocation_buttonTitle_other,
                subtitle = getString(R.string.holder_chooseEventLocation_buttonSubTitle_other)
            ) {
                infoFragmentUtil.presentFullScreen(
                    currentFragment = this@PapFragment,
                    toolbarTitle = getString(R.string.choose_provider_toolbar),
                    data = InfoFragmentData.TitleDescriptionWithButton(
                        title = getString(R.string.holder_contactProviderHelpdesk_vaccinationFlow_title),
                        descriptionData = DescriptionData(R.string.holder_contactProviderHelpdesk_message_ggdPortalEnabled),
                        primaryButtonData = ButtonData.NavigationButton(
                            text = getString(R.string.general_toMyOverview),
                            navigationActionId = R.id.action_my_overview
                        )
                    )
                )
            }
        } else {
            binding.title.text = getString(R.string.holder_checkForBSN_title)
            binding.description.text = getString(R.string.holder_checkForBSN_message)
            binding.description.visibility = View.VISIBLE
            binding.firstButton.bind(
                title = R.string.holder_checkForBSN_buttonTitle_doesHaveBSN,
                subtitle = getString(R.string.holder_checkForBSN_buttonSubTitle_doesHaveBSN)
            ) {
                val contactInformation = cachedAppConfigUseCase.getCachedAppConfig().contactInfo
                infoFragmentUtil.presentFullScreen(
                    currentFragment = this@PapFragment,
                    toolbarTitle = getString(R.string.choose_provider_toolbar),
                    data = InfoFragmentData.TitleDescriptionWithButton(
                        title = getString(R.string.holder_contactCoronaCheckHelpdesk_title),
                        descriptionData = DescriptionData(
                            htmlTextString = getString(
                                R.string.holder_contactCoronaCheckHelpdesk_message,
                                openDaysString(requireContext(), contactInformation),
                                contactInformation.startHour,
                                contactInformation.endHour,
                                contactInformation.phoneNumber,
                                contactInformation.phoneNumber,
                                contactInformation.phoneNumberAbroad,
                                contactInformation.phoneNumberAbroad
                            ),
                            htmlLinksEnabled = true
                        ),
                        primaryButtonData = ButtonData.NavigationButton(
                            text = getString(R.string.general_toMyOverview),
                            navigationActionId = R.id.action_my_overview
                        )
                    )
                )
            }

            binding.secondButton.bind(
                title = R.string.holder_checkForBSN_buttonTitle_doesNotHaveBSN,
                subtitle = getString(R.string.holder_checkForBSN_buttonSubTitle_doesNotHaveBSN_testFlow)
            ) {
                if (holderFeatureFlagUseCase.getPapEnabled()) {
                    /** disable accessibility, otherwise it is announced when loading is finished
                     * reenable with [dialogPresented] if a dialog is presented cause then the user will again interact with it
                     */
                    binding.secondButton.root.importantForAccessibility =
                        View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    loginWithDigiD()
                } else {
                    infoFragmentUtil.presentFullScreen(
                        currentFragment = this@PapFragment,
                        toolbarTitle = getString(R.string.choose_provider_toolbar),
                        data = InfoFragmentData.TitleDescriptionWithButton(
                            title = getString(R.string.holder_contactProviderHelpdesk_testFlow_title),
                            descriptionData = DescriptionData(R.string.holder_contactProviderHelpdesk_testFlow_message),
                            primaryButtonData = ButtonData.NavigationButton(
                                text = getString(R.string.general_toMyOverview),
                                navigationActionId = R.id.action_my_overview
                            )
                        )
                    )
                }
            }
        }

        digidViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })
    }

    override fun getLoginType(): LoginType {
        return LoginType.Pap
    }

    private fun setEnabled(enabled: Boolean) {
        binding.title.isEnabled = enabled
        binding.description.isEnabled = enabled
    }

    override fun onDigidLoading(loading: Boolean) {
        binding.firstButton.setEnabled(!loading)
        binding.secondButton.setEnabled(!loading)
        setEnabled(!loading)
    }

    override fun onGetEventsLoading(loading: Boolean) {
        binding.loadingOverlay.progressBar.makeIndeterminateAccessible(
            context = requireContext(),
            isLoading = loading,
            message = R.string.holder_fetchevents_loading
        )
        binding.loadingOverlay.root.isVisible = loading
    }

    override fun getOriginTypes(): List<RemoteOriginType> {
        return listOf(args.originType)
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

    override fun yourEventsFragmentType(
        remoteProtocols: Map<RemoteProtocol, ByteArray>,
        eventProviders: List<EventProvider>
    ): YourEventsFragmentType {
        return YourEventsFragmentType.RemoteProtocol3Type(
            remoteEvents = remoteProtocols,
            eventProviders = eventProviders
        )
    }

    override fun dialogPresented() {
        binding.secondButton.root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    override fun openDialog(data: DialogFragmentData) {
        navigateSafety(PapFragmentDirections.actionDialog(data))
    }
}
