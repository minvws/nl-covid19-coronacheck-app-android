package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItems
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.sharedViewModelWithOwner
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ViewModelOwner
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
    }

    private val section = Section()

    private val refreshOverviewItemsHandler = Handler(Looper.getMainLooper())
    private val refreshOverviewItemsRunnable = Runnable { refreshOverviewItems() }

    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()
    private val myOverviewViewModel: MyOverviewViewModel by sharedViewModelWithOwner(
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_graph_overview),
                this
            )
        })

    private val dialogUtil: DialogUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMyOverviewBinding.bind(view)
        initRecyclerView(binding)

        setListeners(binding)

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
                    binding = binding,
                    myOverviewItems = myOverviewItems
                )
            })

        myOverviewViewModel.databaseSyncerResultLiveData.observe(viewLifecycleOwner,
            EventObserver {
                if (it is DatabaseSyncerResult.NetworkError) {
                    if (it.hasGreenCardsWithoutCredentials) {
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

    private fun setListeners(binding: FragmentMyOverviewBinding) {
        binding.addQrButton.setOnClickListener {
            findNavController().navigate(
                MyOverviewFragmentDirections.actionQrType()
            )
        }

        binding.scroll.setOnScrollChangeListener { _, _, _, _, _ ->
            setBottomElevation(binding)
        }
    }

    private fun refreshOverviewItems(forceSync: Boolean = false) {
        myOverviewViewModel.refreshOverviewItems(
            forceSync = forceSync
        )
        refreshOverviewItemsHandler.postDelayed(refreshOverviewItemsRunnable, TimeUnit.SECONDS.toMillis(10))
    }

    override fun onResume() {
        super.onResume()
        refreshOverviewItems()

        (parentFragment?.parentFragment as HolderMainFragment?)?.getToolbar().let { toolbar ->
            if (toolbar?.menu?.size() == 0) {
                toolbar.apply {
                    inflateMenu(R.menu.overview_toolbar)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        refreshOverviewItemsHandler.removeCallbacks(refreshOverviewItemsRunnable)
        (parentFragment?.parentFragment as HolderMainFragment).getToolbar().menu.clear()
    }

    private fun setItems(
        binding: FragmentMyOverviewBinding,
        myOverviewItems: MyOverviewItems
    ) {
        binding.typeToggle.root.visibility = View.GONE
        binding.bottom.visibility = View.GONE

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
                    adapterItems.add(MyOverviewGreenCardPlaceholderItem())
                }
                is MyOverviewItem.GreenCardItem -> {
                    adapterItems.add(
                        MyOverviewGreenCardAdapterItem(
                            greenCard = myOverviewItem.greenCard,
                            originStates = myOverviewItem.originStates,
                            credentialState = myOverviewItem.credentialState,
                            databaseSyncerResult = myOverviewItem.databaseSyncerResult,
                            onButtonClick = { greenCard, credential ->
                                findNavControllerSafety()?.navigate(
                                    MyOverviewFragmentDirections.actionQrCode(
                                        toolbarTitle = when (greenCard.greenCardEntity.type) {
                                            is GreenCardType.Domestic -> {
                                                getString(R.string.my_overview_test_result_title)
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
                                        )
                                    )
                                )
                            },
                            onRetryClick = { refreshOverviewItems(
                                forceSync = true
                            ) },
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
                is MyOverviewItem.TravelModeItem -> {
                    binding.typeToggle.root.visibility = View.VISIBLE
                    binding.typeToggle.description.setText(myOverviewItem.text)

                    binding.typeToggle.button.setText(myOverviewItem.buttonText)

                    binding.typeToggle.button.setOnClickListener {
                        findNavControllerSafety()?.navigate(MyOverviewFragmentDirections.actionShowTravelMode())
                    }
                }
                MyOverviewItem.AddCertificateItem -> binding.bottom.visibility = View.VISIBLE
            }
        }

        section.update(adapterItems)

        setBottomElevation(binding)
    }

    private fun navigateToEuQr(originType: OriginType) {
        when (originType) {
            is OriginType.Test -> {
                findNavControllerSafety()?.navigate(
                    MyOverviewFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_test),
                        description = getString(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_test)
                    )
                )
            }
            is OriginType.Vaccination -> {
                findNavControllerSafety()?.navigate(
                    MyOverviewFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                        description = getString(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_vaccination)
                    )
                )
            }
            is OriginType.Recovery -> {
                findNavControllerSafety()?.navigate(
                    MyOverviewFragmentDirections.actionShowQrExplanation(
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
                findNavControllerSafety()?.navigate(
                    MyOverviewFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_test),
                        description = getString(
                            R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_test,
                            cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours()
                                .toString()
                        )
                    )
                )
            }
            is OriginType.Vaccination -> {
                findNavControllerSafety()?.navigate(
                    MyOverviewFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                        description = getString(R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_vaccination)
                    )
                )
            }
            is OriginType.Recovery -> {
                findNavControllerSafety()?.navigate(
                    MyOverviewFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_recovery),
                        description = getString(R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_recovery)
                    )
                )
            }
        }
    }

    private fun setBottomElevation(binding: FragmentMyOverviewBinding) {
        binding.bottom.cardElevation = if (binding.scroll.canScrollVertically(1)) {
            resources.getDimensionPixelSize(R.dimen.scroll_view_button_elevation).toFloat()
        } else 0f
    }
}
