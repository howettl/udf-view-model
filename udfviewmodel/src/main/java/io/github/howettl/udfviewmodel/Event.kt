package io.github.howettl.udfviewmodel

/**
 * An event that will result in a new view state.
 *
 * Example:
 *
 * ```
 * sealed interface Mutation : MutationEvent {
 *     data object OnSignInPressed : Mutation
 *     data object OnSignOutPressed : Mutation
 * }
 * ```
 */
interface MutationEvent

/**
 * An event that will not result in a new view state, useful for handling side effects in the view model.
 *
 * Example:
 *
 * ```
 * sealed interface Effect : EffectEvent {
 *     data object OnContinuePressed : Effect
 * }
 * ```
 */
interface EffectEvent

/**
 * A mutation implementation that can be used if there are no mutations required for this view model.
 *
 * Example:
 *
 * ```
 * class UdfViewModelImpl : UdfViewModel<ViewStateImpl, NoOpMutation, NoOpEffect>() {
 *
 *   // ...
 *
 *   override suspend fun mutateViewState(mutation: NoOpMutation): ViewStateImpl {
 *     return currentViewState
 *   }
 * }
 * ```
 */
sealed interface NoOpMutation : MutationEvent

/**
 * An effect implementation that can be used if there are no effects required for this view model.
 *
 * Example:
 *
 * ```
 * class UdfViewModelImpl : UdfViewModel<ViewStateImpl, NoOpMutation, NoOpEffect>() {
 *
 *   // ...
 *
 *   override suspend fun handleEffect(effect: NoOpEffect) {}
 * }
 * ```
 */
sealed interface NoOpEffect : EffectEvent
