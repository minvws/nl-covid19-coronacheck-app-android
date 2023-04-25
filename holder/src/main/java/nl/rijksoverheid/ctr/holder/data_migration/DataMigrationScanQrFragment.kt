package nl.rijksoverheid.ctr.holder.data_migration

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.qrscanner.QrCodeScannerFragment

class DataMigrationScanQrFragment : QrCodeScannerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.indicators.initIndicator(3)
        binding.indicators.updateSelected(2)
        binding.indicators.updateSelectedColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    override fun onQrScanned(content: String) {
    }

    override fun getCopy(): Copy {
        return Copy(
            title = getString(R.string.add_paper_proof_qr_scanner_title),
            message = "",
            extraContent = Copy.ExtraContent(
                header = getString(R.string.holder_startMigration_onboarding_step, "3"),
                title = getString(R.string.holder_startMigration_toThisDevice_onboarding_step3_title),
                message = getString(R.string.holder_startMigration_toThisDevice_onboarding_step3_message)
            )
        )
    }
}
