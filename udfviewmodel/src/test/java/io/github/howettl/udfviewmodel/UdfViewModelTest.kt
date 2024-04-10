package io.github.howettl.udfviewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.howettl.udfviewmodel.UdfViewModelTest.Effect.OnContinuePressed
import io.github.howettl.udfviewmodel.UdfViewModelTest.Mutation.*
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class UdfViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun getViewModel(
        initializedListener: () -> Unit = {},
        navigationEventListener: () -> Unit = {},
    ): UdfViewModelImpl {
        return UdfViewModelImpl(
            initializedListener = initializedListener,
            navigationEventListener = navigationEventListener,
        )
    }

    @Test
    fun `onViewStateInitialized is called at initialization`() = runTest {
        val initListener = spyk<() -> Unit>()
        val vm = getViewModel(
            initializedListener = initListener,
        )

        vm.viewState.test {
            awaitItem()
            verify { initListener.invoke() }
        }
    }

    @Test
    fun `initial view state is emitted`() = runTest {
        val vm = getViewModel()
        vm.viewState.test {
            assertThat(awaitItem().isSignedIn).isFalse()
        }
    }

    @Test
    fun `dispatching mutation triggers view state update`() = runTest {
        val vm = getViewModel()
        vm.viewState.test {
            assertThat(awaitItem().isSignedIn).isFalse() // initial state
            vm.dispatchEvent(OnSignInPressed)
            assertThat(awaitItem().isSignedIn).isTrue()
        }
    }

    @Test
    fun `dispatching a mutation that doesn't change anything does not emit a new state`() = runTest {
        val vm = getViewModel()
        vm.viewState.test {
            assertThat(awaitItem().isSignedIn).isFalse() // initial state
            vm.dispatchEvent(OnSignOutPressed)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `multiple mutations trigger multiple view state emissions`() = runTest {
        val vm = getViewModel()
        vm.viewState.test {
            awaitItem() // initial state
            vm.dispatchEvent(OnSignInPressed)
            assertThat(awaitItem().isSignedIn).isTrue()
            vm.dispatchEvent(OnSignOutPressed)
            assertThat(awaitItem().isSignedIn).isFalse()
        }
    }

    @Test
    fun `dispatching an effect works properly`() = runTest {
        val navListener = spyk<() -> Unit>()
        val vm = getViewModel(
            navigationEventListener = navListener,
        )
        vm.viewState.test {
            awaitItem() // initial state
            vm.dispatchEvent(OnContinuePressed)
            verify { navListener.invoke() }
        }
    }

    private class UdfViewModelImpl(
        private val initializedListener: () -> Unit,
        private val navigationEventListener: () -> Unit,
    ) : UdfViewModel<ViewStateImpl, Mutation, Effect>() {
        override fun createViewState() = ViewStateImpl()

        override suspend fun onViewStateInitialized() {
            super.onViewStateInitialized()
            initializedListener()
        }

        override suspend fun mutateViewState(mutation: Mutation): ViewStateImpl {
            return when (mutation) {
                OnSignInPressed -> currentViewState.copy(isSignedIn = true)
                OnSignOutPressed -> currentViewState.copy(isSignedIn = false)
            }
        }

        override suspend fun handleEffect(effect: Effect) {
            when (effect) {
                OnContinuePressed -> navigationEventListener()
            }
        }
    }

    private data class ViewStateImpl(
        val isSignedIn: Boolean = false
    ) : ViewState

    private sealed interface Mutation : MutationEvent {
        data object OnSignInPressed : Mutation
        data object OnSignOutPressed : Mutation
    }

    private sealed interface Effect : EffectEvent {
        data object OnContinuePressed : Effect
    }
}
