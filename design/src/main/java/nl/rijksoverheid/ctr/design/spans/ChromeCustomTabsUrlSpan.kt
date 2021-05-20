package nl.rijksoverheid.ctr.design.spans

import android.text.style.URLSpan
import android.view.View
import android.webkit.URLUtil
import nl.rijksoverheid.ctr.shared.ext.launchUrl

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ChromeCustomTabsUrlSpan(url: String?) : URLSpan(url) {
    override fun onClick(widget: View) {
        super.onClick(widget)
        if (URLUtil.isNetworkUrl(url)) {
            url?.launchUrl(widget.context)
        }
    }
}
