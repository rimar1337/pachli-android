/*
 * Copyright 2023 Pachli Association
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

package app.pachli.components.timeline.viewmodel

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import app.pachli.entity.Status
import timber.log.Timber
import javax.inject.Inject

private val INVALID = LoadResult.Invalid<String, Status>()

/** [PagingSource] for Mastodon Status, identified by the Status ID */
class NetworkTimelinePagingSource @Inject constructor(
    private val pageCache: PageCache,
) : PagingSource<String, Status>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Status> {
        Timber.d("- load(), type = ${params.javaClass.simpleName}, key = ${params.key}")
        pageCache.debug()

        val page = synchronized(pageCache) {
            if (pageCache.isEmpty()) {
                return@synchronized null
            }

            when (params) {
                is LoadParams.Refresh -> {
                    // Return the page that contains the given key, or the most recent page if
                    // the key isn't in the cache.
                    params.key?.let { key ->
                        pageCache.floorEntry(key)?.value
                    } ?: pageCache.lastEntry()?.value
                }
                // Loading previous / next pages (`Prepend` or `Append`) is a little complicated.
                //
                // Append and Prepend requests have a `params.key` that corresponds to the previous
                // or next page. For some timeline types those keys have the same form as the
                // item keys, and are valid item keys.
                //
                // But for some timeline types they are completely different.
                //
                // For example, bookmarks might have item keys that look like 110542553707722778
                // but prevKey / nextKey values that look like 1480606 / 1229303.
                //
                // There's no guarantee that the `nextKey` value for one page matches the `prevKey`
                // value of the page immediately before it.
                //
                // E.g., suppose `pages` has the following entries (older entries have lower page
                // indices).
                //
                // .--- page index
                // |     .-- ID of last item (key in `pageCache`)
                // v     V
                // 0: k: 109934818460629189, prevKey: 995916, nextKey: 941865
                // 1: k: 110033940961955385, prevKey: 1073324, nextKey: 997376
                // 2: k: 110239564017438509, prevKey: 1224838, nextKey: 1073352
                // 3: k: 110542553707722778, prevKey: 1480606, nextKey: 1229303
                //
                // And the request is `LoadParams.Append` with `params.key` == 1073352. This means
                // "fetch the page *before* the page that has `nextKey` == 1073352".
                //
                // The desired page has index 1. But that can't be found directly, because although
                // the page after it (index 2) points back to it with the `nextKey` value, the page
                // at index 1 **does not** have a `prevKey` value of 1073352. There can be gaps in
                // the `prevKey` / `nextKey` chain -- I assume this is a Mastodon implementation
                // detail.
                //
                // Further, we can't assume anything about the structure of the keys.
                //
                // To find the correct page for Append we must:
                //
                // 1. Find the page that has a `nextKey` value that matches `params.key` (page 2)
                // 2. Get that page's key ("110239564017438509")
                // 3. Return the page with the key that is immediately lower than the key from step 2
                //
                // The approach for Prepend is the same, except it is `prevKey` that is checked.
                is LoadParams.Append -> {
                    pageCache.firstNotNullOfOrNull { entry -> entry.takeIf { it.value.nextKey == params.key }?.value }
                        ?.let { page -> pageCache.lowerEntry(page.data.last().id)?.value }
                }
                is LoadParams.Prepend -> {
                    pageCache.firstNotNullOfOrNull { entry -> entry.takeIf { it.value.prevKey == params.key }?.value }
                        ?.let { page -> pageCache.higherEntry(page.data.last().id)?.value }
                }
            }
        }

        if (page == null) {
            Timber.d("  Returning empty page")
        } else {
            Timber.d("  Returning full page:")
            Timber.d("     $page")
        }

        // Bail if this paging source has already been invalidated. If you do not do this there
        // is a lot of spurious animation, especially during the initial load, as multiple pages
        // are loaded and the paging source is repeatedly invalidated.
        if (invalid) {
            Timber.d("Invalidated, returning LoadResult.Invalid")
            return INVALID
        }

        // Calculate itemsBefore and itemsAfter values to include in the returned Page.
        // If you do not do this (and this is not documented anywhere) then the anchorPosition
        // in the PagingState (used in getRefreshKey) is bogus, and refreshing the list can
        // result in large jumps in the user's position.
        //
        // The items are calculated relative to the local cache, not the remote data source.
        val itemsBefore = page?.let {
            it.prevKey?.let { key ->
                pageCache.tailMap(key).values.fold(0) { sum, p -> sum + p.data.size }
            }
        } ?: 0
        val itemsAfter = page?.let {
            // Note: headMap and tailMap have different behaviour, tailMap is greater-or-equal,
            // headMap is strictly less than, so `it.nextKey` does not work here.
            pageCache.headMap(it.data.first().id).values.fold(0) { sum, p -> sum + p.data.size }
        } ?: 0

        return LoadResult.Page(
            page?.data ?: emptyList(),
            nextKey = page?.nextKey,
            prevKey = page?.prevKey,
            itemsAfter = itemsAfter,
            itemsBefore = itemsBefore,
        )
    }

    override fun getRefreshKey(state: PagingState<String, Status>): String? {
        val refreshKey = state.anchorPosition?.let {
            state.closestItemToPosition(it)?.id
        } ?: pageCache.firstEntry()?.value?.data?.let {
            it.getOrNull(it.size / 2)?.id
        }

        Timber.d("- getRefreshKey(), state.anchorPosition = ${state.anchorPosition}, return $refreshKey")
        return refreshKey
    }
}
