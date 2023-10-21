package io.zenandroid.onlinego.ui.screens.stats

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.data.model.ogs.OGSPlayerFull.Tournament
import io.zenandroid.onlinego.databinding.ItemTournamentBinding

class TournamentRecyclerItem(val item: Tournament) : BindableItem<ItemTournamentBinding>(item.hashCode().toLong()) {
    override fun bind(binding: ItemTournamentBinding, position: Int) {
        binding.titleView.text = item.name
        binding.trophyView.text = "\uD83C\uDFC6"
    }

    override fun getLayout() = R.layout.item_tournament

    override fun initializeViewBinding(view: View): ItemTournamentBinding = ItemTournamentBinding.bind(view)
}
