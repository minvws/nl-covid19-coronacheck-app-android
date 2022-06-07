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

    private var currentPosition = 0

    private val overlayVisibilityStates = mutableListOf<QrCodeViewHolder.OverlayVisibility>()

    fun addData(data: List<QrCodeData>) {
        val hasItems = qrCodeDataList.isNotEmpty()
        val hideOverlayInCurrentPosition =
            overlayVisibilityStates.getOrNull(currentPosition) == QrCodeViewHolder.OverlayVisibility.HIDDEN
                    || overlayVisibilityStates.getOrNull(currentPosition) == QrCodeViewHolder.OverlayVisibility.EXPIRED
        qrCodeDataList.clear()
        overlayVisibilityStates.clear()
        data.forEachIndexed { index, it ->
            // if user chose to display the QR, don't hide it until scrolled away from it
            if (index == currentPosition && hideOverlayInCurrentPosition) {
                overlayVisibilityStates.add(QrCodeViewHolder.OverlayVisibility.VISIBLE)
            } else {
                overlayVisibilityStates.add(isQrCodeHidden(it))
            }
        }
        qrCodeDataList.addAll(data)
        if (hasItems) {
            notifyItemRangeChanged(0, data.size)
        } else {
            notifyItemRangeInserted(0, data.size)
        }
    }

    private fun isQrCodeHidden(data: QrCodeData): QrCodeViewHolder.OverlayVisibility {
        val vaccinationData = data as? QrCodeData.European.Vaccination
        return when {
            vaccinationData?.isDoseNumberSmallerThanTotalDose == true -> QrCodeViewHolder.OverlayVisibility.HIDDEN
            vaccinationData?.isExpired == true -> QrCodeViewHolder.OverlayVisibility.EXPIRED
            else -> QrCodeViewHolder.OverlayVisibility.VISIBLE
        }
    }

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
            overlayVisibilityStates[currentPosition] = QrCodeViewHolder.OverlayVisibility.VISIBLE
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
        overlayVisibility: OverlayVisibility,
        onOverlayButtonClick: () -> Unit,
    ) {
        val showOverlay = overlayVisibility == OverlayVisibility.VISIBLE

        binding.image.setImageBitmap(qrCodeData.bitmap)
        binding.overlayShowQrButton.setOnClickListener {
            onOverlayButtonClick.invoke()
        }

        // using View.INVISIBLE instead View.GONE cause the latter breaks
        // the click listener for physical keyboards accessibility
        setOverlay(showOverlay, overlayVisibility)

        // not visible pages can also gain focus, so we have to take care of that for hardware keyboard users
        binding.image.isFocusable = isCurrentlyDisplayed && !showOverlay
        binding.image.importantForAccessibility = if (isCurrentlyDisplayed && !showOverlay) {
            View.IMPORTANT_FOR_ACCESSIBILITY_YES
        } else {
            View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        binding.overlay.isFocusable = isCurrentlyDisplayed && showOverlay
    }

    private fun setOverlay(
        showOverlay: Boolean,
        overlayVisibility: OverlayVisibility
    ) {
        binding.overlay.visibility = if (showOverlay) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
        binding.overlayText.text = when (overlayVisibility) {
            OverlayVisibility.HIDDEN -> itemView.context.getText(R.string.qr_code_hidden_title)
            OverlayVisibility.EXPIRED -> "QR-code is verborgen"
            else -> ""
        }
        binding.overlayText.setCompoundDrawablesWithIntrinsicBounds(
            0,
            when (overlayVisibility) {
                OverlayVisibility.HIDDEN -> R.drawable.ic_visibility_off
                OverlayVisibility.EXPIRED -> R.drawable.ic_qr_hidden
                else -> 0
            },
            0,
            0
        )
    }

    enum class OverlayVisibility {
        HIDDEN, EXPIRED, VISIBLE
    }
}
