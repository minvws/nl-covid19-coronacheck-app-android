package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentScanPaperProofBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanPaperProofFragment : Fragment(R.layout.fragment_scan_paper_proof) {

    private val viewModel: ScanPaperQrViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FragmentScanPaperProofBinding.bind(view).run {
            button.setOnClickListener {
                viewModel.onQrScanned(
                    "HC1:NCFO20\$80T9WTWGVLK-49NJ3B0J\$OCC*AX*4FBB.R3*70J+9DN03E52F3%0US.3Y50.FK8ZKO/EZKEZ967L6C56GVC*JC1A6QW63W5KF6746TPCBEC7ZKW.CSEE*KEQPC.OEFOAF\$DN34VKE0/DLPCG/DSEE5IA\$M8NNASNAQY9 R7.HAB+9 JC:.DNUAU3EI3D5WE TAQ1A7:EDOL9WEQDD+Q6TW6FA7C466KCN9E%961A6DL6FA7D46.JCP9EJY8L/5M/5546.96VF6.JCBECB1A-:8\$966469L6OF6VX6FVCPD0KQEPD0LVC6JD846Y96D463W5307UPCBJCOT9+EDL8FHZ95/D QEALEN44:+C%69AECAWE:34: CJ.CZKE9440/D+34S9E5LEWJC0FD3%4AIA%G7ZM81G72A6J+9QG7OIBENA.S90IAY+A17A+B9:CB*6AVX8AF6F:5678M2927SM6NAN24WKP0VTMO8.CMJF1CF-*7%XN3R0C0E45L0EKUGEA-SL0HYN71PBTWHCITDHPIHG/A7%8U9PEBHEPD9DD4\$O4000FGW5HIWGG"
                )
            }
        }
    }
}