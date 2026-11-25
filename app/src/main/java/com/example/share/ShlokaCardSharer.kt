package com.example.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.Shloka
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ShlokaCardSharer {

    suspend fun renderShlokaCardBitmap(context: Context, shloka: Shloka): Bitmap =
        withContext(Dispatchers.Default) {
            val width = 1080
            val height = 1440
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 1. Background parchment base + generated devotional card artwork
            canvas.drawColor(Color.parseColor("#FBF5EA"))
            val bgRaw = BitmapFactory.decodeResource(context.resources, R.drawable.img_share_card_bg)
            if (bgRaw != null) {
                val src = Rect(
                    (bgRaw.width * 0.08f).toInt(),
                    (bgRaw.height * 0.08f).toInt(),
                    (bgRaw.width * 0.92f).toInt(),
                    (bgRaw.height * 0.92f).toInt()
                )
                val dst = Rect(0, 0, width, height)
                canvas.drawBitmap(bgRaw, src, dst, Paint(Paint.FILTER_BITMAP_FLAG))
                bgRaw.recycle()
            }

            // 2. Soft cream veil in center for guaranteed crisp legibility
            val cardRect = RectF(96f, 110f, width - 96f, height - 110f)
            val veilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(232, 255, 252, 245)
            }
            canvas.drawRoundRect(cardRect, 36f, 36f, veilPaint)

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = Color.parseColor("#C68B1D")
            }
            canvas.drawRoundRect(cardRect, 36f, 36f, borderPaint)

            val devanagariTypeface = ResourcesCompat.getFont(context, R.font.noto_serif_devanagari)
                ?: Typeface.SERIF
            val englishSerifTypeface = ResourcesCompat.getFont(context, R.font.lora)
                ?: Typeface.SERIF
            val sansTypeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans)
                ?: Typeface.SANS_SERIF

            val innerWidth = width - 280
            val leftMargin = 140f
            var y = 175f

            // 3. Top Header Badge
            val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#6E1B2A")
                textSize = 30f
                typeface = Typeface.create(devanagariTypeface, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("श्रीमद्भगवद्गीता", width / 2f, y, headerPaint)
            y += 46f

            val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#B84B14")
                textSize = 26f
                typeface = Typeface.create(sansTypeface, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.08f
            }
            canvas.drawText(
                "CHAPTER ${shloka.chapter} • VERSE ${shloka.verse}",
                width / 2f,
                y,
                refPaint
            )
            y += 38f

            // Ornamental divider line
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D8A64A")
                strokeWidth = 3f
            }
            canvas.drawLine(width / 2f - 140f, y, width / 2f + 140f, y, dividerPaint)
            y += 54f

            // 4. Sanskrit Verse (large, devotional deep maroon)
            val sanskritPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#4E121E")
                textSize = 44f
                typeface = Typeface.create(devanagariTypeface, Typeface.BOLD)
            }
            val sanskritLayout = buildCenteredLayout(shloka.sanskrit, sanskritPaint, innerWidth, 1.32f)
            canvas.save()
            canvas.translate(leftMargin, y)
            sanskritLayout.draw(canvas)
            canvas.restore()
            y += sanskritLayout.height + 42f

            // 5. Hindi Meaning
            val hindiPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2B1B17")
                textSize = 31f
                typeface = devanagariTypeface
            }
            val hindiLayout = buildCenteredLayout(shloka.hindi, hindiPaint, innerWidth, 1.32f)
            canvas.save()
            canvas.translate(leftMargin, y)
            hindiLayout.draw(canvas)
            canvas.restore()
            y += hindiLayout.height + 36f

            // Subtle dot divider
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#C68B1D")
            }
            canvas.drawCircle(width / 2f, y, 5f, dotPaint)
            canvas.drawCircle(width / 2f - 22f, y, 3.5f, dotPaint)
            canvas.drawCircle(width / 2f + 22f, y, 3.5f, dotPaint)
            y += 38f

            // 6. English Meaning (restrained so it stays cleanly inside frame)
            val maxEnglishHeight = (height - 220f - y).coerceAtLeast(140f)
            var engSize = 29f
            var engLayout: StaticLayout
            do {
                val engPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#543E35")
                    textSize = engSize
                    typeface = englishSerifTypeface
                }
                engLayout = buildCenteredLayout("\"${shloka.english}\"", engPaint, innerWidth, 1.28f)
                engSize -= 2f
            } while (engLayout.height > maxEnglishHeight && engSize >= 21f)

            canvas.save()
            canvas.translate(leftMargin, y)
            engLayout.draw(canvas)
            canvas.restore()

            // 7. Footer branding
            val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#8C5D4A")
                textSize = 24f
                typeface = Typeface.create(sansTypeface, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.12f
            }
            canvas.drawText("GITA SAAR • DAILY WISDOM", width / 2f, height - 154f, footerPaint)

            bitmap
        }

    private fun buildCenteredLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        lineMult: Float
    ): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, lineMult)
            .setIncludePad(true)
            .build()
    }

    suspend fun shareShlokaImage(
        context: Context,
        shloka: Shloka,
        preferWhatsApp: Boolean
    ) {
        val bitmap = renderShlokaCardBitmap(context, shloka)
        val file = withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
            val out = File(dir, "gita_saar_${shloka.chapter}_${shloka.verse}.png")
            FileOutputStream(out).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream)
            }
            bitmap.recycle()
            out
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val caption = "${shloka.referenceEnglish} (${shloka.referenceSanskrit})\n\n" +
            "${shloka.sanskrit}\n\n${shloka.hindi}\n\n${shloka.english}\n— Gita Saar"

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (preferWhatsApp) {
                setPackage("com.whatsapp")
            }
        }

        val chooser = if (preferWhatsApp && sendIntent.resolveActivity(context.packageManager) != null) {
            sendIntent
        } else {
            sendIntent.setPackage(null)
            Intent.createChooser(sendIntent, "Share Gita Saar Shloka")
        }
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
