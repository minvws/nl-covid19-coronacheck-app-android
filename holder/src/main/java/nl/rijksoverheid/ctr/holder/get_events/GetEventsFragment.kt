/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentGetEventsBinding
import nl.rijksoverheid.ctr.holder.get_events.models.EventProvider
import nl.rijksoverheid.ctr.holder.get_events.models.LoginType
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.no_digid.NoDigidFragmentData
import nl.rijksoverheid.ctr.holder.no_digid.NoDigidScreenDataUtil
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GetEventsFragment : DigiDFragment(R.layout.fragment_get_events) {

    private val args: GetEventsFragmentArgs by navArgs()
    private var _binding: FragmentGetEventsBinding? = null
    private val binding get() = _binding!!
    private val noDigidScreenDataUtil: NoDigidScreenDataUtil by inject()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentGetEventsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
        setBindings()
    }

    override fun getLoginType(): LoginType {
        return LoginType.Max
    }

    private fun setBindings() {
        val copy = getCopyForOriginType()

        binding.title.text = copy.title
        binding.description.setHtmlText(copy.description, htmlLinksEnabled = true)
        binding.button.setOnClickListener {
            onButtonClickWithRetryAction()
        }
        binding.noDigidButton.setOnClickListener {
            if (args.originType == RemoteOriginType.Vaccination) {
                navigateSafety(GetEventsFragmentDirections.actionNoDigid(
                    NoDigidFragmentData(
                        title = getString(R.string.holder_noDigiD_title),
                        description = getString(R.string.holder_noDigiD_message),
                        firstNavigationButtonData = noDigidScreenDataUtil.requestDigidButton(),
                        secondNavigationButtonData = noDigidScreenDataUtil.continueWithoutDigidButton(args.originType),
                        originType = args.originType
                    ))
                )
            } else {
                navigateSafety(
                    GetEventsFragmentDirections.actionPap(args.originType)
                )
            }
        }

        if (args.originType == RemoteOriginType.Vaccination) {
            binding.checkboxWithHeader.visibility = View.VISIBLE
            binding.checkboxWithHeader.header(R.string.holder_addVaccination_alsoCollectPositiveTestResults_message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(false)
    }

    override fun onDigidLoading(loading: Boolean) {
        binding.button.isEnabled = !loading
        binding.checkboxWithHeader.binding.checkbox.isEnabled = !loading
    }

    override fun onGetEventsLoading(loading: Boolean) {
        binding.button.isEnabled = !loading
        binding.checkboxWithHeader.binding.checkbox.isEnabled = !loading
    }

    override fun getOriginTypes(): List<RemoteOriginType> {
        val originTypes = mutableListOf<RemoteOriginType>()
        originTypes.add(args.originType)
        if (binding.checkboxWithHeader.binding.checkbox.isChecked) {
            originTypes.add(RemoteOriginType.Recovery)
        }
        return originTypes
    }

    override fun onNavigateToYourEvents(
        remoteProtocols: Map<RemoteProtocol, ByteArray>,
        eventProviders: List<EventProvider>
    ) {
        val flow = getFlow()
        val checkedPositiveTest = binding.checkboxWithHeader.binding.checkbox.isChecked
        navigateSafety(
            GetEventsFragmentDirections.actionYourEvents(
                type = YourEventsFragmentType.RemoteProtocol3Type(
                    remoteEvents = remoteProtocols,
                    eventProviders = eventProviders
                ),
                toolbarTitle = getCopyForOriginType().toolbarTitle,
                flow = if (flow == HolderFlow.Vaccination && checkedPositiveTest) {
                    HolderFlow.VaccinationAndPositiveTest
                } else {
                    flow
                }
            )
        )
    }
}

data class GetEventsFragmentCopy(
    val title: String,
    val description: String,
    val toolbarTitle: String,
    val hasNoEventsTitle: String,
    val hasNoEventsDescription: String
)
