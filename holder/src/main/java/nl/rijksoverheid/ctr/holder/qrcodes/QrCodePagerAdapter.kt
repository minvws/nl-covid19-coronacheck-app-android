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
import androidx.core.view.isVisible
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
class QrCodePagerAdapter(private val onOverlayExplanationClick: (QrCodeViewHolder.QrCodeVisibility) -> Unit) :
    RecyclerView.Adapter<QrCodeViewHolder>() {

    val qrCodeDataList: MutableList<QrCodeData> = mutableListOf()

    private var currentPosition = 0

    private val overlayVisibilityStates = mutableListOf<QrCodeViewHolder.QrCodeVisibility>()

    fun addData(data: List<QrCodeData>) {
        val hasItems = qrCodeDataList.isNotEmpty()
        val hideOverlayInCurrentPosition =
            overlayVisibilityStates.getOrNull(currentPosition) == QrCodeViewHolder.QrCodeVisibility.VISIBLE
        qrCodeDataList.clear()
        overlayVisibilityStates.clear()
        data.forEachIndexed { index, it ->
            // if user chose to display the QR, don't hide it until scrolled away from it
            if (index == currentPosition && hideOverlayInCurrentPosition) {
                overlayVisibilityStates.add(QrCodeViewHolder.QrCodeVisibility.VISIBLE)
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

    private fun isQrCodeHidden(data: QrCodeData): QrCodeViewHolder.QrCodeVisibility {
        val vaccinationData = data as? QrCodeData.European.Vaccination
        return when {
            (data as? QrCodeData.European)?.isExpired == true -> QrCodeViewHolder.QrCodeVisibility.EXPIRED
            vaccinationData?.isDoseNumberSmallerThanTotalDose == true -> QrCodeViewHolder.QrCodeVisibility.HIDDEN
            else -> QrCodeViewHolder.QrCodeVisibility.VISIBLE
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
            overlayVisibilityStates[position],
            onOverlayExplanationClick
        ) {
            overlayVisibilityStates[currentPosition] = QrCodeViewHolder.QrCodeVisibility.VISIBLE
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
        qrCodeVisibility: QrCodeVisibility,
        onOverlayExplanationClick: (QrCodeVisibility) -> Unit,
        onOverlayButtonClick: () -> Unit
    ) {
        val showOverlay = qrCodeVisibility != QrCodeVisibility.VISIBLE

        binding.image.setImageBitmap(qrCodeData.bitmap)
        binding.overlayShowQrButton.setOnClickListener {
            onOverlayButtonClick.invoke()
        }

        if (qrCodeData is QrCodeData.European.NonVaccination && !qrCodeData.explanationNeeded) {
            binding.overlayButton.isVisible = false
        } else {
            binding.overlayButton.setOnClickListener {
                onOverlayExplanationClick.invoke(qrCodeVisibility)
            }
        }

        // using View.INVISIBLE instead View.GONE cause the latter breaks
        // the click listener for physical keyboards accessibility
        setOverlay(showOverlay, qrCodeVisibility)

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
        qrCodeVisibility: QrCodeVisibility
    ) {
        binding.overlay.visibility = if (showOverlay) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
        binding.overlayText.text = itemView.context.getString(
            if (qrCodeVisibility == QrCodeVisibility.HIDDEN) {
                R.string.qr_code_hidden_title } else {
                R.string.holder_qr_code_expired_overlay_title
            }
        )
        binding.overlayText.setCompoundDrawablesWithIntrinsicBounds(
            0,
            when (qrCodeVisibility) {
                QrCodeVisibility.HIDDEN -> R.drawable.ic_visibility_off
                QrCodeVisibility.EXPIRED -> R.drawable.ic_qr_hidden
                else -> 0
            },
            0,
            0
        )
    }

    enum class QrCodeVisibility {
        HIDDEN, EXPIRED, VISIBLE
    }
}
