package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.fragments.ErrorResultFragment
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPaperProofExplanationFragmentBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.bind
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
import org.koin.android.ext.android.inject

class PaperProofExplanationFragment : Fragment(R.layout.fragment_paper_proof_explanation_fragment) {

    private val infoFragmentUtil: InfoFragmentUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPaperProofExplanationFragmentBinding.bind(view)

        binding.itemHealthcare.bind(
            icon = R.drawable.ic_paper_proof_healthcare,
            title = R.string.add_paper_proof_explanation_healthcare_item_title,
            subtitle = R.string.add_paper_proof_explanation_healthcare_item_subtitle
        )
        binding.itemMail.bind(
            icon = R.drawable.ic_paper_proof_mail,
            title = R.string.add_paper_proof_explanation_mail_item_title,
            subtitle = R.string.add_paper_proof_explanation_mail_item_subtitle
        )

        binding.button.setOnClickListener {
            infoFragmentUtil.presentFullScreen(
                currentFragment = this,
                toolbarTitle = getString(R.string.add_paper_proof),
                data = InfoFragmentData.TitleDescriptionWithButton(
                    title = getString(R.string.add_paper_proof_self_printed_title),
                    descriptionData = DescriptionData(htmlText = R.string.add_paper_proof_self_printed_description),
                    secondaryButtonData = ButtonData.NavigationButton(
                        text = getString(R.string.add_paper_proof_self_printed_goto_add_proof_button),
                        navigationActionId = R.id.action_qr_type
                    )
                )
            )
        }

        binding.bottom.setButtonClick {
            try {
                val cameraManager =
                    requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
                if (cameraManager.cameraIdList.isNotEmpty()) {
                    navigateSafety(PaperProofExplanationFragmentDirections.actionPaperProofCode())
                } else {
                    showNoCameraError()
                }
            } catch (exception: CameraAccessException) {
                showNoCameraError()
            }
        }
    }

    private fun showNoCameraError() {
        findNavControllerSafety()?.navigate(
            R.id.action_error_result,
            ErrorResultFragment.getBundle(
                ErrorResultFragmentData(
                    title = getString(R.string.add_paper_proof_no_camera_error_header),
                    description = getString(R.string.add_paper_proof_no_camera_error_description),
                    buttonTitle = getString(R.string.back_to_overview),
                    buttonAction = ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview)
                )
            )
        )
    }
}