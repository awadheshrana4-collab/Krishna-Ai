package com.example.ai

import com.example.data.model.LanguageMode
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KrishnaAiRepository {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are a calm, compassionate, respectful spiritual companion on the Gita Saar app, sharing timeless reflections from the Shrimad Bhagavad Gita.

                    STRICT GUIDELINES:
                    1. Tone: Warm, serene, humble, grounded, and non-judgmental. Speak like a gentle guide sharing the Gita's wisdom — never preachy or harsh.
                    2. Structure every answer clearly:
                       - Begin with brief, empathetic acknowledgment of the user's situation (stress, anger, confusion, career, family, etc.).
                       - Quote 1 to 2 genuinely relevant Bhagavad Gita shlokas with exact Chapter and Verse number (e.g., Bhagavad Gita 2.47). Include the Sanskrit Devanagari verse only if you are 100% certain of the exact wording; otherwise provide the chapter/verse reference and an accurate summary without fabricating words.
                       - NEVER invent, fabricate, or hallucinate shlokas or verse numbers. If you are unsure of an exact verse, explicitly and honestly say so.
                       - Give a clear, simple explanation of the verse(s) in everyday language.
                       - Conclude with 2-3 gentle, practical steps the person can apply today.
                    3. Safety boundaries:
                       - NEVER give medical, psychiatric, diagnostic, or legal advice.
                       - If the user mentions a medical, mental health crisis, self-harm, or legal matter, kindly acknowledge their pain and encourage consulting a qualified doctor, counselor, helpline, or legal professional alongside spiritual comfort.
                    4. Respect the user's chosen language instruction included with their message.
                    """.trimIndent()
                )
            }
        )
    }

    fun streamGuidance(
        userProblem: String,
        languageMode: LanguageMode,
        recentHistory: List<Pair<String, String>>
    ): Flow<String> {
        val languageDirective = when (languageMode) {
            LanguageMode.HINDI -> "Please reply in simple, warm Hindi (Devanagari script), quoting the Gita shloka in Sanskrit with Chapter & Verse number."
            LanguageMode.SANSKRIT -> "Please reply with the relevant Sanskrit shloka(s) (Devanagari) and clear explanations in simple Hindi and English."
            LanguageMode.ENGLISH -> "Please reply in calm, clear English, quoting the relevant Gita shloka with Chapter & Verse number."
            LanguageMode.ALL -> "Please reply in the same language the user wrote in (Hindi or English), including the Sanskrit shloka (with Chapter & Verse number) and simple meaning."
        }

        val chatHistory: List<Content> = recentHistory.takeLast(8).map { (role, msg) ->
            content(role = role) { text(msg) }
        }

        val chat = model.startChat(history = chatHistory)
        val prompt = "$languageDirective\n\nUser's question: $userProblem"
        return chat.sendMessageStream(prompt).map { chunk -> chunk.text.orEmpty() }
    }
}
