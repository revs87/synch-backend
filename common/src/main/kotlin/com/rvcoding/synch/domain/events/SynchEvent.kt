package com.rvcoding.synch.domain.events

import java.time.Instant

interface SynchEvent {
    val eventId: String
    val eventKey: String
    val occurredAt: Instant
    val exchange: String
}