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
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.persistence.RefreshCredentialsJob
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

    private val getQrCardsHandler = Handler(Looper.getMainLooper())
    private val getQrCardsRunnable = Runnable { getQrCards(syncDatabase = false) }

    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()
    private val myOverviewViewModel: MyOverviewViewModel by sharedViewModelWithOwner(
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_graph_overview),
                this
            )
        })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMyOverviewBinding.bind(view)
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null

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

        myOverviewViewModel.myOverviewItemsLiveData.observe(
            viewLifecycleOwner,
            EventObserver { myOverviewItems ->
                setItems(
                    binding = binding,
                    myOverviewItems = myOverviewItems
                )
            })

        RefreshCredentialsJob.schedule(requireContext(), cachedAppConfigUseCase.getCachedAppConfig()?.credentialRenewalDays?.toLong() ?: 5L)
    }

    private fun getQrCards(syncDatabase: Boolean) {
        myOverviewViewModel.refreshOverviewItems(syncDatabase = syncDatabase)
        getQrCardsHandler.postDelayed(getQrCardsRunnable, TimeUnit.SECONDS.toMillis(10))
    }

    override fun onResume() {
        super.onResume()
        getQrCards(syncDatabase = true)

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
        getQrCardsHandler.removeCallbacks(getQrCardsRunnable)
        (parentFragment?.parentFragment as HolderMainFragment).getToolbar().menu.clear()
    }

    private fun setItems(
        binding: FragmentMyOverviewBinding,
        myOverviewItems: MyOverviewItems
    ) {
        binding.typeToggle.root.visibility = View.GONE

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
                is MyOverviewItem.CreateQrCardItem -> {
                    adapterItems.add(MyOverviewNavigationCardAdapterItem(
                        title = R.string.my_overview_no_qr_make_qr_title,
                        description = R.string.my_overview_no_qr_make_qr_description,
                        backgroundColor = R.color.secondary_green,
                        backgroundDrawable = R.drawable.illustration_create_qr,
                        buttonText = R.string.my_overview_no_qr_make_qr_button,
                        onButtonClick = {
                            findNavControllerSafety(R.id.nav_my_overview)?.navigate(
                                MyOverviewFragmentDirections.actionCreateQr()
                            )
                        }
                    ))
                }
                is MyOverviewItem.GreenCardItem -> {
                    adapterItems.add(
                        MyOverviewGreenCardAdapterItem(
                            greenCard = myOverviewItem.greenCard,
                            originStates = myOverviewItem.originStates,
                            credentialState = myOverviewItem.credentialState,
                            launchDate = myOverviewItem.launchDate,
                            loading = myOverviewItems.loading,
                            onButtonClick = { greenCard, credential ->
                                findNavController().navigate(MyOverviewFragmentDirections.actionQrCode(
                                    QrCodeFragmentData(
                                        shouldDisclose = greenCard.greenCardEntity.type == GreenCardType.Domestic,
                                        credential = credential.data,
                                        credentialExpirationTimeSeconds = credential.expirationTime.toEpochSecond(),
                                        type = greenCard.greenCardEntity.type,
                                        originType = greenCard.origins.first().type
                                    )
                                ))
                            }
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
                                is GreenCardType.Domestic -> {
                                    when (originType) {
                                        is OriginType.Test -> {
                                            findNavController().navigate(MyOverviewFragmentDirections.actionShowQrExplanation(
                                                title = getString(R.string.my_overview_green_card_not_valid_title_test),
                                                description = getString(R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_test, cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours().toString())
                                            ))
                                        }
                                        is OriginType.Vaccination -> {
                                            findNavController().navigate(MyOverviewFragmentDirections.actionShowQrExplanation(
                                                title = getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                                                description = getString(R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_vaccination)
                                            ))
                                        }
                                        is OriginType.Recovery -> {
                                            // TODO
                                        }
                                    }
                                }
                                is GreenCardType.Eu -> {
                                    when (originType) {
                                        is OriginType.Test -> {
                                            findNavController().navigate(MyOverviewFragmentDirections.actionShowQrExplanation(
                                                title = getString(R.string.my_overview_green_card_not_valid_title_test),
                                                description = getString(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_test)
                                            ))
                                        }
                                        is OriginType.Vaccination -> {
                                            findNavController().navigate(MyOverviewFragmentDirections.actionShowQrExplanation(
                                                title = getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                                                description = getString(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_vaccination)
                                            ))
                                        }
                                        is OriginType.Recovery -> {
                                            // TODO
                                        }
                                    }
                                }
                            }
                        }
                    ))
                }
                is MyOverviewItem.TravelModeItem -> {
                    binding.typeToggle.root.visibility = View.VISIBLE
                    binding.typeToggle.description.setText(myOverviewItem.text)

                    binding.typeToggle.button.setText(myOverviewItem.buttonText)

                    binding.typeToggle.button.setOnClickListener {
                        findNavController().navigate(MyOverviewFragmentDirections.actionShowTravelMode())
                    }
                }
            }
        }

        section.update(adapterItems)
    }
}
