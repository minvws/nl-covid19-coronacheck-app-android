package nl.rijksoverheid.ctr.holder.fuzzy_matching

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentDirections
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.sync_greencards.SyncGreenCardsViewModel
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class FuzzyMatchingSyncFragment : BaseFragment(R.layout.fragment_saved_events_sync_green_cards) {

    private val syncGreenCardsViewModel: SyncGreenCardsViewModel by viewModel()

    private val infoFragmentUtil: InfoFragmentUtil by inject()

    private val fuzzyMatchingSyncFragmentArgs: FuzzyMatchingSyncFragmentArgs by navArgs()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // going back is disabled here to prevent invalid states while syncing
        }
    }

    override fun onButtonClickWithRetryAction() {
        // This screen does not have a retry button
    }

    override fun getFlow(): Flow {
        return HolderFlow.FuzzyMatching
    }

    private fun hideNavigationIcon() {
        (parentFragment?.parentFragment as? HolderMainFragment)?.getToolbar()?.navigationIcon = null
    }

    private fun disableBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideNavigationIcon()

        disableBackButton()

        syncGreenCardsViewModel.refresh()

        syncGreenCardsViewModel.databaseSyncerResultLiveData.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    is DatabaseSyncerResult.Success -> {
                        val navDirections = InfoFragmentDirections.actionMyOverview()
                        infoFragmentUtil.presentFullScreen(
                            currentFragment = this,
                            data = InfoFragmentData.TitleDescriptionWithButton(
                                title = getString(R.string.holder_identitySelection_success_title),
                                descriptionData = DescriptionData(
                                    htmlTextString = getString(
                                        R.string.holder_identitySelection_success_body,
                                        fuzzyMatchingSyncFragmentArgs.selectedName
                                    ),
                                    htmlLinksEnabled = true
                                ),
                                primaryButtonData = ButtonData.NavigationButton(
                                    text = getString(R.string.back_to_overview),
                                    navigationActionId = navDirections.actionId,
                                    navigationArguments = navDirections.arguments
                                )
                            ),
                            toolbarTitle = getString(R.string.holder_identitySelection_success_toolbar_title),
                            hideNavigationIcon = true
                        )
                    }
                    is DatabaseSyncerResult.Failed -> {
                        presentError(
                            errorResult = it.errorResult
                        )
                    }
                }
            })
    }
}
