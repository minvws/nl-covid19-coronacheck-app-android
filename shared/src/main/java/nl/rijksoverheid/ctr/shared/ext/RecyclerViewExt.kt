package nl.rijksoverheid.ctr.shared.ext

import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.executeAfterAllAnimationsAreFinished(
    callback: (RecyclerView) -> Unit
) = post(
    object : Runnable {
        override fun run() {
            if (isAnimating) {
                itemAnimator?.isRunning {
                    post(this)
                }
            } else {
                callback(this@executeAfterAllAnimationsAreFinished)
            }
        }
    }
)
