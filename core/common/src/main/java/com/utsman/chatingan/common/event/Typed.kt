package com.utsman.chatingan.common.event

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

typealias FlowEvent<T> = StateFlow<StateEvent<T>>
typealias MutableFlowEvent<T> = MutableStateFlow<StateEvent<T>>