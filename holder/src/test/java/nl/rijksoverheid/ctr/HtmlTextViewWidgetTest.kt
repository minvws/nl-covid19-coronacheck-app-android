/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr

import android.content.Context
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.test.core.app.ApplicationProvider
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import nl.rijksoverheid.ctr.design.widgets.HtmlTextViewWidget
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

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

        widget.setHtmlText("<h1>Heading 1</h1>")
        assertTrue(ViewCompat.isAccessibilityHeading(widget.getChildAt(0)))
        widget.setHtmlText("<h2>Heading 1</h2>")
        assertTrue(ViewCompat.isAccessibilityHeading(widget.getChildAt(0)))
        widget.setHtmlText("<h3>Heading 1</h3>")
        assertTrue(ViewCompat.isAccessibilityHeading(widget.getChildAt(0)))
        widget.setHtmlText("<h4>Heading 1</h4>")
        assertTrue(ViewCompat.isAccessibilityHeading(widget.getChildAt(0)))
        widget.setHtmlText("<h5>Heading 1</h5>")
        assertTrue(ViewCompat.isAccessibilityHeading(widget.getChildAt(0)))
        widget.setHtmlText("<h6>Heading 1</h6>")
        assertTrue(ViewCompat.isAccessibilityHeading(widget.getChildAt(0)))

        // List test
        widget.setHtmlText("<ul><li>List item 1</li><li>List item 2</li></ul>")
        assertEquals(widget.childCount, 1) // 3: Item + Item + Linebreak

        widget.setHtmlText("<ol><li>List item 1</li><li>List item 2</li></ol>")
        assertEquals(widget.childCount, 1) // 3: Item + Item + Linebreak
    }
}
