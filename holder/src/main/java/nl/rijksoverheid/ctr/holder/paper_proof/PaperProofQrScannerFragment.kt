package nl.rijksoverheid.ctr.holder.paper_proof

import android.os.Bundle
import android.view.View
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofType
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerFragment
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaperProofQrScannerFragment : QrCodeScannerFragment() {

    private val holderMainActivityViewModel: HolderMainActivityViewModel by sharedViewModel()
    private val paperProofScannerViewModel: PaperProofQrScannerViewModel by viewModel()
    private val dialogUtil: DialogUtil by inject()

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

        paperProofScannerViewModel.paperProofTypeLiveData.observe(viewLifecycleOwner, EventObserver { paperProofType ->
            when (paperProofType) {
                is PaperProofType.DCC.Dutch -> {
                    findNavControllerSafety()?.popBackStack()
                    holderMainActivityViewModel.navigate(
                        navDirections = PaperProofStartScanningFragmentDirections.actionPaperProofCode(paperProofType.qrContent),
                        delayMillis = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
                    )
                }
                is PaperProofType.DCC.Foreign -> {
                    findNavControllerSafety()?.popBackStack()
                    holderMainActivityViewModel.navigate(
                        navDirections = PaperProofStartScanningFragmentDirections.actionYourEvents(
                            toolbarTitle = getString(R.string.your_dcc_event_toolbar_title),
                            type = YourEventsFragmentType.DCC(
                                remoteEvent = paperProofType.remoteProtocol,
                                eventGroupJsonData = paperProofType.eventGroupJsonData,
                                originType = OriginType.fromTypeString(paperProofType.remoteProtocol.events!!.first().type!!)
                            ),
                            flow = HolderFlow.HkviScanned(paperProofType.remoteProtocol)
                        )
                    )
                }
                is PaperProofType.CTB -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.holder_scanner_error_title_ctb,
                        message = getString(R.string.holder_scanner_error_message_ctb),
                        positiveButtonText = R.string.holder_scanner_error_action,
                        positiveButtonCallback = {
                            setupScanner()
                        }
                    )
                }
                is PaperProofType.Unknown -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.holder_scanner_error_title_unknown,
                        message = getString(R.string.holder_scanner_error_message_unknown),
                        positiveButtonText = R.string.holder_scanner_error_action,
                        positiveButtonCallback = {
                            setupScanner()
                        }
                    )
                }
            }
        })
    }
}
