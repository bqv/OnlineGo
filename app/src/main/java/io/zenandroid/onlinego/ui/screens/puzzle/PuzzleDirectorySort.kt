package io.zenandroid.onlinego.ui.screens.puzzle

import io.zenandroid.onlinego.data.model.local.PuzzleCollection

sealed class PuzzleDirectorySort (
    val asc: Boolean
) {
    val desc: Boolean
        get() = !asc

    abstract val comparator: Comparator<PuzzleCollection?>
    abstract val reversed: PuzzleDirectorySort

    class NameSort(asc: Boolean = true): PuzzleDirectorySort(asc) {
        override val comparator: Comparator<PuzzleCollection?>
            = if(asc) compareBy { it?.name }
        else compareByDescending { it?.name }

      override val reversed: PuzzleDirectorySort
            get() = NameSort(!asc)

      override fun toString(): String {
        return "A -> Z"
      }
    }

    class RatingSort(asc: Boolean = true): PuzzleDirectorySort(asc) {
        override val comparator: Comparator<PuzzleCollection?>
            = if(asc) compareBy<PuzzleCollection?> { it?.rating }
          .thenByDescending { it?.rating_count }
        else compareByDescending<PuzzleCollection?> { it?.rating }
          .thenByDescending { it?.rating_count }

      override val reversed: PuzzleDirectorySort
            get() = RatingSort(!asc)

      override fun toString(): String {
        return "Rating"
      }
    }

    class CountSort(asc: Boolean = true): PuzzleDirectorySort(asc) {
        override val comparator: Comparator<PuzzleCollection?>
            = if(asc) compareBy { it?.puzzle_count }
        else compareByDescending { it?.puzzle_count }

      override val reversed: PuzzleDirectorySort
            get() = CountSort(!asc)

      override fun toString(): String {
        return "Puzzle count"
      }
    }

    class ViewsSort(asc: Boolean = true): PuzzleDirectorySort(asc) {
        override val comparator: Comparator<PuzzleCollection?>
            = if(asc) compareBy { it?.view_count }
        else compareByDescending { it?.view_count }

      override val reversed: PuzzleDirectorySort
            get() = ViewsSort(!asc)

      override fun toString(): String {
        return "Views"
      }
    }

    class RankSort(asc: Boolean = true): PuzzleDirectorySort(asc) {
        override val comparator: Comparator<PuzzleCollection?>
            = if(asc) compareBy({ it?.min_rank }, { it?.max_rank })
              else compareByDescending<PuzzleCollection?> { it?.min_rank }
          .thenByDescending { it?.max_rank }

      override val reversed: PuzzleDirectorySort
            get() = RankSort(!asc)

      override fun toString(): String {
        return "Rank"
      }
    }
}
