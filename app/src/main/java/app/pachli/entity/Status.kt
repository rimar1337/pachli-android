/* Copyright 2017 Andrew Dawson
 *
 * This file is a part of Pachli.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Pachli is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Pachli; if not,
 * see <http://www.gnu.org/licenses>.
 */

package app.pachli.entity

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import app.pachli.R
import app.pachli.util.parseAsMastodonHtml
import com.google.gson.annotations.SerializedName
import java.util.Date

data class Status(
    val id: String,
    val url: String?, // not present if it's reblog
    val account: TimelineAccount,
    @SerializedName("in_reply_to_id") val inReplyToId: String?,
    @SerializedName("in_reply_to_account_id") val inReplyToAccountId: String?,
    val reblog: Status?,
    val content: String,
    @SerializedName("created_at") val createdAt: Date,
    @SerializedName("edited_at") val editedAt: Date?,
    val emojis: List<Emoji>,
    @SerializedName("reblogs_count") val reblogsCount: Int,
    @SerializedName("favourites_count") val favouritesCount: Int,
    @SerializedName("replies_count") val repliesCount: Int,
    val reblogged: Boolean,
    val favourited: Boolean,
    val bookmarked: Boolean,
    val sensitive: Boolean,
    @SerializedName("spoiler_text") val spoilerText: String,
    val visibility: Visibility,
    @SerializedName("media_attachments") val attachments: List<Attachment>,
    val mentions: List<Mention>,
    val tags: List<HashTag>?,
    val application: Application?,
    val pinned: Boolean?,
    val muted: Boolean?,
    val poll: Poll?,
    val card: Card?,
    val language: String?,
    val filtered: List<FilterResult>?,
) {

    val actionableId: String
        get() = reblog?.id ?: id

    val actionableStatus: Status
        get() = reblog ?: this

    enum class Visibility(val num: Int) {
        UNKNOWN(0),

        @SerializedName("public")
        PUBLIC(1),

        @SerializedName("unlisted")
        UNLISTED(2),

        @SerializedName("private")
        PRIVATE(3),

        @SerializedName("direct")
        DIRECT(4),
        ;

        fun serverString(): String {
            return when (this) {
                PUBLIC -> "public"
                UNLISTED -> "unlisted"
                PRIVATE -> "private"
                DIRECT -> "direct"
                UNKNOWN -> "unknown"
            }
        }

        companion object {
            @JvmStatic
            fun byNum(num: Int): Visibility {
                return when (num) {
                    4 -> DIRECT
                    3 -> PRIVATE
                    2 -> UNLISTED
                    1 -> PUBLIC
                    0 -> UNKNOWN
                    else -> UNKNOWN
                }
            }

            @JvmStatic
            fun byString(s: String): Visibility {
                return when (s) {
                    "public" -> PUBLIC
                    "unlisted" -> UNLISTED
                    "private" -> PRIVATE
                    "direct" -> DIRECT
                    "unknown" -> UNKNOWN
                    else -> UNKNOWN
                }
            }
        }
    }

    fun rebloggingAllowed(): Boolean {
        return (visibility != Visibility.DIRECT && visibility != Visibility.UNKNOWN)
    }

    fun isPinned(): Boolean {
        return pinned ?: false
    }

    fun toDeletedStatus(): DeletedStatus {
        return DeletedStatus(
            text = getEditableText(),
            inReplyToId = inReplyToId,
            spoilerText = spoilerText,
            visibility = visibility,
            sensitive = sensitive,
            attachments = attachments,
            poll = poll,
            createdAt = createdAt,
            language = language,
        )
    }

    private fun getEditableText(): String {
        val contentSpanned = content.parseAsMastodonHtml()
        val builder = SpannableStringBuilder(content.parseAsMastodonHtml())
        for (span in contentSpanned.getSpans(0, content.length, URLSpan::class.java)) {
            val url = span.url
            for ((_, url1, username) in mentions) {
                if (url == url1) {
                    val start = builder.getSpanStart(span)
                    val end = builder.getSpanEnd(span)
                    if (start >= 0 && end >= 0) {
                        builder.replace(start, end, "@$username")
                    }
                    break
                }
            }
        }
        return builder.toString()
    }

    data class Mention(
        val id: String,
        val url: String,
        @SerializedName("acct") val username: String,
        @SerializedName("username") val localUsername: String,
    )

    data class Application(
        val name: String,
        val website: String?,
    )

    companion object {
        const val MAX_MEDIA_ATTACHMENTS = 4
        const val MAX_POLL_OPTIONS = 4
    }
}

/**
 * @return A description for this visibility, or "" if it's null or [Status.Visibility.UNKNOWN].
 */
fun Status.Visibility?.description(context: Context): CharSequence {
    this ?: return ""

    val resource: Int = when (this) {
        Status.Visibility.PUBLIC -> R.string.description_visibility_public
        Status.Visibility.UNLISTED -> R.string.description_visibility_unlisted
        Status.Visibility.PRIVATE -> R.string.description_visibility_private
        Status.Visibility.DIRECT -> R.string.description_visibility_direct
        Status.Visibility.UNKNOWN -> return ""
    }
    return context.getString(resource)
}

/**
 * @return An icon for this visibility scaled and coloured to match the text on [textView].
 *     Returns null if visibility is [Status.Visibility.UNKNOWN].
 */
fun Status.Visibility?.icon(textView: TextView): Drawable? {
    this ?: return null

    val resource: Int = when (this) {
        Status.Visibility.PUBLIC -> R.drawable.ic_public_24dp
        Status.Visibility.UNLISTED -> R.drawable.ic_lock_open_24dp
        Status.Visibility.PRIVATE -> R.drawable.ic_lock_outline_24dp
        Status.Visibility.DIRECT -> R.drawable.ic_email_24dp
        Status.Visibility.UNKNOWN -> return null
    }
    val visibilityDrawable = AppCompatResources.getDrawable(
        textView.context,
        resource,
    ) ?: return null
    val size = textView.textSize.toInt()
    visibilityDrawable.setBounds(0, 0, size, size)
    visibilityDrawable.setTint(textView.currentTextColor)
    return visibilityDrawable
}
