package app.pachli.components.filters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.pachli.R
import app.pachli.databinding.ItemRemovableBinding
import app.pachli.entity.Filter
import app.pachli.util.BindingHolder
import app.pachli.util.getRelativeTimeSpanString

class FiltersAdapter(val listener: FiltersListener, val filters: List<Filter>) :
    RecyclerView.Adapter<BindingHolder<ItemRemovableBinding>>() {

    override fun getItemCount(): Int = filters.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemRemovableBinding> {
        return BindingHolder(ItemRemovableBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemRemovableBinding>, position: Int) {
        val binding = holder.binding
        val resources = binding.root.resources
        val actions = resources.getStringArray(R.array.filter_actions)
        val contexts = resources.getStringArray(R.array.filter_contexts)

        val filter = filters[position]
        val context = binding.root.context
        binding.textPrimary.text = if (filter.expiresAt == null) {
            filter.title
        } else {
            context.getString(
                R.string.filter_expiration_format,
                filter.title,
                getRelativeTimeSpanString(binding.root.context, filter.expiresAt.time, System.currentTimeMillis()),
            )
        }
        binding.textSecondary.text = context.getString(
            R.string.filter_description_format,
            actions.getOrNull(filter.action.ordinal - 1),
            filter.context.map { contexts.getOrNull(Filter.Kind.from(it).ordinal) }.joinToString("/"),
        )

        binding.delete.setOnClickListener {
            listener.deleteFilter(filter)
        }

        binding.root.setOnClickListener {
            listener.updateFilter(filter)
        }
    }
}
