/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.qrscanner

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class ScannerOverlayWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val cornerRadius = resources.getDimension(R.dimen.overlay_cutout_corner_radius)
    private val overlayMargin = resources.getDimensionPixelSize(R.dimen.overlay_cutout_margin)
    private val overlayPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.overlay_color)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val overlayRectPath = Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Determine center
        val centerOfCanvas = Point(w / 2, h / 2)
        // Calculate width of cutout based on width or height depending on orientation
        val rectW = if (w < h) {
            w - (2 * overlayMargin)
        } else {
            h * (2 * overlayMargin)
        }
        // Calculate corner coordinates
        val left = centerOfCanvas.x - rectW / 2
        val top = centerOfCanvas.y - rectW / 2
        val right = centerOfCanvas.x + rectW / 2
        val bottom = centerOfCanvas.y + rectW / 2

        // Create path of rounded rectangle based on coordinates calculated above
        overlayRectPath.addRoundRect(
            RectF(
                left.toFloat(),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat()
            ), cornerRadius, cornerRadius, Path.Direction.CW
        )
        // Set filltype to Inverse Even Odd to draw outside of the path only
        overlayRectPath.fillType = Path.FillType.INVERSE_EVEN_ODD
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(overlayRectPath, overlayPaint)
    }
}