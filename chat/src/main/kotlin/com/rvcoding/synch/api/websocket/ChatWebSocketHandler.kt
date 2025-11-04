package com.rvcoding.synch.api.websocket


import com.fasterxml.jackson.databind.ObjectMapper
import com.rvcoding.synch.domain.type.ChatId
import com.rvcoding.synch.domain.type.UserId
import com.rvcoding.synch.service.ChatMessageService
import com.rvcoding.synch.service.ChatService
import com.rvcoding.synch.service.JwtService
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
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

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession
    )
}