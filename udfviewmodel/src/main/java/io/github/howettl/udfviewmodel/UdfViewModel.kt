package io.github.howettl.udfviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * A view model base class that enforces a unidirectional data flow pattern.
 *
 * All changes to the view state must be executed via a call to [dispatchEvent] with a specific [MutationEvent].
 * Also exposes an [EffectEvent] which can be used for the view model to action on events which do not result
 * in changes to the view state itself (ie. side effects like screen navigation).
 *
 * If your use case does not require either a [MutationEvent] or an [EffectEvent], you can use [NoOpMutation] and
 * [NoOpEffect] as generic parameters.
 *
 * [onViewStateInitialized] will be called after a view starts subscribing to the [viewState], but _before_ the first
 * emission. This could be used to do some asynchronous initialization of the view state, but be aware that this
 * will delay the first view state emission. In most cases it is probably better to have an initial empty (or loading)
 * state and dispatch an event from [onViewStateInitialized] to do the async setup.
 *
 * [Mutation]s will always be applied in sequence, in the order they were dispatched. Duplicate view states are not
 * emitted.
 */
abstract class UdfViewModel<State : ViewState, Mutation : MutationEvent, Effect : EffectEvent> : ViewModel() {

    /**
     * A flow that emits the a new view state each time a [Mutation] has been applied.
     * This should be collected by the view and rendered on each emission.
     *
     * Jetpack Compose example:
     * ```
     * val viewState = viewModel.viewState.collectAsState()
     * val isSignedIn: State<Boolean> = remember { derivedStateOf { viewState.value.isSignedIn } }
     * ```
     */
    val viewState: StateFlow<State> by lazy {
        mutationQueue.onSubscription {
            onViewStateInitialized()
        }.map { mutateViewState(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = createViewState(),
        )
    }

    /**
     * The view state as it was last emitted. This is useful within [mutateViewState] to copy the data class.
     */
    protected val currentViewState: State
        get() = viewState.value

    private val mutationQueue: MutableSharedFlow<Mutation> = MutableSharedFlow()

    /**
     * An initial view state object. It's recommended that [ViewState] implementations be data classes.
     */
    abstract fun createViewState(): State

    /**
     * This will be invoked after the view subscribes to the view state, but before the first view state is emitted.
     */
    open suspend fun onViewStateInitialized() {}

    /**
     * Returns a new view state object with the given [mutation] applied.
     *
     * Example:
     * ```
     * override suspend fun mutateViewState(mutation: Mutation): ViewStateImpl {
     *     return when (mutation) {
     *         OnSignInPressed -> currentViewState.copy(isSignedIn = true)
     *         OnSignOutPressed -> currentViewState.copy(isSignedIn = false)
     *     }
     * }
     * ```
     */
    abstract suspend fun mutateViewState(mutation: Mutation): State

    /**
     * Execute an action for the given [effect].
     *
     * Example:
     * ```
     * override suspend fun handleEffect(effect: Effect) {
     *     when (effect) {
     *         OnContinuePressed -> navigateToNextScreen()
     *     }
     * }
     * ```
     */
    abstract suspend fun handleEffect(effect: Effect)

    /**
     * Fire an event that will result in a change to the view state.
     */
    fun dispatchEvent(mutation: Mutation) {
        viewModelScope.launch {
            mutationQueue.emit(mutation)
        }
    }

    /**
     * Fire an event that will not result in a change to the view state.
     */
    fun dispatchEvent(effect: Effect) {
        viewModelScope.launch {
            handleEffect(effect)
        }
    }
}