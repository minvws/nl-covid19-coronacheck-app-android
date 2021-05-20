package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

/**
 * Get all cards that should be displayed in MyOverviewFragment
 */
interface GetMyOverviewItemsUseCase {
    suspend fun get(
        walletId: Int,
        type: GreenCardType
    ): MyOverviewItems
}

class GetMyOverviewItemsUseCaseImpl(private val holderDatabase: HolderDatabase) :
    GetMyOverviewItemsUseCase {

    override suspend fun get(
        walletId: Int,
        type: GreenCardType
    ): MyOverviewItems {
        val greenCards = holderDatabase.greenCardDao().getAll(
            walletId = walletId,
            type = type
        )

        val items = when (type) {
            is GreenCardType.Domestic -> {
                if (greenCards.isEmpty()) {
                    listOf(
                        MyOverviewItem.Header(
                            text = R.string.my_overview_no_qr_description
                        ),
                        MyOverviewItem.CreateQrCard(greenCards.isNotEmpty()),
                    )
                } else {
                    val qrCards = greenCards.map { MyOverviewItem.GreenCardItem(it) }
                    val items = mutableListOf<MyOverviewItem>()
                    items.add(
                        MyOverviewItem.Header(
                            text = R.string.my_overview_description
                        )
                    )
                    qrCards.forEach { qrCard ->
                        items.add(qrCard)
                    }
                    items.add(MyOverviewItem.ToggleGreenCardTypeItem)
                    items
                }
            }
            is GreenCardType.Eu -> {
                val qrCards = greenCards.map { MyOverviewItem.GreenCardItem(it) }
                val items = mutableListOf<MyOverviewItem>()
                items.add(
                    MyOverviewItem.Header(
                        text = R.string.my_overview_description_eu
                    )
                )
                qrCards.forEach { qrCard ->
                    items.add(qrCard)
                }
                items.add(MyOverviewItem.ToggleGreenCardTypeItem)
                items
            }
        }

        return MyOverviewItems(
            type = type,
            items = items
        )
    }
}

data class MyOverviewItems(
    val type: GreenCardType,
    val items: List<MyOverviewItem>
)

sealed class MyOverviewItem {

    data class Header(@StringRes val text: Int) : MyOverviewItem()
    data class CreateQrCard(val hasGreenCards: Boolean) : MyOverviewItem()

    data class GreenCardItem(
        val greenCard: GreenCard
    ) : MyOverviewItem()

    sealed class BannerItem(
        @StringRes open val text: Int,
        @DrawableRes open val icon: Int,
    ) : MyOverviewItem() {
        // UI card with info icon
        data class Close(
            override val text: Int
        ) : BannerItem(text, R.drawable.ic_close)

        // UI card with close icon
        data class Info(
            override val text: Int,
            override val icon: Int,
            val screen: Screen
        ) : BannerItem(text, R.drawable.ic_question) {

            data class Screen(
                @StringRes val title: Int,
                @StringRes val description: Int
            )
        }
    }

    object ToggleGreenCardTypeItem : MyOverviewItem()
}
