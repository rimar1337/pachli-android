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

package app.pachli.adapter

import android.text.InputFilter
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.pachli.R
import app.pachli.databinding.ItemStatusBinding
import app.pachli.entity.Emoji
import app.pachli.entity.Filter
import app.pachli.interfaces.StatusActionListener
import app.pachli.util.SmartLengthInputFilter
import app.pachli.util.StatusDisplayOptions
import app.pachli.util.emojify
import app.pachli.util.formatNumber
import app.pachli.util.hide
import app.pachli.util.show
import app.pachli.util.unicodeWrap
import app.pachli.util.visible
import app.pachli.viewdata.StatusViewData
import at.connyduck.sparkbutton.helpers.Utils

open class StatusViewHolder(
    private val binding: ItemStatusBinding,
    root: View? = null,
) : StatusBaseViewHolder(root ?: binding.root) {

    override fun setupWithStatus(
        status: StatusViewData,
        listener: StatusActionListener,
        statusDisplayOptions: StatusDisplayOptions,
        payloads: Any?,
    ) = with(binding) {
        if (payloads == null) {
            val sensitive = !TextUtils.isEmpty(status.actionable.spoilerText)
            val expanded = status.isExpanded
            setupCollapsedState(sensitive, expanded, status, listener)
            val reblogging = status.rebloggingStatus
            if (reblogging == null || status.filterAction === Filter.Action.WARN) {
                statusInfo.hide()
            } else {
                val rebloggedByDisplayName = reblogging.account.name
                setRebloggedByDisplayName(
                    rebloggedByDisplayName,
                    reblogging.account.emojis,
                    statusDisplayOptions,
                )
                statusInfo.setOnClickListener {
                    listener.onOpenReblog(bindingAdapterPosition)
                }
            }
        }
        statusReblogsCount.visible(statusDisplayOptions.showStatsInline)
        statusFavouritesCount.visible(statusDisplayOptions.showStatsInline)
        setFavouritedCount(status.actionable.favouritesCount)
        setReblogsCount(status.actionable.reblogsCount)
        super.setupWithStatus(status, listener, statusDisplayOptions, payloads)
    }

    private fun setRebloggedByDisplayName(
        name: CharSequence,
        accountEmoji: List<Emoji>?,
        statusDisplayOptions: StatusDisplayOptions,
    ) = with(binding) {
        val wrappedName: CharSequence = name.unicodeWrap()
        val boostedText: CharSequence = context.getString(R.string.post_boosted_format, wrappedName)
        val emojifiedText =
            boostedText.emojify(accountEmoji, statusInfo, statusDisplayOptions.animateEmojis)
        statusInfo.text = emojifiedText
        statusInfo.show()
    }

    // don't use this on the same ViewHolder as setRebloggedByDisplayName, will cause recycling issues as paddings are changed
    protected fun setPollInfo(ownPoll: Boolean) = with(binding) {
        statusInfo.setText(if (ownPoll) R.string.poll_ended_created else R.string.poll_ended_voted)
        statusInfo.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_poll_24dp, 0, 0, 0)
        statusInfo.compoundDrawablePadding =
            Utils.dpToPx(context, 10)
        statusInfo.setPaddingRelative(Utils.dpToPx(context, 28), 0, 0, 0)
        statusInfo.show()
    }

    private fun setReblogsCount(reblogsCount: Int) = with(binding) {
        statusReblogsCount.text = formatNumber(reblogsCount.toLong(), 1000)
    }

    private fun setFavouritedCount(favouritedCount: Int) = with(binding) {
        statusFavouritesCount.text = formatNumber(favouritedCount.toLong(), 1000)
    }

    protected fun hideStatusInfo() = with(binding) {
        statusInfo.hide()
    }

    private fun setupCollapsedState(
        sensitive: Boolean,
        expanded: Boolean,
        status: StatusViewData,
        listener: StatusActionListener,
    ) = with(binding) {
        /* input filter for TextViews have to be set before text */
        if (status.isCollapsible && (!sensitive || expanded)) {
            buttonToggleContent.setOnClickListener {
                val position = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return@setOnClickListener
                listener.onContentCollapsedChange(
                    !status.isCollapsed,
                    position,
                )
            }
            buttonToggleContent.show()
            if (status.isCollapsed) {
                buttonToggleContent.setText(R.string.post_content_warning_show_more)
                content.filters = COLLAPSE_INPUT_FILTER
            } else {
                buttonToggleContent.setText(R.string.post_content_warning_show_less)
                content.filters = NO_INPUT_FILTER
            }
        } else {
            buttonToggleContent.hide()
            content.filters = NO_INPUT_FILTER
        }
    }

    override fun showStatusContent(show: Boolean) = with(binding) {
        super.showStatusContent(show)
        buttonToggleContent.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun toggleExpandedState(
        sensitive: Boolean,
        expanded: Boolean,
        status: StatusViewData,
        statusDisplayOptions: StatusDisplayOptions,
        listener: StatusActionListener,
    ) {
        setupCollapsedState(sensitive, expanded, status, listener)
        super.toggleExpandedState(sensitive, expanded, status, statusDisplayOptions, listener)
    }

    companion object {
        private val COLLAPSE_INPUT_FILTER = arrayOf<InputFilter>(SmartLengthInputFilter)
        private val NO_INPUT_FILTER = arrayOfNulls<InputFilter>(0)
    }
}
