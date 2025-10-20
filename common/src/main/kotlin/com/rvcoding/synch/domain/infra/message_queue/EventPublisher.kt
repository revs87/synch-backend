package com.rvcoding.synch.domain.infra.message_queue

import com.rvcoding.synch.domain.events.SynchEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val rabbitTemplate: RabbitTemplate
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun <T: SynchEvent> publish(event: T) {
        try {
            rabbitTemplate.convertAndSend(
                /* exchange = */ event.exchange,
                /* routingKey = */ event.eventKey,
                /* object = */ event
            )
            logger.info("Successfully published ${event.eventKey} event")
        } catch (e: Exception) {
            logger.error("Failed to publish ${event.eventKey} event", e)
        }
    }
}