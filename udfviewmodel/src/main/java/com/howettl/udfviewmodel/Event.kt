package com.howettl.udfviewmodel

interface MutationEvent

interface EffectEvent

sealed interface NoOpMutation : MutationEvent

sealed interface NoOpEffect : EffectEvent
