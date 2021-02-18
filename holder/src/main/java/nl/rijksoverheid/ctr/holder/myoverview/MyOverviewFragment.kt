package nl.rijksoverheid.ctr.holder.myoverview

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.myoverview.items.MyOverviewHeaderAdapterItem
import nl.rijksoverheid.ctr.holder.myoverview.items.MyOverviewNavigationCardAdapterItem
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewFragment : Fragment(R.layout.fragment_my_overview) {

    private lateinit var binding: FragmentMyOverviewBinding
    private val localTestResultViewModel: LocalTestResultViewModel by sharedViewModel(
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_home),
                this
            )
        }
    )
    private val qrCodeViewModel: QrCodeViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMyOverviewBinding.bind(view)

        val section = Section()
        GroupAdapter<GroupieViewHolder>().run {
            add(section)
            binding.recyclerView.adapter = this
        }
        section.run {
            addAll(
                listOf(
                    MyOverviewHeaderAdapterItem(),
                    MyOverviewNavigationCardAdapterItem(
                        title = R.string.my_overview_no_qr_make_appointment_title,
                        description = R.string.my_overview_no_qr_make_appointment_description,
                        backgroundColor = Color.parseColor("#69dbff"),
                        buttonText = R.string.my_overview_no_qr_make_appointment_button,
                        onButtonClick = {

                        }
                    ),
                    MyOverviewNavigationCardAdapterItem(
                        title = R.string.my_overview_no_qr_make_qr_title,
                        description = R.string.my_overview_no_qr_make_qr_description,
                        backgroundColor = Color.parseColor("#3dec94"),
                        buttonText = R.string.my_overview_no_qr_make_qr_button,
                        onButtonClick = {

                        }
                    ),
                )
            )
        }
    }
}
