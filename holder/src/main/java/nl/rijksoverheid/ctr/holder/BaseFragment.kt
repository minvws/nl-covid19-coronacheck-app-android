package nl.rijksoverheid.ctr.holder

import androidx.fragment.app.Fragment

abstract class BaseFragment(contentLayoutId: Int) : Fragment(contentLayoutId) {

    sealed class Flow {
        object Startup: Flow()
        object CommercialTest: Flow()
        object Vaccination: Flow()
        object Recovery: Flow()
        object DigidTest: Flow()
        object HkviScan: Flow()
    }

    abstract fun getFlow(): Flow
}