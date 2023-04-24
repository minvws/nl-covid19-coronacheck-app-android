package nl.rijksoverheid.ctr.holder.data_migration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentDataMigrationShowQrBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DataMigrationShowQrCodeFragment : Fragment(R.layout.fragment_data_migration_show_qr) {

    private val viewModel: DataMigrationShowQrCodeViewModel by viewModel()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDataMigrationShowQrBinding.bind(view)
        binding.step.text = getString(R.string.holder_startMigration_onboarding_step, "3")

        viewModel.qrCodesLiveData.observe(viewLifecycleOwner) {
            binding.dataMigrationQrCodes.setImageBitmap(it.first())
        }

        viewModel.generateQrCodes(resources.displayMetrics.widthPixels)
    }
}
