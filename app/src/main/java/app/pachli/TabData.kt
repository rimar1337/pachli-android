/*
 * Copyright 2019 Conny Duck
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

package app.pachli

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import app.pachli.components.conversation.ConversationsFragment
import app.pachli.components.notifications.NotificationsFragment
import app.pachli.components.timeline.TimelineFragment
import app.pachli.components.timeline.TimelineKind
import app.pachli.components.trending.TrendingLinksFragment
import app.pachli.components.trending.TrendingTagsFragment
import java.util.Objects

/** this would be a good case for a sealed class, but that does not work nice with Room */

const val HOME = "Home"
const val NOTIFICATIONS = "Notifications"
const val LOCAL = "Local"
const val FEDERATED = "Federated"
const val DIRECT = "Direct"
const val TRENDING_TAGS = "TrendingTags"
const val TRENDING_LINKS = "TrendingLinks"
const val TRENDING_STATUSES = "TrendingStatuses"
const val HASHTAG = "Hashtag"
const val LIST = "List"
const val BOOKMARKS = "Bookmarks"

data class TabData(
    val id: String,
    @StringRes val text: Int,
    @DrawableRes val icon: Int,
    val fragment: (List<String>) -> Fragment,
    val arguments: List<String> = emptyList(),
    val title: (Context) -> String = { context -> context.getString(text) },
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TabData

        if (id != other.id) return false
        return arguments == other.arguments
    }

    override fun hashCode() = Objects.hash(id, arguments)
}

fun createTabDataFromId(id: String, arguments: List<String> = emptyList()): TabData {
    return when (id) {
        HOME -> TabData(
            id = HOME,
            text = R.string.title_home,
            icon = R.drawable.ic_home_24dp,
            fragment = { TimelineFragment.newInstance(TimelineKind.Home) },
        )
        NOTIFICATIONS -> TabData(
            id = NOTIFICATIONS,
            text = R.string.title_notifications,
            icon = R.drawable.ic_notifications_24dp,
            fragment = { NotificationsFragment.newInstance() },
        )
        LOCAL -> TabData(
            id = LOCAL,
            text = R.string.title_public_local,
            icon = R.drawable.ic_local_24dp,
            fragment = { TimelineFragment.newInstance(TimelineKind.PublicLocal) },
        )
        FEDERATED -> TabData(
            id = FEDERATED,
            text = R.string.title_public_federated,
            icon = R.drawable.ic_public_24dp,
            fragment = { TimelineFragment.newInstance(TimelineKind.PublicFederated) },
        )
        DIRECT -> TabData(
            id = DIRECT,
            text = R.string.title_direct_messages,
            icon = R.drawable.ic_reblog_direct_24dp,
            fragment = { ConversationsFragment.newInstance() },
        )
        TRENDING_TAGS -> TabData(
            id = TRENDING_TAGS,
            text = R.string.title_public_trending_hashtags,
            icon = R.drawable.ic_trending_up_24px,
            fragment = { TrendingTagsFragment.newInstance() },
        )
        TRENDING_LINKS -> TabData(
            id = TRENDING_LINKS,
            text = R.string.title_public_trending_links,
            icon = R.drawable.ic_trending_up_24px,
            fragment = { TrendingLinksFragment.newInstance() },
        )
        TRENDING_STATUSES -> TabData(
            id = TRENDING_STATUSES,
            text = R.string.title_public_trending_statuses,
            icon = R.drawable.ic_trending_up_24px,
            fragment = { TimelineFragment.newInstance(TimelineKind.TrendingStatuses) },
        )
        HASHTAG -> TabData(
            id = HASHTAG,
            text = R.string.hashtags,
            icon = R.drawable.ic_hashtag,
            fragment = { args -> TimelineFragment.newInstance(TimelineKind.Tag(args)) },
            arguments = arguments,
            title = { context -> arguments.joinToString(separator = " ") { context.getString(R.string.title_tag, it) } },
        )
        LIST -> TabData(
            id = LIST,
            text = R.string.list,
            icon = R.drawable.ic_list,
            fragment = { args -> TimelineFragment.newInstance(TimelineKind.UserList(args.first(), args.last())) },
            arguments = arguments,
            title = { arguments.getOrNull(1).orEmpty() },
        )
        BOOKMARKS -> TabData(
            id = BOOKMARKS,
            text = R.string.title_bookmarks,
            icon = R.drawable.ic_bookmark_active_24dp,
            fragment = { TimelineFragment.newInstance(TimelineKind.Bookmarks) },
        )
        else -> throw IllegalArgumentException("unknown tab type")
    }
}

fun defaultTabs(): List<TabData> {
    return listOf(
        createTabDataFromId(HOME),
        createTabDataFromId(NOTIFICATIONS),
        createTabDataFromId(LOCAL),
        createTabDataFromId(DIRECT),
    )
}
