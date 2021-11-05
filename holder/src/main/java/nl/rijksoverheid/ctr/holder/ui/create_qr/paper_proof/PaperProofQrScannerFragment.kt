package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.os.Bundle
import android.view.View
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.ValidatePaperProofResult
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerFragment
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaperProofQrScannerFragment : QrCodeScannerFragment() {

    companion object {
        const val EXTRA_COUPLING_CODE = "EXTRA_COUPLING_CODE"
    }

    private val holderMainActivityViewModel: HolderMainActivityViewModel by sharedViewModel()
    private val paperProofScannerViewModel: PaperProofQrScannerViewModel by viewModel()

    override fun onQrScanned(content: String) {
        val couplingCode =
            arguments?.getString(EXTRA_COUPLING_CODE) ?: error("Coupling code cannot be null")
        paperProofScannerViewModel.validatePaperProof(
            qrContent = content,
            couplingCode = couplingCode
        )
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paperProofScannerViewModel.loadingLiveData.observe(viewLifecycleOwner, EventObserver {
            binding.progress.visibility = if (it) View.VISIBLE else View.GONE
        })

        paperProofScannerViewModel.validatePaperProofResultLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is ValidatePaperProofResult.Valid -> {
                    holderMainActivityViewModel.sendEvents(it.events)
                    findNavControllerSafety()?.popBackStack()
                }
                is ValidatePaperProofResult.Invalid -> {
                    holderMainActivityViewModel.sendValidatePaperProofInvalid(it)
                    findNavControllerSafety()?.popBackStack()
                }
            }
        })
    }
}