@file:Suppress("DEPRECATION")

package com.rvcoding.synch.infra.message_queue

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rvcoding.synch.domain.events.SynchEvent
import com.rvcoding.synch.domain.events.user.UserEventConstants
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class RabbitMqConfig {

    @Bean
    fun messageConverter(): Jackson2JsonMessageConverter {
        val objectMapper = ObjectMapper().apply {
            // Introduces the ability to serialize all Kotlin base classes
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            findAndRegisterModules()

            // Introduces the ability to serialize all classes that implement the interface SynchEvent
            val polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(SynchEvent::class.java)
                .allowIfSubType("java.util.") // Allow Java lists
                .allowIfSubType("kotlin.collections.") // Allow Kotlin collections
                .build()

            activateDefaultTyping(
                polymorphicTypeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
            )
        }

        val typeMapper = DefaultJackson2JavaTypeMapper().apply {
            setTrustedPackages("*")
            typePrecedence = Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID
        }

        return Jackson2JsonMessageConverter(objectMapper).apply {
            javaTypeMapper = typeMapper
        }
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: Jackson2JsonMessageConverter
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    @Bean
    fun userExchange() = TopicExchange(
        /* name = */ UserEventConstants.USER_EXCHANGE,
        /* durable = */ true,
        /* autoDelete = */ false
    )

    @Bean
    fun notificationUserEventsQueue() = Queue(
        /* name = */ MessageQueues.NOTIFICATION_USER_EVENTS,
        /* durable = */ true
    )

    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        transactionManager: PlatformTransactionManager,
        messageConverter: Jackson2JsonMessageConverter
    ): SimpleRabbitListenerContainerFactory {
        return SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setTransactionManager(transactionManager)
            setChannelTransacted(true)
            setMessageConverter(messageConverter)
        }
    }

    @Bean
    fun notificationUserEventsBinding(
        notificationUserEventsQueue: Queue,
        userExchange: TopicExchange
    ): Binding {
        return BindingBuilder
            .bind(notificationUserEventsQueue)
            .to(userExchange)
            .with("user.*")
    }
}
