package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionManager
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import nl.rijksoverheid.ctr.design.utils.BottomSheetData
import nl.rijksoverheid.ctr.design.utils.BottomSheetDialogUtil
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentQrCodesBinding
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.QrInfoScreenUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.ExternalReturnAppData
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeData
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodesResult
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodesFragment : Fragment(R.layout.fragment_qr_codes) {

    private var _binding: FragmentQrCodesBinding? = null
    private val binding get() = _binding!!
    private val args: QrCodesFragmentArgs by navArgs()
    private val personalDetailsUtil: PersonalDetailsUtil by inject()
    private val infoScreenUtil: QrInfoScreenUtil by inject()
    private val dialogUtil: DialogUtil by inject()
    private val bottomSheetDialogUtil: BottomSheetDialogUtil by inject()
    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()
    private lateinit var qrCodePagerAdapter: QrCodePagerAdapter

    private val qrCodeHandler = Handler(Looper.getMainLooper())
    private val qrCodeRunnable = Runnable {
        generateQrCode()
        checkIfCredentialExpired()
    }

    private val qrCodeViewModel: QrCodesViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.FLAVOR == "prod") {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        val params = requireActivity().window.attributes
        params?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        requireActivity().window.attributes = params
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        _binding = FragmentQrCodesBinding.bind(view)

        setupViewPager()
        applyStyling()
        dispatchTouchEventDoseInfo()

        qrCodeViewModel.qrCodeDataListLiveData.observe(viewLifecycleOwner, ::bindQrCodeDataList)
        qrCodeViewModel.returnAppLivedata.observe(viewLifecycleOwner, ::returnToApp)

        args.returnUri?.let { qrCodeViewModel.onReturnUriGiven(it, args.data.type) }
    }

    /**
     * Dispatch touch events on the overlapping dose info view to have the animation view mirror itself.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun dispatchTouchEventDoseInfo() {
        binding.doseInfo.setOnTouchListener { v, event ->
            binding.animation.dispatchTouchEvent(event)
            true
        }
    }

    private fun setupViewPager() {
        qrCodePagerAdapter = QrCodePagerAdapter()
        binding.viewPager.adapter = qrCodePagerAdapter
    }

    private fun applyStyling() {
        when (args.data.type) {
            is GreenCardType.Domestic -> {
                binding.animation.setWidget(R.raw.skatefiets2)
            }
            is GreenCardType.Eu -> {
                binding.animation.setWidget(R.raw.moving_walkway)
            }
        }
    }

    private fun returnToApp(externalReturnAppData: ExternalReturnAppData?) {
        binding.button.run {
            if (externalReturnAppData != null) {
                visibility = View.VISIBLE
                text = getString(R.string.qr_code_return_app_button, externalReturnAppData.appName)
                setOnClickListener { startIntent(externalReturnAppData) }
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun startIntent(externalReturnAppData: ExternalReturnAppData) {
        try {
            startActivity(externalReturnAppData.intent)
        } catch (exception: ActivityNotFoundException) {
            dialogUtil.presentDialog(
                context = requireContext(),
                title = R.string.dialog_error_title,
                message = getString(R.string.dialog_error_message),
                positiveButtonText = R.string.dialog_close,
                positiveButtonCallback = {}
            )
        }
    }

    private fun bindQrCodeDataList(qrCodesResult: QrCodesResult) {
        presentQrLoading(false)

        when (qrCodesResult) {
            is QrCodesResult.SingleQrCode -> {
                qrCodePagerAdapter.addData(listOf(qrCodesResult.qrCodeData))
            }
            is QrCodesResult.MultipleQrCodes -> {
                qrCodePagerAdapter.addData(qrCodesResult.europeanVaccinationQrCodeDataList)
                if (binding.qrVaccinationIndicators.visibility == View.GONE) {
                    // Setup extra viewpager UI only once
                    setupEuropeanVaccinationQr(
                        qrCodesResult.europeanVaccinationQrCodeDataList,
                        qrCodesResult.mostRelevantVaccinationIndex
                    )
                }
            }
        }

        // Nullable so tests don't trip over parentFragment
        (parentFragment?.parentFragment as HolderMainFragment?)?.getToolbar().let { toolbar ->
            if (toolbar?.menu?.size() == 0) {
                toolbar.apply {
                    inflateMenu(R.menu.my_qr_toolbar)

                    setOnMenuItemClickListener {
                        val qrCodeData =
                            qrCodePagerAdapter.qrCodeDataList.get(binding.viewPager.currentItem)
                        if (it.itemId == R.id.action_show_qr_explanation) {
                            val infoScreen = when (qrCodeData) {
                                is QrCodeData.Domestic -> {
                                    val personalDetails = personalDetailsUtil.getPersonalDetails(
                                        firstNameInitial = qrCodeData.readDomesticCredential.firstNameInitial,
                                        lastNameInitial = qrCodeData.readDomesticCredential.lastNameInitial,
                                        birthDay = qrCodeData.readDomesticCredential.birthDay,
                                        birthMonth = qrCodeData.readDomesticCredential.birthMonth
                                    )

                                    infoScreenUtil.getForDomesticQr(
                                        personalDetails = personalDetails
                                    )
                                }
                                is QrCodeData.European -> {
                                    when (args.data.originType) {
                                        is OriginType.Test -> {
                                            infoScreenUtil.getForEuropeanTestQr(
                                                qrCodeData.readEuropeanCredential
                                            )
                                        }
                                        is OriginType.Vaccination -> {
                                            infoScreenUtil.getForEuropeanVaccinationQr(
                                                qrCodeData.readEuropeanCredential
                                            )
                                        }
                                        is OriginType.Recovery -> {
                                            infoScreenUtil.getForEuropeanRecoveryQr(
                                                qrCodeData.readEuropeanCredential
                                            )
                                        }
                                    }
                                }
                            }

                            bottomSheetDialogUtil.present(
                                childFragmentManager, BottomSheetData.TitleDescriptionWithFooter(
                                    title = infoScreen.title,
                                    applyOnDescription = {
                                        it.setHtmlText(infoScreen.description)
                                    },
                                    footerText = infoScreen.footer
                                )
                            )
                        }
                        true
                    }
                }
            }
        }
    }

    /**
     * Show extra UI when we are dealing with european vaccination qrs
     */
    private fun setupEuropeanVaccinationQr(
        europeanVaccinations: List<QrCodeData.European.Vaccination>,
        mostRelevantVaccinationIndex: Int
    ) {
        // Make extra UI visible to show more information about the QR
        binding.vaccinationQrsContainer.visibility = View.VISIBLE
        binding.qrVaccinationDosis.visibility = View.VISIBLE
        binding.qrVaccinationDosis.text = getString(
            R.string.qr_code_dosis,
            "${europeanVaccinations.first().dose}/${europeanVaccinations.first().ofTotalDoses}"
        )

        // If there are more then one vaccinations we update UI based on the selected page
        if (europeanVaccinations.size > 1) {
            // Initialize our viewpager indicators
            binding.qrVaccinationIndicators.visibility = View.VISIBLE
            binding.qrVaccinationIndicators.initIndicator(europeanVaccinations.size)

            binding.viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    // Select current indicator
                    binding.qrVaccinationIndicators.updateSelected(position)

                    Handler(Looper.getMainLooper()).post {
                        binding.nextQrButton.visibility = if (position == europeanVaccinations.size - 1) View.INVISIBLE else View.VISIBLE
                        binding.previousQrButton.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE

                        val vaccination = europeanVaccinations[position]
                        binding.qrVaccinationDosis.text = getString(
                            R.string.qr_code_dosis,
                            "${vaccination.dose}/${vaccination.ofTotalDoses}"
                        )

                        showDoseInfo(vaccination)
                    }

                    // reset qr overlay state on page change
                    qrCodePagerAdapter.isOverlayStateReset = true
                }
            })

            // Default select the last item
            binding.viewPager.setCurrentItem(mostRelevantVaccinationIndex, false)

            // Make buttons click to scroll through viewpager
            binding.previousQrButton.setOnClickListener {
                binding.viewPager.setCurrentItem(binding.viewPager.currentItem - 1, true)
            }

            binding.nextQrButton.setOnClickListener {
                binding.viewPager.setCurrentItem(binding.viewPager.currentItem + 1, true)
            }
        }
    }

    private fun showDoseInfo(vaccination: QrCodeData.European.Vaccination) {
        TransitionManager.beginDelayedTransition(binding.bottomScroll)
        when {
            vaccination.isOverVaccinated -> {
                binding.doseInfo.text = getString(
                    R.string.qr_code_over_vaccinated,
                    "${vaccination.ofTotalDoses}/${vaccination.ofTotalDoses}"
                )
                binding.doseInfo.visibility = View.VISIBLE
            }
            vaccination.isHidden -> {
                binding.doseInfo.text = getString(R.string.qr_code_newer_dose_available)
                binding.doseInfo.visibility = View.VISIBLE
            }
            else -> binding.doseInfo.visibility = View.GONE
        }
    }

    private fun presentQrLoading(loading: Boolean) {
        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(loading)
        binding.root.visibility = if (loading) View.GONE else View.VISIBLE
    }

    private fun generateQrCode() {
        qrCodeViewModel.generateQrCodes(
            greenCardType = args.data.type,
            originType = args.data.originType,
            size = resources.displayMetrics.widthPixels,
            credentials = args.data.credentials,
            shouldDisclose = args.data.shouldDisclose
        )
        val refreshMillis =
            if (BuildConfig.FLAVOR == "tst") TimeUnit.SECONDS.toMillis(10) else TimeUnit.SECONDS.toMillis(
                cachedAppConfigUseCase.getCachedAppConfig().domesticQRRefreshSeconds.toLong()
            )
        qrCodeHandler.postDelayed(qrCodeRunnable, refreshMillis)
    }

    /**
     * If the QR is expired we close this fragment
     * The [MyOverviewFragment] should correctly handle new or expired credentials
     */
    private fun checkIfCredentialExpired() {
        val expirationTime = OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(args.data.credentialExpirationTimeSeconds),
            ZoneOffset.UTC
        )
        if (OffsetDateTime.now(ZoneOffset.UTC).isAfter(expirationTime)) {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        presentQrLoading(true)
        generateQrCode()
    }

    override fun onPause() {
        super.onPause()
        qrCodeHandler.removeCallbacks(qrCodeRunnable)
        (parentFragment?.parentFragment as HolderMainFragment).let {
            it.getToolbar().menu.clear()
            // Reset menu item listener to default
            it.resetMenuItemListener()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Set brightness back to previous
        val params = requireActivity().window.attributes
        params?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        requireActivity().window.attributes = params

        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(false)

        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}