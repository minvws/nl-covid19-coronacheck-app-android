package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.fragments.ErrorResultFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPaperProofExplanationFragmentBinding
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData

class PaperProofExplanationFragment : Fragment(R.layout.fragment_paper_proof_explanation_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPaperProofExplanationFragmentBinding.bind(view)
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