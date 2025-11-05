package com.rvcoding.synch.api.websocket


import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.rvcoding.synch.api.dto.ws.ErrorDto
import com.rvcoding.synch.api.dto.ws.IncomingWebSocketMessage
import com.rvcoding.synch.api.dto.ws.IncomingWebSocketMessageType
import com.rvcoding.synch.api.dto.ws.OutgoingWebSocketMessage
import com.rvcoding.synch.api.dto.ws.OutgoingWebSocketMessageType
import com.rvcoding.synch.api.dto.ws.SendMessageDto
import com.rvcoding.synch.api.mappers.toChatMessageDto
import com.rvcoding.synch.domain.type.ChatId
import com.rvcoding.synch.domain.type.UserId
import com.rvcoding.synch.service.ChatMessageService
import com.rvcoding.synch.service.ChatService
import com.rvcoding.synch.service.JwtService
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler


private typealias SessionId = String

@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
    private val jwtService: JwtService
) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val connectionLock = ReentrantReadWriteLock()

    private val sessions = ConcurrentHashMap<SessionId, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<SessionId>>()
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<SessionId>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader = session
            .handshakeHeaders
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Session ${session.id} was closed due to missing Authorization header")
                session.close(CloseStatus.SERVER_ERROR.withReason("Authentication failed"))
                return
            }

        val userId = jwtService.getUserIdFromToken(authHeader)

        val userSession = UserSession(
            userId = userId,
            session = session
        )

        val chatIdsFromDb = chatService.findChatsByUser(userId).map { it.id }

        connectionLock.write {
            sessions[session.id] = userSession

            userToSessions.compute(userId) { _, existingSessions ->
                (existingSessions ?: mutableSetOf()).apply {
                    add(session.id)
                }
            }

            val chatIds = userChatIds.computeIfAbsent(userId) {
                ConcurrentHashMap.newKeySet<ChatId>().apply {
                    addAll(chatIdsFromDb)
                }
            }

            chatIds.forEach { chatId ->
                chatToSessions.compute(chatId) { _, sessions ->
                    (sessions ?: mutableSetOf()).apply {
                        add(session.id)
                    }
                }
            }
        }

        logger.info("Websocket connection established for user $userId")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Received message ${message.payload}")

        val userSession = connectionLock.read {
            sessions[session.id] ?: return
        }

        try {
            val webSocketMessage = objectMapper.readValue(
                /* content = */ message.payload,
                /* valueType = */ IncomingWebSocketMessage::class.java
            )
            when (webSocketMessage.type) {
                IncomingWebSocketMessageType.NEW_MESSAGE -> {
                    val dto = objectMapper.readValue(
                        /* content = */ webSocketMessage.payload,
                        /* valueType = */ SendMessageDto::class.java
                    )
                    handleSendMessage(
                        dto = dto,
                        senderId = userSession.userId
                    )
                }
                else -> {}
            }
        } catch (e: JsonMappingException) {
            logger.warn("Could not parse message ${message.payload}", e)
            sendError(
                session = userSession.session,
                error = ErrorDto(
                    code = "INVALID_JSON",
                    message = "Incoming JSON or UUID is invalid"
                )
            )
        }
    }

    private fun sendError(
        session: WebSocketSession,
        error: ErrorDto
    ) {
        val webSocketMessage = objectMapper.writeValueAsString(
            OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.ERROR,
                payload = objectMapper.writeValueAsString(error)
            )
        )

        try {
            session.sendMessage(TextMessage(webSocketMessage))
        } catch (e: Exception) {
            logger.warn("Couldn't send error message", e)
        }
    }

    private fun broadcastToChat(
        chatId: ChatId,
        message: OutgoingWebSocketMessage
    ) {
        val chatSessions = connectionLock.read {
            chatToSessions[chatId]?.toList() ?: emptyList()
        }

        chatSessions.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId]
            } ?: return@forEach

            sendToUser(
                userId = userSession.userId,
                message = message
            )
        }
    }

    private fun handleSendMessage(
        dto: SendMessageDto,
        senderId: UserId
    ) {
        val userChatIds = connectionLock.read {
            this@ChatWebSocketHandler.userChatIds[senderId]
        } ?: return

        if (dto.chatId !in userChatIds) {
            return
        }

        val savedMessage = chatMessageService.sendMessage(
            chatId = dto.chatId,
            senderId = senderId,
            content = dto.content,
            messageId = dto.messageId
        )

        broadcastToChat(
            chatId = dto.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.NEW_MESSAGE,
                payload = objectMapper.writeValueAsString(
                    savedMessage.toChatMessageDto()
                )
            )
        )
    }

    private fun sendToUser(
        userId: UserId,
        message: OutgoingWebSocketMessage
    ) {
        val userSessions = connectionLock.read {
            userToSessions[userId] ?: emptySet()
        }
        userSessions.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId] ?: return@forEach
            }
            if (userSession.session.isOpen) {
                try {
                    val messageJson = objectMapper.writeValueAsString(message)
                    userSession.session.sendMessage(TextMessage(messageJson))
                    logger.debug("Sent message to user {}: {}", userId, messageJson)
                } catch (e: Exception) {
                    logger.error("Error while sending message to $userId", e)
                }
            }
        }
    }

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession
    )
}