/* Copyright 2019 Tusky Contributors
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

package app.pachli.components.account

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import app.pachli.components.account.media.AccountMediaFragment
import app.pachli.components.timeline.TimelineFragment
import app.pachli.components.timeline.TimelineKind
import app.pachli.interfaces.RefreshableFragment
import app.pachli.util.CustomFragmentStateAdapter

class AccountPagerAdapter(
    activity: FragmentActivity,
    private val accountId: String,
) : CustomFragmentStateAdapter(activity) {

    override fun getItemCount() = TAB_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TimelineFragment.newInstance(TimelineKind.User.Posts(accountId), false)
            1 -> TimelineFragment.newInstance(TimelineKind.User.Replies(accountId), false)
            2 -> TimelineFragment.newInstance(TimelineKind.User.Pinned(accountId), false)
            3 -> AccountMediaFragment.newInstance(accountId)
            else -> throw AssertionError("Page $position is out of AccountPagerAdapter bounds")
        }
    }

    fun refreshContent() {
        for (i in 0 until TAB_COUNT) {
            val fragment = getFragment(i)
            if (fragment != null && fragment is RefreshableFragment) {
                (fragment as RefreshableFragment).refreshContent()
            }
        }
    }

    companion object {
        private const val TAB_COUNT = 4
    }
}
