package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ViewQrCodeBinding
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeData
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAccessibilityFocus

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodePagerAdapter: RecyclerView.Adapter<QrCodeViewHolder>() {
    val qrCodeDataList: MutableList<QrCodeData> = mutableListOf()

    fun addData(data: List<QrCodeData>) {
        qrCodeDataList.addAll(data)
        notifyItemRangeInserted(0, data.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrCodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_qr_code, parent, false)
        return QrCodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: QrCodeViewHolder, position: Int) {
        holder.bind(qrCodeDataList[position])
    }

    override fun getItemCount(): Int {
        return qrCodeDataList.size
    }
}

class QrCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(qrCodeData: QrCodeData) {
        val binding = ViewQrCodeBinding.bind(itemView)
        binding.image.setImageBitmap(qrCodeData.bitmap)
        binding.image.setAccessibilityFocus()
    }
}

