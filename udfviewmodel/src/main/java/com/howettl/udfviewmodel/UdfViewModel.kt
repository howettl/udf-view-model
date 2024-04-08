package com.howettl.udfviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class UdfViewModel<State : ViewState, Mutation : MutationEvent, Effect : EffectEvent> : ViewModel() {
    val viewState: StateFlow<State> by lazy {
        mutationQueue.onSubscription {
            onViewStateInitialized()
        }.map { mutateViewState(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = createViewState(),
        )
    }
    protected val currentViewState: State
        get() = viewState.value

    private val mutationQueue: MutableSharedFlow<Mutation> = MutableSharedFlow()

    abstract fun createViewState(): State
    open suspend fun onViewStateInitialized() {}
    abstract suspend fun mutateViewState(mutation: Mutation): State
    abstract suspend fun handleEffect(effect: Effect)

    fun dispatchEvent(mutation: Mutation) {
        viewModelScope.launch {
            mutationQueue.emit(mutation)
        }
    }

    fun dispatchEvent(effect: Effect) {
        viewModelScope.launch {
            handleEffect(effect)
        }
    }
}
