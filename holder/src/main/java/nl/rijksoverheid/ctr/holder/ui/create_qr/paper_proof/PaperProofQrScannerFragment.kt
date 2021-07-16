package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.os.Bundle
import android.os.Handler
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

        Handler().postDelayed({
            onQrScanned("HC1:NCF%RN%TS3DH0RGPJB/IB-OM7533SR7694RI3XH8/FWP5IJBVGAMAU5PNPF6R:5SVBWVBDKBYLDZ4D74DWZJ\$7K+ CREDRCK*9C%PD8DJI7JSTNB95326HW4*IOQAOGU7\$35+Y5MT4K0P*5PP:7X\$RL353X7IKRE:7SA7G6M/NRO9SQKMHEE5IAXMFU*GSHGRKMXGG6DB-B93:GQBGZHHBIH5C9HFEC+GYHILIIX2MELNJIKCCHWIJNKMQ-ILKLXGGN+IRB84C9Q2LCIJ/HHKGL/BHOUB7IT8DJUIJ6DBSJLI7BI8AZ3CVOJ3BI9IL NILMLSVB*8BEPLA8KC42UIIUHSBKB+GIAZI3DJ/JAJZIR9KICT.XI/VB6TSYIJGDBGIA181:0TLOJJPACGKC2KRTI-8BEPL3DJ/LKQVBE2C*NIKYJIGK:H3J1DKVTQEDK8C+2TDSCNTCNJS6F3W.C\$USE\$2:*TIT3C7D8MS7LCTO3MMSSHT0\$U58PLY3 ZRA5PUF7MDN QKI7B\$WKL 6Q:S14GW4Q:LRERC6FPK1J*IUIH7S3J UQ2VQQ3ONV2CVR/TFFSQJ8KP.BENIQETGK6112U50-BW/IVK5")
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