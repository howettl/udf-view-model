package io.github.howettl.udfviewmodel

interface MutationEvent

interface EffectEvent

sealed interface NoOpMutation : MutationEvent

sealed interface NoOpEffect : EffectEvent
