package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.os.Bundle
import android.view.View
import com.google.mlkit.vision.barcode.Barcode
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.ValidatePaperProofResult
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerFragment
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaperProofQrScannerFragment : QrCodeScannerFragment() {

    companion object {
        const val EXTRA_COUPLING_CODE = "EXTRA_COUPLING_CODE"
    }

    private val dialogUtil: DialogUtil by inject()
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

    override fun getBarcodeFormats(): List<Int> {
        val formats = mutableListOf<Int>()
        formats.add(Barcode.FORMAT_QR_CODE)
        return formats
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paperProofScannerViewModel.loadingLiveData.observe(viewLifecycleOwner, EventObserver {
            binding.progress.visibility = if (it) View.VISIBLE else View.GONE
        })

        paperProofScannerViewModel.validatePaperProofResultLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is ValidatePaperProofResult.Success -> {
                    holderMainActivityViewModel.sendEvents(it.events)
                    findNavControllerSafety()?.popBackStack()
                }
                is ValidatePaperProofResult.Error.NetworkError -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.dialog_no_internet_connection_title,
                        message = getString(R.string.dialog_no_internet_connection_description),
                        positiveButtonText = R.string.ok,
                        positiveButtonCallback = { setupCamera() },
                        onDismissCallback = { setupCamera() }
                    )
                }
                is ValidatePaperProofResult.Error.ServerError -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.dialog_error_title,
                        message = getString(
                            R.string.dialog_error_message_with_error_code,
                            it.httpCode.toString()
                        ),
                        positiveButtonText = R.string.ok,
                        positiveButtonCallback = { setupCamera() },
                        onDismissCallback = { setupCamera() }
                    )
                }
                is ValidatePaperProofResult.Error.ExpiredQr -> {
                    holderMainActivityViewModel.sendValidatePaperProofError(it)
                    findNavControllerSafety()?.popBackStack()
                }
                is ValidatePaperProofResult.Error.BlockedQr -> {
                    holderMainActivityViewModel.sendValidatePaperProofError(it)
                    findNavControllerSafety()?.popBackStack()
                }
                is ValidatePaperProofResult.Error.RejectedQr -> {
                    holderMainActivityViewModel.sendValidatePaperProofError(it)
                    findNavControllerSafety()?.popBackStack()
                }
                is ValidatePaperProofResult.Error.InvalidQr -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.add_paper_proof_qr_error_dutch_qr_code_dialog_title,
                        message = getString(R.string.add_paper_proof_qr_error_invalid_qr_dialog_description),
                        positiveButtonText = R.string.ok,
                        positiveButtonCallback = { setupCamera() },
                        onDismissCallback = { setupCamera() }
                    )
                }
                is ValidatePaperProofResult.Error.DutchQr -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.add_paper_proof_qr_error_dutch_qr_code_dialog_title,
                        message = getString(R.string.add_paper_proof_qr_error_invalid_qr_dialog_description),
                        positiveButtonText = R.string.ok,
                        positiveButtonCallback = { setupCamera() },
                        onDismissCallback = { setupCamera() }
                    )
                }
            }
        })
    }
}