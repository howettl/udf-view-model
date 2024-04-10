package io.github.howettl.udfviewmodel

/**
 * A view state to be emitted by the view model and rendered by a view.
 * It is strongly recommended that implementations of this interface be data classes, which enables duplicate
 * view state conflation, and copying the current state during mutation events.
 */
interface ViewState
