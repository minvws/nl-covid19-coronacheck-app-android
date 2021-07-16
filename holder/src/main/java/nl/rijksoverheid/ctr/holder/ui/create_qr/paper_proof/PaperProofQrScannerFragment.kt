package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.Barcode
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.YourEventsFragmentDirections
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
        val couplingCode = arguments?.getString(EXTRA_COUPLING_CODE) ?: error("Coupling code cannot be null")
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

        Handler().postDelayed({
            onQrScanned("HC1:NCFO20\$80T9WTWGVLK-49NJ3B0J\$OCC*AX*4FBB.R3*70J+9DN03E52F3%0US.3Y50.FK8ZKO/EZKEZ967L6C56GVC*JC1A6QW63W5KF6746TPCBEC7ZKW.CSEE*KEQPC.OEFOAF\$DN34VKE0/DLPCG/DSEE5IA\$M8NNASNAQY9 R7.HAB+9 JC:.DNUAU3EI3D5WE TAQ1A7:EDOL9WEQDD+Q6TW6FA7C466KCN9E%961A6DL6FA7D46.JCP9EJY8L/5M/5546.96VF6.JCBECB1A-:8\$966469L6OF6VX6FVCPD0KQEPD0LVC6JD846Y96D463W5307UPCBJCOT9+EDL8FHZ95/D QEALEN44:+C%69AECAWE:34: CJ.CZKE9440/D+34S9E5LEWJC0FD3%4AIA%G7ZM81G72A6J+9QG7OIBENA.S90IAY+A17A+B9:CB*6AVX8AF6F:5678M2927SM6NAN24WKP0VTMO8.CMJF1CF-*7%XN3R0C0E45L0EKUGEA-SL0HYN71PBTWHCITDHPIHG/A7%8U9PEBHEPD9DD4\$O4000FGW5HIWGG")
        }, 2000)

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
                        positiveButtonCallback = {}
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
                        positiveButtonCallback = {}
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
                        positiveButtonCallback = {}
                    )
                }
                is ValidatePaperProofResult.Error.DutchQr -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.add_paper_proof_qr_error_dutch_qr_code_dialog_title,
                        message = getString(R.string.add_paper_proof_qr_error_invalid_qr_dialog_description),
                        positiveButtonText = R.string.ok,
                        positiveButtonCallback = {}
                    )
                }
            }
        })
    }
}