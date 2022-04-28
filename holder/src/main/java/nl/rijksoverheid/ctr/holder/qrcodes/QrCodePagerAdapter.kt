/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.qrcodes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ViewQrCodeBinding
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeData

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodePagerAdapter : RecyclerView.Adapter<QrCodeViewHolder>() {

    val qrCodeDataList: MutableList<QrCodeData> = mutableListOf()

    private var currentPosition = 1

    private val overlayVisibilityStates = mutableListOf<Boolean>()

    fun addData(data: List<QrCodeData>) {
        val hasItems = qrCodeDataList.isNotEmpty()
        qrCodeDataList.clear()
        overlayVisibilityStates.clear()
        data.forEach { overlayVisibilityStates.add(isQrCodeHidden(it)) }
        qrCodeDataList.addAll(data)
        if (hasItems) {
            notifyItemRangeChanged(0, data.size)
        } else {
            notifyItemRangeInserted(0, data.size)
        }
    }

    private fun isQrCodeHidden(data: QrCodeData) =
        (data as? QrCodeData.European.Vaccination)?.isHidden == true

    fun onPositionChanged(position: Int) {
        currentPosition = position
        overlayVisibilityStates.forEachIndexed { index, _ ->
            overlayVisibilityStates[index] = isQrCodeHidden(qrCodeDataList[index])
        }
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrCodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_qr_code, parent, false)
        return QrCodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: QrCodeViewHolder, position: Int) {
        holder.bind(
            qrCodeDataList[position],
            position == currentPosition,
            overlayVisibilityStates[position]
        ) {
            overlayVisibilityStates[currentPosition] = false
            notifyItemChanged(currentPosition)
        }
    }

    override fun getItemCount(): Int {
        return qrCodeDataList.size
    }

}

class QrCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val binding = ViewQrCodeBinding.bind(itemView)

    fun bind(
        qrCodeData: QrCodeData,
        isCurrentlyDisplayed: Boolean,
        showOverlay: Boolean,
        onOverlayButtonClick: () -> Unit,
    ) {
        binding.image.setImageBitmap(qrCodeData.bitmap)
        binding.overlayButton.setOnClickListener {
            onOverlayButtonClick.invoke()
        }

        // using View.INVISIBLE instead View.GONE cause the latter breaks
        // the click listener for physical keyboards accessibility
        binding.overlay.visibility = if (showOverlay) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }

        // not visible pages can also gain focus, so we have to take care of that for hardware keyboard users
        binding.image.isFocusable = isCurrentlyDisplayed && !showOverlay
        binding.image.importantForAccessibility = if (isCurrentlyDisplayed && !showOverlay) {
            View.IMPORTANT_FOR_ACCESSIBILITY_YES
        } else {
            View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        binding.overlay.isFocusable = isCurrentlyDisplayed && showOverlay
    }
}
