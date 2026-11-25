package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.model.ChapterInfo
import com.example.data.model.Shloka
import com.example.data.model.YaadPuzzle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar

private const val TAG = "GitaAssetRepository"

class GitaAssetRepository(private val context: Context) {

    private val cacheMutex = Mutex()
    private var cachedChapters: List<ChapterInfo>? = null
    private val chapterShlokasCache = mutableMapOf<Int, List<Shloka>>()

    // Iconic verses from Chapter 1 & 2 featured in Shloka of the Day & Yaad Karo
    private val curatedFeaturedIds = listOf(
        "2.47", "2.48", "2.14", "2.20", "2.22", "2.23",
        "2.27", "2.38", "2.40", "2.50", "2.55", "2.56",
        "2.62", "2.63", "2.64", "2.65", "2.70", "2.71",
        "1.1", "2.7", "2.13", "2.15"
    )

    suspend fun getChapters(): List<ChapterInfo> = cacheMutex.withLock {
        cachedChapters?.let { return it }
        val loaded = withContext(Dispatchers.IO) {
            try {
                val jsonText = context.assets.open("gita/chapters.json")
                    .bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                val arr = root.getJSONArray("chapters")
                buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val fileName = obj.getString("jsonFile")
                        val populated = obj.optBoolean("isPopulated", false) ||
                            hasShlokasInAsset(fileName)
                        add(
                            ChapterInfo(
                                chapterNumber = obj.getInt("chapterNumber"),
                                nameSanskrit = obj.getString("nameSanskrit"),
                                nameTransliteration = obj.getString("nameTransliteration"),
                                nameHindi = obj.getString("nameHindi"),
                                nameEnglish = obj.getString("nameEnglish"),
                                versesCount = obj.getInt("versesCount"),
                                summaryHindi = obj.getString("summaryHindi"),
                                summaryEnglish = obj.getString("summaryEnglish"),
                                jsonFile = fileName,
                                isPopulated = populated
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load chapters.json", e)
                emptyList()
            }
        }
        cachedChapters = loaded
        loaded
    }

    private fun hasShlokasInAsset(fileName: String): Boolean {
        return try {
            val text = context.assets.open("gita/$fileName").bufferedReader().use { it.readText() }
            val arr = JSONObject(text).optJSONArray("shlokas")
            arr != null && arr.length() > 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getChapterShlokas(chapterNumber: Int): List<Shloka> = cacheMutex.withLock {
        chapterShlokasCache[chapterNumber]?.let { return it }
        val loaded = withContext(Dispatchers.IO) {
            val fileName = "gita/chapter_%02d.json".format(chapterNumber)
            try {
                val jsonText = context.assets.open(fileName)
                    .bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                val arr = root.optJSONArray("shlokas") ?: return@withContext emptyList()
                buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        add(
                            Shloka(
                                chapter = obj.getInt("chapter"),
                                verse = obj.getInt("verse"),
                                speaker = obj.optString("speaker", ""),
                                sanskrit = obj.getString("sanskrit"),
                                transliteration = obj.getString("transliteration"),
                                hindi = obj.getString("hindi"),
                                english = obj.getString("english")
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
        chapterShlokasCache[chapterNumber] = loaded
        loaded
    }

    suspend fun getAllAvailableShlokas(): List<Shloka> {
        val chapters = getChapters().filter { it.isPopulated }
        return buildList {
            for (chapter in chapters) {
                addAll(getChapterShlokas(chapter.chapterNumber))
            }
        }
    }

    suspend fun findShlokaById(shlokaId: String): Shloka? {
        val parts = shlokaId.split(".")
        if (parts.size != 2) return null
        val ch = parts[0].toIntOrNull() ?: return null
        val vs = parts[1].toIntOrNull() ?: return null
        return getChapterShlokas(ch).firstOrNull { it.verse == vs }
    }

    suspend fun getShlokaOfTheDay(): Shloka? {
        val all = getAllAvailableShlokas()
        if (all.isEmpty()) return null
        val byId = all.associateBy { it.id }
        val featured = curatedFeaturedIds.mapNotNull { byId[it] }.ifEmpty { all }
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val index = ((year * 31) + dayOfYear) % featured.size
        return featured[index]
    }

    suspend fun searchShlokas(query: String): List<Shloka> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return emptyList()
        val all = getAllAvailableShlokas()
        return all.filter { shloka ->
            shloka.id == trimmed ||
                "${shloka.chapter}:${shloka.verse}" == trimmed ||
                shloka.sanskrit.lowercase().contains(trimmed) ||
                shloka.transliteration.lowercase().contains(trimmed) ||
                shloka.hindi.lowercase().contains(trimmed) ||
                shloka.english.lowercase().contains(trimmed)
        }
    }

    suspend fun buildYaadPuzzles(): List<YaadPuzzle> {
        val all = getAllAvailableShlokas()
        val byId = all.associateBy { it.id }
        val pool = curatedFeaturedIds.mapNotNull { byId[it] }.ifEmpty { all.take(15) }
        return pool.mapNotNull { shloka ->
            val rawTokens = shloka.sanskrit
                .replace("\n", " ")
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }

            if (rawTokens.size < 4) return@mapNotNull null

            val candidateIndices = rawTokens.indices.filter { idx ->
                val clean = rawTokens[idx].replace("।", "").replace("॥", "")
                clean.length >= 3
            }

            if (candidateIndices.size < 2) return@mapNotNull null

            val blankCount = when {
                candidateIndices.size >= 8 -> 3
                else -> 2
            }

            val step = (candidateIndices.size / blankCount).coerceAtLeast(1)
            val chosen = buildList {
                var cursor = 0
                while (size < blankCount && cursor < candidateIndices.size) {
                    add(candidateIndices[cursor])
                    cursor += step
                }
            }.sorted()

            val correctWords = chosen.map { rawTokens[it] }
            // Add 2 gentle distractors from nearby words in the same chapter
            val distractorPool = pool
                .filter { it.id != shloka.id }
                .flatMap { it.sanskrit.replace("\n", " ").split(Regex("\\s+")) }
                .filter { token ->
                    val cleaned = token.replace("।", "").replace("॥", "")
                    cleaned.length in 4..10 && token !in correctWords
                }
                .distinct()

            val seed = shloka.chapter * 100 + shloka.verse
            val distractors = if (distractorPool.size >= 2) {
                listOf(
                    distractorPool[seed % distractorPool.size],
                    distractorPool[(seed + 7) % distractorPool.size]
                ).distinct()
            } else {
                emptyList()
            }

            val bank = (correctWords + distractors).shuffled(java.util.Random(seed.toLong()))

            YaadPuzzle(
                shloka = shloka,
                tokens = rawTokens,
                blankIndices = chosen,
                wordBank = bank
            )
        }
    }
}
