/* Copyright 2021 Tusky Contributors
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

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Upsert

@Dao
abstract class TimelineDao {

    @Insert(onConflict = REPLACE)
    abstract suspend fun insertAccount(timelineAccountEntity: TimelineAccountEntity): Long

    @Insert(onConflict = REPLACE)
    abstract suspend fun insertStatus(timelineStatusEntity: TimelineStatusEntity): Long

    @Query(
        """
SELECT s.serverId, s.url, s.timelineUserId,
s.authorServerId, s.inReplyToId, s.inReplyToAccountId, s.createdAt, s.editedAt,
s.emojis, s.reblogsCount, s.favouritesCount, s.repliesCount, s.reblogged, s.favourited, s.bookmarked, s.sensitive,
s.spoilerText, s.visibility, s.mentions, s.tags, s.application, s.reblogServerId,s.reblogAccountId,
s.content, s.attachments, s.poll, s.card, s.muted, s.pinned, s.language, s.filtered,
a.serverId as 'a_serverId', a.timelineUserId as 'a_timelineUserId',
a.localUsername as 'a_localUsername', a.username as 'a_username',
a.displayName as 'a_displayName', a.url as 'a_url', a.avatar as 'a_avatar',
a.emojis as 'a_emojis', a.bot as 'a_bot',
rb.serverId as 'rb_serverId', rb.timelineUserId 'rb_timelineUserId',
rb.localUsername as 'rb_localUsername', rb.username as 'rb_username',
rb.displayName as 'rb_displayName', rb.url as 'rb_url', rb.avatar as 'rb_avatar',
rb.emojis as 'rb_emojis', rb.bot as 'rb_bot',
svd.serverId as 'svd_serverId', svd.timelineUserId as 'svd_timelineUserId',
svd.expanded as 'svd_expanded', svd.contentShowing as 'svd_contentShowing',
svd.contentCollapsed as 'svd_contentCollapsed', svd.translationState as 'svd_translationState',
t.serverId as 't_serverId', t.timelineUserId as 't_timelineUserId', t.content as 't_content',
t.spoilerText as 't_spoilerText', t.poll as 't_poll', t.attachments as 't_attachments',
t.provider as 't_provider'
FROM TimelineStatusEntity s
LEFT JOIN TimelineAccountEntity a ON (s.timelineUserId = a.timelineUserId AND s.authorServerId = a.serverId)
LEFT JOIN TimelineAccountEntity rb ON (s.timelineUserId = rb.timelineUserId AND s.reblogAccountId = rb.serverId)
LEFT JOIN StatusViewDataEntity svd ON (s.timelineUserId = svd.timelineUserId AND (s.serverId = svd.serverId OR s.reblogServerId = svd.serverId))
LEFT JOIN TranslatedStatusEntity t ON (s.timelineUserId = t.timelineUserId AND (s.serverId = t.serverId OR s.reblogServerId = t.serverId))
WHERE s.timelineUserId = :account
ORDER BY LENGTH(s.serverId) DESC, s.serverId DESC""",
    )
    abstract fun getStatuses(account: Long): PagingSource<Int, TimelineStatusWithAccount>

    /**
     * All statuses for [account] in timeline ID. Used to find the correct initialKey to restore
     * the user's reading position.
     *
     * @see [app.pachli.components.timeline.viewmodel.CachedTimelineViewModel.statuses]
     */
    @Query(
        """
SELECT serverId
  FROM TimelineStatusEntity
 WHERE timelineUserId = :account
 ORDER BY LENGTH(serverId) DESC, serverId DESC""",
    )
    abstract fun getStatusRowNumber(account: Long): List<String>

    @Query(
        """
SELECT s.serverId, s.url, s.timelineUserId,
s.authorServerId, s.inReplyToId, s.inReplyToAccountId, s.createdAt, s.editedAt,
s.emojis, s.reblogsCount, s.favouritesCount, s.repliesCount, s.reblogged, s.favourited, s.bookmarked, s.sensitive,
s.spoilerText, s.visibility, s.mentions, s.tags, s.application, s.reblogServerId,s.reblogAccountId,
s.content, s.attachments, s.poll, s.card, s.muted, s.pinned, s.language, s.filtered,
a.serverId as 'a_serverId', a.timelineUserId as 'a_timelineUserId',
a.localUsername as 'a_localUsername', a.username as 'a_username',
a.displayName as 'a_displayName', a.url as 'a_url', a.avatar as 'a_avatar',
a.emojis as 'a_emojis', a.bot as 'a_bot',
rb.serverId as 'rb_serverId', rb.timelineUserId 'rb_timelineUserId',
rb.localUsername as 'rb_localUsername', rb.username as 'rb_username',
rb.displayName as 'rb_displayName', rb.url as 'rb_url', rb.avatar as 'rb_avatar',
rb.emojis as 'rb_emojis', rb.bot as 'rb_bot',
svd.serverId as 'svd_serverId', svd.timelineUserId as 'svd_timelineUserId',
svd.expanded as 'svd_expanded', svd.contentShowing as 'svd_contentShowing',
svd.contentCollapsed as 'svd_contentCollapsed', svd.translationState as 'svd_translationState',
t.serverId as 't_serverId', t.timelineUserId as 't_timelineUserId', t.content as 't_content',
t.spoilerText as 't_spoilerText', t.poll as 't_poll', t.attachments as 't_attachments',
t.provider as 't_provider'
FROM TimelineStatusEntity s
LEFT JOIN TimelineAccountEntity a ON (s.timelineUserId = a.timelineUserId AND s.authorServerId = a.serverId)
LEFT JOIN TimelineAccountEntity rb ON (s.timelineUserId = rb.timelineUserId AND s.reblogAccountId = rb.serverId)
LEFT JOIN StatusViewDataEntity svd ON (s.timelineUserId = svd.timelineUserId AND (s.serverId = svd.serverId OR s.reblogServerId = svd.serverId))
LEFT JOIN TranslatedStatusEntity t ON (s.timelineUserId = t.timelineUserId AND (s.serverId = t.serverId OR s.reblogServerId = t.serverId))
WHERE (s.serverId = :statusId OR s.reblogServerId = :statusId)
AND s.authorServerId IS NOT NULL""",
    )
    abstract suspend fun getStatus(statusId: String): TimelineStatusWithAccount?

    @Query(
        """DELETE FROM TimelineStatusEntity WHERE timelineUserId = :accountId AND
        (LENGTH(serverId) < LENGTH(:maxId) OR LENGTH(serverId) == LENGTH(:maxId) AND serverId <= :maxId)
AND
(LENGTH(serverId) > LENGTH(:minId) OR LENGTH(serverId) == LENGTH(:minId) AND serverId >= :minId)
    """,
    )
    abstract suspend fun deleteRange(accountId: Long, minId: String, maxId: String): Int

    @Query(
        """UPDATE TimelineStatusEntity SET favourited = :favourited
WHERE timelineUserId = :accountId AND (serverId = :statusId OR reblogServerId = :statusId)""",
    )
    abstract suspend fun setFavourited(accountId: Long, statusId: String, favourited: Boolean)

    @Query(
        """UPDATE TimelineStatusEntity SET bookmarked = :bookmarked
WHERE timelineUserId = :accountId AND (serverId = :statusId OR reblogServerId = :statusId)""",
    )
    abstract suspend fun setBookmarked(accountId: Long, statusId: String, bookmarked: Boolean)

    @Query(
        """UPDATE TimelineStatusEntity SET reblogged = :reblogged
WHERE timelineUserId = :accountId AND (serverId = :statusId OR reblogServerId = :statusId)""",
    )
    abstract suspend fun setReblogged(accountId: Long, statusId: String, reblogged: Boolean)

    @Query(
        """DELETE FROM TimelineStatusEntity WHERE timelineUserId = :accountId AND
(authorServerId = :userId OR reblogAccountId = :userId)""",
    )
    abstract suspend fun removeAllByUser(accountId: Long, userId: String)

    /**
     * Removes everything for one account in the following tables:
     *
     * - TimelineStatusEntity
     * - TimelineAccountEntity
     * - StatusViewDataEntity
     * - TranslatedStatusEntity
     *
     * @param accountId id of the account for which to clean tables
     */
    suspend fun removeAll(accountId: Long) {
        removeAllStatuses(accountId)
        removeAllAccounts(accountId)
        removeAllStatusViewData(accountId)
        removeAllTranslatedStatus(accountId)
    }

    @Query("DELETE FROM TimelineStatusEntity WHERE timelineUserId = :accountId")
    abstract suspend fun removeAllStatuses(accountId: Long)

    @Query("DELETE FROM TimelineAccountEntity WHERE timelineUserId = :accountId")
    abstract suspend fun removeAllAccounts(accountId: Long)

    @Query("DELETE FROM StatusViewDataEntity WHERE timelineUserId = :accountId")
    abstract suspend fun removeAllStatusViewData(accountId: Long)

    @Query("DELETE FROM TranslatedStatusEntity WHERE timelineUserId = :accountId")
    abstract suspend fun removeAllTranslatedStatus(accountId: Long)

    @Query(
        """DELETE FROM TimelineStatusEntity WHERE timelineUserId = :accountId
AND serverId = :statusId""",
    )
    abstract suspend fun delete(accountId: Long, statusId: String)

    /**
     * Cleans the TimelineStatusEntity and TimelineAccountEntity tables from old entries.
     * @param accountId id of the account for which to clean tables
     * @param limit how many statuses to keep
     */
    suspend fun cleanup(accountId: Long, limit: Int) {
        cleanupStatuses(accountId, limit)
        cleanupAccounts(accountId)
        cleanupStatusViewData(accountId, limit)
        cleanupTranslatedStatus(accountId, limit)
    }

    /**
     * Cleans the TimelineStatusEntity table from old status entries.
     * @param accountId id of the account for which to clean statuses
     * @param limit how many statuses to keep
     */
    @Query(
        """DELETE FROM TimelineStatusEntity WHERE timelineUserId = :accountId AND serverId NOT IN
        (SELECT serverId FROM TimelineStatusEntity WHERE timelineUserId = :accountId ORDER BY LENGTH(serverId) DESC, serverId DESC LIMIT :limit)
    """,
    )
    abstract suspend fun cleanupStatuses(accountId: Long, limit: Int)

    /**
     * Cleans the TimelineAccountEntity table from accounts that are no longer referenced in the TimelineStatusEntity table
     * @param accountId id of the user account for which to clean timeline accounts
     */
    @Query(
        """DELETE FROM TimelineAccountEntity WHERE timelineUserId = :accountId AND serverId NOT IN
        (SELECT authorServerId FROM TimelineStatusEntity WHERE timelineUserId = :accountId)
        AND serverId NOT IN
        (SELECT reblogAccountId FROM TimelineStatusEntity WHERE timelineUserId = :accountId AND reblogAccountId IS NOT NULL)""",
    )
    abstract suspend fun cleanupAccounts(accountId: Long)

    /**
     * Cleans the StatusViewDataEntity table of old view data, keeping the most recent [limit]
     * entries.
     */
    @Query(
        """DELETE
             FROM StatusViewDataEntity
            WHERE timelineUserId = :accountId
              AND serverId NOT IN (
                SELECT serverId FROM StatusViewDataEntity WHERE timelineUserId = :accountId ORDER BY LENGTH(serverId) DESC, serverId DESC LIMIT :limit
              )
        """,
    )
    abstract suspend fun cleanupStatusViewData(accountId: Long, limit: Int)

    /**
     * Cleans the TranslatedStatusEntity table of old data, keeping the most recent [limit]
     * entries.
     */
    @Query(
        """DELETE
             FROM TranslatedStatusEntity
            WHERE timelineUserId = :accountId
              AND serverId NOT IN (
                SELECT serverId FROM TranslatedStatusEntity WHERE timelineUserId = :accountId ORDER BY LENGTH(serverId) DESC, serverId DESC LIMIT :limit
              )
        """,
    )
    abstract suspend fun cleanupTranslatedStatus(accountId: Long, limit: Int)

    @Query(
        """UPDATE TimelineStatusEntity SET poll = :poll
WHERE timelineUserId = :accountId AND (serverId = :statusId OR reblogServerId = :statusId)""",
    )
    abstract suspend fun setVoted(accountId: Long, statusId: String, poll: String)

    @Upsert
    abstract suspend fun upsertStatusViewData(svd: StatusViewDataEntity)

    /**
     * @param accountId the accountId to query
     * @param serverIds the IDs of the statuses to check
     * @return Map between serverIds and any cached viewdata for those statuses
     */
    @MapInfo(keyColumn = "serverId")
    @Query(
        """SELECT *
             FROM StatusViewDataEntity
            WHERE timelineUserId = :accountId
              AND serverId IN (:serverIds)""",
    )
    abstract suspend fun getStatusViewData(accountId: Long, serverIds: List<String>): Map<String, StatusViewDataEntity>

    @Query(
        """UPDATE TimelineStatusEntity SET pinned = :pinned
WHERE timelineUserId = :accountId AND (serverId = :statusId OR reblogServerId = :statusId)""",
    )
    abstract suspend fun setPinned(accountId: Long, statusId: String, pinned: Boolean)

    @Query(
        """DELETE FROM TimelineStatusEntity
WHERE timelineUserId = :accountId AND authorServerId IN (
SELECT serverId FROM TimelineAccountEntity WHERE username LIKE '%@' || :instanceDomain
AND timelineUserId = :accountId
)""",
    )
    abstract suspend fun deleteAllFromInstance(accountId: Long, instanceDomain: String)

    @Query("UPDATE TimelineStatusEntity SET filtered = NULL WHERE timelineUserId = :accountId AND (serverId = :statusId OR reblogServerId = :statusId)")
    abstract suspend fun clearWarning(accountId: Long, statusId: String): Int

    @Query("SELECT COUNT(*) FROM TimelineStatusEntity WHERE timelineUserId = :accountId")
    abstract suspend fun getStatusCount(accountId: Long): Int

    /** Developer tools: Find N most recent status IDs */
    @Query("SELECT serverId FROM TimelineStatusEntity WHERE timelineUserId = :accountId ORDER BY LENGTH(serverId) DESC, serverId DESC LIMIT :count")
    abstract suspend fun getMostRecentNStatusIds(accountId: Long, count: Int): List<String>
}
