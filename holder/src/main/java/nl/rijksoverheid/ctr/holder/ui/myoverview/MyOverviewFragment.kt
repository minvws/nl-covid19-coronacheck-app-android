package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.room.Database
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItems
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewFragment : Fragment(R.layout.fragment_my_overview) {

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY"
        const val EXTRA_BACK_FROM_QR = "EXTRA_BACK_FROM_QR"
        const val GREEN_CARD_TYPE = "GREEN_CARD_TYPE"
        const val RETURN_URI = "RETURN_URI"
    }

    private val section = Section()

    private val refreshOverviewItemsHandler = Handler(Looper.getMainLooper())
    private val refreshOverviewItemsRunnable = Runnable { refreshOverviewItems() }

    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()
    private val myOverviewViewModel: MyOverviewViewModel by viewModel()

    private val dialogUtil: DialogUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMyOverviewBinding.bind(view)
        initRecyclerView(binding)

        setFragmentResultListener(
            REQUEST_KEY
        ) { requestKey, bundle ->
            if (requestKey == REQUEST_KEY && bundle.getBoolean(
                    EXTRA_BACK_FROM_QR
                )
            ) {
                requireActivity().requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        myOverviewViewModel.myOverviewItemsLiveData.observe(viewLifecycleOwner,
            EventObserver { myOverviewItems ->
                setItems(
                    myOverviewItems = myOverviewItems
                )
            })

        observeSyncErrors()
    }

    private fun observeSyncErrors() {
        myOverviewViewModel.databaseSyncerResultLiveData.observe(viewLifecycleOwner,
            EventObserver {
                if (it is DatabaseSyncerResult.Failed) {
                    if (it is DatabaseSyncerResult.Failed.NetworkError && it.hasGreenCardsWithoutCredentials) {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = R.string.dialog_title_no_internet,
                            message = getString(R.string.dialog_credentials_expired_no_internet),
                            positiveButtonText = R.string.app_status_internet_required_action,
                            positiveButtonCallback = {
                                refreshOverviewItems(
                                    forceSync = true
                                )
                            },
                            negativeButtonText = R.string.dialog_close,
                        )
                    } else {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = R.string.dialog_title_no_internet,
                            message = getString(R.string.dialog_update_credentials_no_internet),
                            positiveButtonText = R.string.app_status_internet_required_action,
                            positiveButtonCallback = {
                                refreshOverviewItems(
                                    forceSync = true
                                )
                            },
                            negativeButtonText = R.string.dialog_close,
                        )
                    }
                }
            }
        )
    }

    private fun initRecyclerView(binding: FragmentMyOverviewBinding) {
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null
    }

    private fun refreshOverviewItems(forceSync: Boolean = false) {
        myOverviewViewModel.refreshOverviewItems(
            forceSync = forceSync,
            selectType = arguments?.getParcelable(GREEN_CARD_TYPE) ?: myOverviewViewModel.getSelectedType()
        )
        refreshOverviewItemsHandler.postDelayed(
            refreshOverviewItemsRunnable,
            TimeUnit.SECONDS.toMillis(cachedAppConfigUseCase.getCachedAppConfig().domesticQRRefreshSeconds.toLong())
        )
    }

    override fun onResume() {
        super.onResume()
        refreshOverviewItems()
    }

    override fun onPause() {
        super.onPause()
        refreshOverviewItemsHandler.removeCallbacks(refreshOverviewItemsRunnable)
    }

    private fun setItems(
        myOverviewItems: MyOverviewItems
    ) {

        val adapterItems = mutableListOf<BindableItem<*>>()
        myOverviewItems.items.forEach { myOverviewItem ->
            when (myOverviewItem) {
                is MyOverviewItem.HeaderItem -> {
                    adapterItems.add(
                        MyOverviewHeaderAdapterItem(
                            text = myOverviewItem.text
                        )
                    )
                }
                is MyOverviewItem.PlaceholderCardItem -> {
                    adapterItems.add(
                        MyOverviewGreenCardPlaceholderItem(
                            isEu = myOverviewItems.selectedType == GreenCardType.Eu
                        )
                    )
                }
                is MyOverviewItem.GreenCardItem -> {
                    adapterItems.add(
                        MyOverviewGreenCardAdapterItem(
                            greenCard = myOverviewItem.greenCard,
                            originStates = myOverviewItem.originStates,
                            credentialState = myOverviewItem.credentialState,
                            databaseSyncerResult = myOverviewItem.databaseSyncerResult,
                            onButtonClick = { greenCard, credential ->
                                navigateSafety(
                                    MyOverviewFragmentDirections.actionQrCode(
                                        toolbarTitle = when (greenCard.greenCardEntity.type) {
                                            is GreenCardType.Domestic -> {
                                                getString(R.string.domestic_qr_code_title)
                                            }
                                            is GreenCardType.Eu -> {
                                                getString(R.string.my_overview_test_result_international_title)
                                            }
                                        },
                                        data = QrCodeFragmentData(
                                            shouldDisclose = greenCard.greenCardEntity.type == GreenCardType.Domestic,
                                            credential = credential.data,
                                            credentialExpirationTimeSeconds = credential.expirationTime.toEpochSecond(),
                                            type = greenCard.greenCardEntity.type,
                                            originType = greenCard.origins.first().type
                                        ),
                                        returnUri = arguments?.getString(RETURN_URI)
                                    )
                                )
                            },
                            onRetryClick = {
                                refreshOverviewItems(
                                    forceSync = true
                                )
                            },
                        )
                    )
                }
                is MyOverviewItem.GreenCardExpiredItem -> {
                    adapterItems.add(MyOverviewGreenCardExpiredAdapterItem(
                        greenCardType = myOverviewItem.greenCardType,
                        onDismissClick = {
                            section.remove(it)
                        }
                    ))
                }
                is MyOverviewItem.OriginInfoItem -> {
                    adapterItems.add(MyOverviewOriginInfoAdapterItem(
                        greenCardType = myOverviewItem.greenCardType,
                        originType = myOverviewItem.originType,
                        onInfoClick = { greenCardType, originType ->
                            when (greenCardType) {
                                is GreenCardType.Domestic -> navigateToDomesticQr(originType)
                                is GreenCardType.Eu -> navigateToEuQr(originType)
                            }
                        }
                    ))
                }
                is MyOverviewItem.ClockDeviationItem -> {
                    adapterItems.add(MyOverviewClockDeviationItem(onInfoIconClicked = {
                        navigateSafety(MyOverviewTabsFragmentDirections.actionShowClockDeviationExplanation())
                    }))
                }
            }
        }

        section.update(adapterItems)
    }

    private fun navigateToEuQr(originType: OriginType) {
        when (originType) {
            is OriginType.Test -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_test),
                        description = getString(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_test)
                    )
                )
            }
            is OriginType.Vaccination -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                        description = getString(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_vaccination)
                    )
                )
            }
            is OriginType.Recovery -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_recovery),
                        description = getString(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_recovery)
                    )
                )
            }
        }
    }

    private fun navigateToDomesticQr(originType: OriginType) {
        when (originType) {
            is OriginType.Test -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_test),
                        description = getString(
                            R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_test
                        )
                    )
                )
            }
            is OriginType.Vaccination -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                        description = getString(R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_vaccination)
                    )
                )
            }
            is OriginType.Recovery -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_recovery),
                        description = getString(R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_recovery)
                    )
                )
            }
        }
    }
}
