package nl.rijksoverheid.ctr.holder.your_events.models

sealed class ConflictingEventResult {
    object Holder : ConflictingEventResult()
    object Existing : ConflictingEventResult()
    object None : ConflictingEventResult()
}
