package nl.rijksoverheid.ctr.holder.data_migration

import android.os.Bundle
import android.view.View
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentDataMigrationShowQrBinding
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.androidx.viewmodel.ext.android.viewModel

class DataMigrationShowQrCodeFragment : BaseFragment(R.layout.fragment_data_migration_show_qr) {

    private val viewModel: DataMigrationShowQrCodeViewModel by viewModel()

    override fun onButtonClickWithRetryAction() {
        // no-op
    }

    override fun getFlow(): Flow {
        return HolderFlow.Migration
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDataMigrationShowQrBinding.bind(view)
        binding.step.text = getString(R.string.holder_startMigration_onboarding_step, "3")

        viewModel.qrCodesLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is DataMigrationShowQrViewState.ShowError -> {
                    presentError(
                        data = ErrorResultFragmentData(
                            title = getString(R.string.error_something_went_wrong_title),
                            description = getString(
                                R.string.holder_migration_errorcode_message,
                                errorCodeStringFactory.get(getFlow(), it.errorResults)
                            ),
                            buttonTitle = getString(R.string.general_toMyOverview),
                            buttonAction = ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview)
                        )
                    )
                }
                is DataMigrationShowQrViewState.ShowQrs -> {
                    val bitmaps = it.bitmaps
                    binding.dataMigrationQrCodes.setImageBitmap(bitmaps.first())
                    var nextIndex = 2
                    val runnable = object : Runnable {
                        override fun run() {
                            binding.dataMigrationQrCodes.setImageBitmap(bitmaps[nextIndex])
                            nextIndex++
                            if (nextIndex == bitmaps.size) {
                                nextIndex = 0
                            }
                            binding.dataMigrationQrCodes.postDelayed(this, 200)
                        }
                    }
                    binding.dataMigrationQrCodes.postDelayed(runnable, 200)
                }
            }
        }

        viewModel.generateQrCodes(resources.displayMetrics.widthPixels)
    }
}
