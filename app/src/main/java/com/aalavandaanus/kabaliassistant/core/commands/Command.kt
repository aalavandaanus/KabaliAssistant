package com.aalavandaanus.kabaliassistant.core.commands

/**
 * A language-neutral description of what Kabali believes the user requested.
 *
 * This model describes interpretation only. It does not authorize execution;
 * the safety/confirmation layer must validate the request before a handler runs.
 */
data class CommandInterpretation(
    /** Exact transcript supplied by speech recognition; keep in memory by default. */
    val originalTranscript: String,
    /** Normalized text used for matching, kept separate from the original transcript. */
    val normalizedText: String,
    val intent: CommandIntent,
    /** Intent-specific values, such as "time" -> "7:00 AM" or "contactName" -> "Ravi". */
    val parameters: Map<String, String> = emptyMap(),
    val confidence: CommandConfidence,
    val language: CommandLanguage = CommandLanguage.UNKNOWN,
    /** Names of required details that are not known yet, such as "time" or "contact". */
    val missingParameters: Set<String> = emptySet(),
    val conversationRelation: ConversationRelation = ConversationRelation.NEW_REQUEST
)

enum class CommandIntent {
    CALL_CONTACT,
    LIST_ALARMS,
    LIST_UPCOMING_SCHEDULES,
    LIST_UPCOMING_BIRTHDAYS,
    CREATE_ALARM,
    CREATE_REMINDER,
    UPDATE_ALARM,
    UPDATE_REMINDER,
    DELETE_ALARM,
    DELETE_REMINDER,
    CONVERSATION,
    UNKNOWN
}

enum class CommandConfidence {
    HIGH,
    MEDIUM,
    LOW
}

enum class CommandLanguage {
    ENGLISH,
    TAMIL,
    TANGLISH,
    MIXED,
    UNKNOWN
}

/**
 * Describes how this utterance relates to the current interaction.
 * The conversation state manager decides whether a pending request is replaced.
 */
enum class ConversationRelation {
    NEW_REQUEST,
    FOLLOW_UP,
    CORRECTION,
    CANCELLATION,
    TOPIC_CHANGE
}
