package io.zenandroid.onlinego.ui.screens.stats

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.data.model.ogs.OGSPlayerFull.Group
import io.zenandroid.onlinego.databinding.ItemGroupBinding

class GroupRecyclerItem(val item: Group) : BindableItem<ItemGroupBinding>(item.hashCode().toLong()) {
    override fun bind(binding: ItemGroupBinding, position: Int) {
        binding.titleView.text = item.name
    }

    override fun getLayout() = R.layout.item_group

    override fun initializeViewBinding(view: View): ItemGroupBinding = ItemGroupBinding.bind(view)
}
