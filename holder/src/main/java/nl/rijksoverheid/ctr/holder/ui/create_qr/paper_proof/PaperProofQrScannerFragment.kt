package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.Barcode
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerFragment
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PaperProofQrScannerFragment : QrCodeScannerFragment() {
    private val holderMainActivityViewModel: HolderMainActivityViewModel by sharedViewModel()

    override fun onQrScanned(content: String) {

    }

    override fun getCopy(): Copy {
        return Copy(
                title = getString(R.string.add_paper_proof_qr_scanner_title),
                message = getString(R.string.add_paper_proof_qr_scanner_text),
                rationaleDialog = Copy.RationaleDialog(
                        title = getString(R.string.camera_rationale_dialog_title),
                        description = getString(R.string.camera_rationale_dialog_description),
                        okayButtonText = getString(R.string.ok)
                )
        )
    }

    override fun getBarcodeFormats(): List<Int> {
        val formats = mutableListOf<Int>()
        formats.add(Barcode.FORMAT_QR_CODE)
        return formats
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler().postDelayed({
            holderMainActivityViewModel.sendEvents(listOf())
            findNavControllerSafety()?.popBackStack()
        }, 2000)
    }
}