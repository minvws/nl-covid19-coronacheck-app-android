/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.rijksoverheid.ctr.design.views.HtmlTextViewWidget
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentYourEventExplanationBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.InfoScreen

class YourEventExplanationAdapter(private val dataSet: Array<InfoScreen>) :
    RecyclerView.Adapter<YourEventExplanationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.subheader)
        val htmlTextViewWidget: HtmlTextViewWidget = view.findViewById(R.id.description)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.your_event_explanation_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.htmlTextViewWidget.setHtmlText(dataSet[position].description)
    }

    override fun getItemCount() = dataSet.size

}

class YourEventExplanationFragment : Fragment(R.layout.fragment_your_event_explanation) {

    private val args: YourEventExplanationFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentYourEventExplanationBinding.bind(view)

        val adapter = YourEventExplanationAdapter(
            dataSet = args.data
        )

        binding.scroll.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(requireContext(), linearLayoutManager.orientation)
        binding.scroll.layoutManager = linearLayoutManager
        if (args.data.size > 1) {
            binding.scroll.addItemDecoration(dividerItemDecoration)
        }
    }
}
