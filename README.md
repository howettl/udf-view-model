# UdfViewModel

## About

A view model base class that enforces a unidirectional data flow pattern.

All changes to the view state must be executed via a call to `dispatchEvent` with a specific `MutationEvent`.
Also exposes an `EffectEvent` which can be used for the view model to action on events which do not result
in changes to the view state itself (ie. side effects like screen navigation).

If your use case does not require either a `MutationEvent` or an `EffectEvent`, you can use `NoOpMutation` and
`NoOpEffect` as generic parameters.

`onViewStateInitialized` will be called after a view starts subscribing to the `viewState`, but _before_ the first
emission. This could be used to do some asynchronous initialization of the view state, but be aware that this
will delay the first view state emission. In most cases it is probably better to have an initial empty (or loading)
state and dispatch an event from `onViewStateInitialized` to do the async setup.

`Mutation`s will always be applied in sequence, in the order they were dispatched. Duplicate view states are not
emitted.

## Download

```kotlin
repositories {
  mavenCentral()
}
dependencies {
  testImplementation("io.github.howettl:udfviewmodel:<latest-version>")
}
```

## Example

```kotlin
class UdfViewModelImpl : UdfViewModel<ViewStateImpl, Mutation, Effect>() {
    override fun createViewState() = ViewStateImpl()

    override suspend fun onViewStateInitialized() {
        // Start loading some data for the screen
    }

    override suspend fun mutateViewState(mutation: Mutation): ViewStateImpl {
        return when (mutation) {
            OnSignInPressed -> {
                // authenticate with a backend service ...
                currentViewState.copy(isSignedIn = true)
            }
            OnSignOutPressed -> {
                // log out of the backend service ...
                currentViewState.copy(isSignedIn = false)
            }
        }
    }

    override suspend fun handleEffect(effect: Effect) {
        when (effect) {
            OnContinuePressed -> {
                // Take some action, such as navigating to another screen
            }
        }
    }
}

data class ViewStateImpl(
    val isSignedIn: Boolean = false
) : ViewState

sealed interface Mutation : MutationEvent {
    data object OnSignInPressed : Mutation
    data object OnSignOutPressed : Mutation
}

sealed interface Effect : EffectEvent {
    data object OnContinuePressed : Effect
}
```
