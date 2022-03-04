/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import nl.rijksoverheid.ctr.holder.databinding.FragmentMainBinding
import nl.rijksoverheid.ctr.shared.utils.Accessibility.makeIndeterminateAccessible

class HolderMainFragment : Fragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var _navController: NavController? = null
    private val navController get() = _navController!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMainBinding.bind(view)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        _navController = navHostFragment.navController

        val defaultToolbarElevation = resources.getDimension(R.dimen.toolbar_elevation)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.elevation = if (destination.id == R.id.nav_dashboard) {
                0f
            } else {
                defaultToolbarElevation
            }
        }
        binding.toolbar.setupWithNavController(navController)

        binding.toolbar.setNavigationOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.nav_your_events -> {
                    // Trigger custom dispatcher in destination
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    return@setNavigationOnClickListener
                }
            }

            NavigationUI.navigateUp(navController, null)
        }
    }

    fun presentLoading(loading: Boolean) {
        binding.loading.makeIndeterminateAccessible(
            context = requireContext(),
            isLoading = loading
        )
        binding.loading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun resetMenuItemListener() {
        binding.toolbar.setOnMenuItemClickListener {
            NavigationUI.onNavDestinationSelected(it, navController)
        }
    }
}
