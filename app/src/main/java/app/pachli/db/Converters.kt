/* Copyright 2018 Conny Duck
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

package app.pachli.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import app.pachli.TabData
import app.pachli.components.conversation.ConversationAccountEntity
import app.pachli.createTabDataFromId
import app.pachli.entity.Attachment
import app.pachli.entity.Emoji
import app.pachli.entity.FilterResult
import app.pachli.entity.HashTag
import app.pachli.entity.NewPoll
import app.pachli.entity.Poll
import app.pachli.entity.Status
import app.pachli.entity.TranslatedAttachment
import app.pachli.entity.TranslatedPoll
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@ProvidedTypeConverter
@Singleton
class Converters @Inject constructor(
    private val gson: Gson,
) {

    @TypeConverter
    fun jsonToEmojiList(emojiListJson: String?): List<Emoji>? {
        return gson.fromJson(emojiListJson, object : TypeToken<List<Emoji>>() {}.type)
    }

    @TypeConverter
    fun emojiListToJson(emojiList: List<Emoji>?): String {
        return gson.toJson(emojiList)
    }

    @TypeConverter
    fun visibilityToInt(visibility: Status.Visibility?): Int {
        return visibility?.num ?: Status.Visibility.UNKNOWN.num
    }

    @TypeConverter
    fun intToVisibility(visibility: Int): Status.Visibility {
        return Status.Visibility.byNum(visibility)
    }

    @TypeConverter
    fun stringToTabData(str: String?): List<TabData>? {
        return str?.split(";")
            ?.map {
                val data = it.split(":")
                createTabDataFromId(data[0], data.drop(1).map { s -> URLDecoder.decode(s, "UTF-8") })
            }
    }

    @TypeConverter
    fun tabDataToString(tabData: List<TabData>?): String? {
        // List name may include ":"
        return tabData?.joinToString(";") { it.id + ":" + it.arguments.joinToString(":") { s -> URLEncoder.encode(s, "UTF-8") } }
    }

    @TypeConverter
    fun accountToJson(account: ConversationAccountEntity?): String {
        return gson.toJson(account)
    }

    @TypeConverter
    fun jsonToAccount(accountJson: String?): ConversationAccountEntity? {
        return gson.fromJson(accountJson, ConversationAccountEntity::class.java)
    }

    @TypeConverter
    fun accountListToJson(accountList: List<ConversationAccountEntity>?): String {
        return gson.toJson(accountList)
    }

    @TypeConverter
    fun jsonToAccountList(accountListJson: String?): List<ConversationAccountEntity>? {
        return gson.fromJson(accountListJson, object : TypeToken<List<ConversationAccountEntity>>() {}.type)
    }

    @TypeConverter
    fun attachmentListToJson(attachmentList: List<Attachment>?): String {
        return gson.toJson(attachmentList)
    }

    @TypeConverter
    fun jsonToAttachmentList(attachmentListJson: String?): List<Attachment>? {
        return gson.fromJson(attachmentListJson, object : TypeToken<List<Attachment>>() {}.type)
    }

    @TypeConverter
    fun mentionListToJson(mentionArray: List<Status.Mention>?): String? {
        return gson.toJson(mentionArray)
    }

    @TypeConverter
    fun jsonToMentionArray(mentionListJson: String?): List<Status.Mention>? {
        return gson.fromJson(mentionListJson, object : TypeToken<List<Status.Mention>>() {}.type)
    }

    @TypeConverter
    fun tagListToJson(tagArray: List<HashTag>?): String? {
        return gson.toJson(tagArray)
    }

    @TypeConverter
    fun jsonToTagArray(tagListJson: String?): List<HashTag>? {
        return gson.fromJson(tagListJson, object : TypeToken<List<HashTag>>() {}.type)
    }

    @TypeConverter
    fun dateToLong(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun longToDate(date: Long?): Date? {
        return date?.let { Date(it) }
    }

    @TypeConverter
    fun pollToJson(poll: Poll?): String? {
        return gson.toJson(poll)
    }

    @TypeConverter
    fun jsonToPoll(pollJson: String?): Poll? {
        return gson.fromJson(pollJson, Poll::class.java)
    }

    @TypeConverter
    fun newPollToJson(newPoll: NewPoll?): String? {
        return gson.toJson(newPoll)
    }

    @TypeConverter
    fun jsonToNewPoll(newPollJson: String?): NewPoll? {
        return gson.fromJson(newPollJson, NewPoll::class.java)
    }

    @TypeConverter
    fun draftAttachmentListToJson(draftAttachments: List<DraftAttachment>?): String? {
        return gson.toJson(draftAttachments)
    }

    @TypeConverter
    fun jsonToDraftAttachmentList(draftAttachmentListJson: String?): List<DraftAttachment>? {
        return gson.fromJson(draftAttachmentListJson, object : TypeToken<List<DraftAttachment>>() {}.type)
    }

    @TypeConverter
    fun filterResultListToJson(filterResults: List<FilterResult>?): String? {
        return gson.toJson(filterResults)
    }

    @TypeConverter
    fun jsonToFilterResultList(filterResultListJson: String?): List<FilterResult>? {
        return gson.fromJson(filterResultListJson, object : TypeToken<List<FilterResult>>() {}.type)
    }

    @TypeConverter
    fun translatedPolltoJson(translatedPoll: TranslatedPoll?): String? {
        return gson.toJson(translatedPoll)
    }

    @TypeConverter
    fun jsonToTranslatedPoll(translatedPollJson: String?): TranslatedPoll? {
        return gson.fromJson(translatedPollJson, TranslatedPoll::class.java)
    }

    @TypeConverter
    fun translatedAttachmentToJson(translatedAttachment: List<TranslatedAttachment>?): String {
        return gson.toJson(translatedAttachment)
    }

    @TypeConverter
    fun jsonToTranslatedAttachment(translatedAttachmentJson: String): List<TranslatedAttachment>? {
        return gson.fromJson(translatedAttachmentJson, object : TypeToken<List<TranslatedAttachment>>() {}.type)
    }
}
