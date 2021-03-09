package nl.rijksoverheid.ctr.design

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class FullScreenDialogFragment(contentLayoutId: Int) : DialogFragment(contentLayoutId) {

    sealed class AnimationStyle(open val styleRes: Int) {
        object SlideFromBottom : AnimationStyle(R.style.SlideFromBottomFragmentAnimations)
        object Fade : AnimationStyle(R.style.FadeFragmentAnimations)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.AppTheme_Dialog_FullScreen
        )
    }

    abstract fun getAnimationStyle(): AnimationStyle

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setWindowAnimations(getAnimationStyle().styleRes)
        return dialog
    }
}


