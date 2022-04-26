package nl.rijksoverheid.ctr.holder.paper_proof

import android.os.Bundle
import android.os.Handler
import android.view.View
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentDirections
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.GetEventsFragmentDirections
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofType
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreenUtil
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
            findNavControllerSafety()?.popBackStack()

            when (paperProofType) {
                is PaperProofType.DCC.Dutch -> {
                    holderMainActivityViewModel.navigate(
                        navDirections = PaperProofStartScanningFragmentDirections.actionPaperProofCode(paperProofType.qrContent),
                        delayMillis = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
                    )
                }
                is PaperProofType.DCC.Foreign -> {
                    holderMainActivityViewModel.navigate(
                        navDirections = PaperProofStartScanningFragmentDirections.actionYourEvents(
                            toolbarTitle = getString(R.string.your_dcc_event_toolbar_title),
                            type = YourEventsFragmentType.DCC(
                                remoteEvents = paperProofType.events,
                                originType = OriginType.fromTypeString(paperProofType.events.keys.first().events!!.first().type!!)
                            ),
                            flow = HolderFlow.HkviScan
                        )
                    )
                }
                is PaperProofType.CTB -> {
                    val navDirection = InfoFragmentDirections.actionPaperProofStartScanning(true)
                    holderMainActivityViewModel.navigate(
                        navDirections = PaperProofStartScanningFragmentDirections.actionInfoFragment(
                            toolbarTitle = getString(R.string.add_paper_proof_qr_scanner_title),
                            data = InfoFragmentData.TitleDescriptionWithButton(
                                title = getString(R.string.holder_scanner_error_title_ctb),
                                descriptionData = DescriptionData(
                                    htmlText = R.string.holder_scanner_error_message_ctb
                                ),
                                primaryButtonData = ButtonData.NavigationButton(
                                    text = getString(R.string.holder_scanner_error_action),
                                    navigationActionId = navDirection.actionId,
                                    navigationArguments = navDirection.arguments
                                )
                            )
                        )
                    )
                }
                is PaperProofType.Unknown -> {
                    val navDirection = InfoFragmentDirections.actionPaperProofStartScanning(true)
                    holderMainActivityViewModel.navigate(
                        navDirections = PaperProofStartScanningFragmentDirections.actionInfoFragment(
                            toolbarTitle = getString(R.string.add_paper_proof_qr_scanner_title),
                            data = InfoFragmentData.TitleDescriptionWithButton(
                                title = getString(R.string.holder_scanner_error_title_unknown),
                                descriptionData = DescriptionData(
                                    htmlText = R.string.holder_scanner_error_message_unknown
                                ),
                                primaryButtonData = ButtonData.NavigationButton(
                                    text = getString(R.string.holder_scanner_error_action),
                                    navigationActionId = navDirection.actionId,
                                    navigationArguments = navDirection.arguments
                                )
                            )
                        )
                    )
                }
            }
        })
    }
}