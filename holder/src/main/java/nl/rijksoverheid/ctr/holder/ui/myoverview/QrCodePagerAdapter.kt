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
class QrCodePagerAdapter : RecyclerView.Adapter<QrCodeViewHolder>() {

    val qrCodeDataList: MutableList<QrCodeData> = mutableListOf()

    var isOverlayStateReset: Boolean = true
        set(value) {
            field = value
            if (value) resetOverlays.forEach { it.invoke() }
        }

    /** functions to each view holder to reset the overlay state */
    private var resetOverlays: MutableList<(() -> Unit)> = mutableListOf()

    fun addData(data: List<QrCodeData>) {
        val hasItems = qrCodeDataList.isNotEmpty()
        qrCodeDataList.clear()
        resetOverlays.clear()
        qrCodeDataList.addAll(data)
        if (hasItems) {
            notifyItemRangeChanged(0, data.size)
        } else {
            notifyItemRangeInserted(0, data.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrCodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_qr_code, parent, false)
        return QrCodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: QrCodeViewHolder, position: Int) {
        holder.bind(qrCodeDataList[position], isOverlayStateReset) { isOverlayStateReset = false }
        resetOverlays.add { holder.resetOverlay(qrCodeDataList[position]) }
    }

    override fun getItemCount(): Int {
        return qrCodeDataList.size
    }
}

class QrCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val binding = ViewQrCodeBinding.bind(itemView)

    fun bind(
        qrCodeData: QrCodeData,
        isOverlayStateReset: Boolean,
        onOverlayButtonClicked: () -> Unit
    ) {
        binding.image.setImageBitmap(qrCodeData.bitmap)
        binding.image.setAccessibilityFocus()
        binding.overlayButton.setOnClickListener {
            binding.overlay.visibility = View.GONE
            onOverlayButtonClicked.invoke()
        }
        if (isOverlayStateReset) {
            binding.overlay.visibility = if (isHidden(qrCodeData)) View.VISIBLE else View.GONE
        }
    }

    fun resetOverlay(qrCodeData: QrCodeData) {
        binding.overlay.visibility = if (isHidden(qrCodeData)) View.VISIBLE else View.GONE
    }

    private fun isHidden(qrCodeData: QrCodeData) =
        (qrCodeData as? QrCodeData.European.Vaccination)?.isHidden == true
}
