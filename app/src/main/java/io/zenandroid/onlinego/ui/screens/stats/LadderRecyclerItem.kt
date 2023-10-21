package io.zenandroid.onlinego.ui.screens.stats

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.data.model.ogs.OGSPlayerFull.Ladder
import io.zenandroid.onlinego.databinding.ItemLadderBinding

class LadderRecyclerItem(val item: Ladder) : BindableItem<ItemLadderBinding>(item.hashCode().toLong()) {
    override fun bind(binding: ItemLadderBinding, position: Int) {
        binding.titleView.text = item.name
        binding.valueView.text = "#${item.rank}"
    }

    override fun getLayout() = R.layout.item_ladder

    override fun initializeViewBinding(view: View): ItemLadderBinding = ItemLadderBinding.bind(view)
}
