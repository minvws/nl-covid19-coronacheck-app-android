/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui

import android.content.Context
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.design.views.HtmlTextViewWidget
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class HtmlTextViewWidgetTest : AutoCloseKoinTest() {

    @Test
    fun `testHtmlParsing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(context)

        val widget = HtmlTextViewWidget(context)

        // NOTE: Using multiple HtmlTextView children has temporary been disabled, therefore the assert is always 1.

        // Paragraph test
        widget.setHtmlText("Paragraph 1")
        assertEquals(widget.childCount, 1) // 1: Text

        widget.setHtmlText("Paragraph 1\nParagraph 2")
        assertEquals(widget.childCount, 1) // 1: Text

        widget.setHtmlText("<p>Paragraph 1</p>")
        assertEquals(widget.childCount, 1) // 2: Text + Linebreak

        widget.setHtmlText("Paragraph 1<br/>Paragraph 2")
        assertEquals(widget.childCount, 1) // 2: Text + Text

        widget.setHtmlText("<p>Paragraph 1</p><p>Paragraph 2</p>")
        assertEquals(widget.childCount, 1) // 3: Text + Linebreak + Text

        // Heading test
        widget.setHtmlText("<strong>Heading 1</strong><p>Paragraph 1</p><strong>Heading 2</strong><p>Paragraph 2</p>")
        assertEquals(widget.children.filter { ViewCompat.isAccessibilityHeading(it) }.count(), 0) // 2: Heading 1 + Heading 2
        assertEquals(widget.childCount, 1) // 5: Heading + Text + Linebreak + Heading + Text

        widget.setHtmlText("<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3><h4>Heading 4</h4><h5>Heading 5</h5><h6>Heading 6</h6>")
        assertEquals(widget.children.filter { ViewCompat.isAccessibilityHeading(it) }.count(), 0) // 6: h1, h2, h3, h4, h5, h6
        assertEquals(widget.childCount, 1) // 7: Heading + Heading + Heading + Heading + Heading + Heading + Linebreak

        // List test
        widget.setHtmlText("<ul><li>List item 1</li><li>List item 2</li></ul>")
        assertEquals(widget.childCount, 1) // 3: Item + Item + Linebreak

        widget.setHtmlText("<ol><li>List item 1</li><li>List item 2</li></ol>")
        assertEquals(widget.childCount, 1) // 3: Item + Item + Linebreak
    }
}
