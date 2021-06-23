package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentGetRecoveryBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigiDFragment
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigidResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsPositiveTest
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemotePositiveTests
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.EventsResult
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class GetRecoveryFragment : DigiDFragment(R.layout.fragment_get_recovery) {

    private val dialogUtil: DialogUtil by inject()
    private val getRecoveryViewModel: GetRecoveryViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentGetRecoveryBinding.bind(view)

        digidViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        getRecoveryViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        getRecoveryViewModel.eventsResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is EventsResult.Success<RemotePositiveTests> -> {
                    // TODO Navigate to show your events
                }
                is EventsResult.HasNoEvents -> {
                    if (it.missingEvents) {
                        findNavController().navigate(
                            GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                                toolbarTitle = getString(R.string.your_positive_test_toolbar_title),
                                title = getString(R.string.missing_events_title),
                                description = getString(R.string.missing_events_description)
                            )
                        )
                    } else {
                        findNavController().navigate(
                            GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                                toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title),
                                title = getString(R.string.no_positive_test_result_title),
                                description = getString(R.string.no_positive_test_result_description)
                            )
                        )
                    }
                }
                is EventsResult.Error.CoronaCheckError.ServerError -> {
                    findNavController().navigate(
                        GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                            toolbarTitle = getString(R.string.your_positive_test_toolbar_title),
                            title = getString(R.string.coronacheck_error_title),
                            description = getString(R.string.coronacheck_error_description, it.httpCode.toString())
                        )
                    )
                }
                EventsResult.Error.EventProviderError.ServerError -> {
                    findNavController().navigate(
                        GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                            toolbarTitle = getString(R.string.your_positive_test_toolbar_title),
                            title = getString(R.string.event_provider_error_title),
                            description = getString(R.string.event_provider_error_description)
                        )
                    )
                }
                EventsResult.Error.NetworkError -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.dialog_no_internet_connection_title,
                        message = getString(R.string.dialog_no_internet_connection_description),
                        positiveButtonText = R.string.dialog_retry,
                        positiveButtonCallback = {
                            loginWithDigiD()
                        },
                        negativeButtonText = R.string.dialog_close
                    )
                }
            }
        })

        digidViewModel.digidResultLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is DigidResult.Success -> {
                    getRecoveryViewModel.getEvents(it.jwt)
                }
                is DigidResult.Failed -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.digid_login_failed_title,
                        message = getString(R.string.digid_login_failed_description, getString(R.string.type_vaccination)),
                        positiveButtonText = R.string.dialog_close,
                        positiveButtonCallback = {}
                    )
                }
            }
        })

        binding.button.setOnClickListener {
            loginWithDigiD()
        }
    }
}