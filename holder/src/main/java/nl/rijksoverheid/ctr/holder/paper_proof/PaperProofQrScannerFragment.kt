package nl.rijksoverheid.ctr.holder.paper_proof

import android.os.Bundle
import android.os.Handler
import android.view.View
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerFragment
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaperProofQrScannerFragment : QrCodeScannerFragment() {

    private val holderMainActivityViewModel: HolderMainActivityViewModel by sharedViewModel()
    private val paperProofScannerViewModel: PaperProofQrScannerViewModel by viewModel()

    override fun onQrScanned(content: String) {
        paperProofScannerViewModel.getType(
            qrContent = content
        )
    }

    override fun getCopy(): Copy {
        return Copy(
            title = getString(R.string.add_paper_proof_qr_scanner_title),
            message = getString(R.string.add_paper_proof_qr_scanner_text),
            rationaleDialog = Copy.RationaleDialog(
                title = R.string.camera_rationale_dialog_title,
                description = getString(R.string.camera_rationale_dialog_description),
                okayButtonText = R.string.ok
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paperProofScannerViewModel.loadingLiveData.observe(viewLifecycleOwner, EventObserver {
            binding.progress.visibility = if (it) View.VISIBLE else View.GONE
        })

        paperProofScannerViewModel.paperProofTypeLiveData.observe(viewLifecycleOwner, EventObserver {
            findNavControllerSafety()?.popBackStack()
            holderMainActivityViewModel.navigate(
                navDirections = PaperProofStartScanningFragmentDirections.actionPaperProofCode(it.qrContent),
                delayMillis = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            )
        })

        Handler().postDelayed({
            onQrScanned("")
        }, 2000)
    }
}