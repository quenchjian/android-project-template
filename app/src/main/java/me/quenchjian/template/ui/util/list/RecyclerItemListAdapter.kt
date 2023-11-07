package me.quenchjian.template.ui.util.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class RecyclerItemListAdapter<T : Any, VH : RecyclerView.ViewHolder>(
  contentDiff: (T, T) -> Boolean = { t1, t2 -> t1 == t2 },
  itemDiff: (T, T) -> Boolean = { t1, t2 -> t1 == t2 }
) : ListAdapter<T, VH>(DefaultCallback<T>(contentDiff, itemDiff)), ListItemAction<T> {

  private val items = mutableListOf<T>()
  private val selectedPositions = mutableListOf<Int>()
  private var itemClick: ListItemClickListener<T> = { _, _ -> }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val holder = onCreateViewHolder(LayoutInflater.from(parent.context), parent, viewType)
    if (holder is ItemViewHolder) {
      holder.onItemViewClick { pos -> itemClick.invoke(pos, getItem(pos)) }
    }
    return holder
  }

  abstract fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VH

  override fun onBindViewHolder(holder: VH, position: Int) {
    val item = getItem(position)
    onBindViewHolder(holder, item)
    if (item is ListItem) {
      item.selected = selectedPositions.contains(position)
      if (holder is ItemViewHolder) {
        when (item.selected) {
          true -> holder.toggleSelectStyle()
          false -> holder.resetDefaultStyle()
        }
      }
    }
  }

  open fun onBindViewHolder(holder: VH, item: T) {}

  override fun insertItems(position: Int, items: List<T>) {
    if (items.isNotEmpty()) {
      val list = currentList.toMutableList()
      list.addAll(checkPosition(position), items)
      submitList(list)
    }
  }

  override fun addItems(items: List<T>) {
    if (items.isNotEmpty()) {
      val list = currentList.toMutableList()
      list.addAll(items)
      submitList(list)
    }
  }

  override fun getItems(): List<T> {
    return currentList
  }

  override fun getItem(position: Int): T {
    return super.getItem(position)
  }

  override fun getSize(): Int {
    return items.size
  }

  override fun updateItems(items: List<T>) {
    val list = currentList.toMutableList()
    items.forEach { item ->
      val position = indexOf(item)
      if (position >= 0) {
        list[position] = item
      }
    }
    submitList(list)
  }

  override fun removeItems(items: List<T>) {
    if (items.isNotEmpty()) {
      val list = currentList.toMutableList()
      list.removeAll(items)
      submitList(list)
    }
  }

  override fun refresh(items: List<T>) {
    val list = currentList.toMutableList()
    list.clear()
    list.addAll(items)
    submitList(list)
  }

  override fun clear(notifyChange: Boolean) {
    submitList(null)
  }

  override fun getSelectedPosition(): List<Int> {
    return selectedPositions
  }

  override fun getSelectedItem(): List<T> {
    return when {
      selectedPositions.isEmpty() -> emptyList()
      else -> selectedPositions.map { getItem(it) }
    }
  }

  override fun select(vararg positions: Int) {
    val prev = selectedPositions.toList()
    selectedPositions.clear()
    positions.forEach { selectedPositions.add(it) }
    prev.union(selectedPositions).forEach { notifyItemChanged(it) }
  }

  override fun unselect(vararg positions: Int) {
    positions.forEach { selectedPositions.remove(it) }
    positions.forEach { notifyItemChanged(it) }
  }

  override fun clearSelection() {
    val prev = selectedPositions.toList()
    selectedPositions.clear()
    prev.forEach { notifyItemChanged(it) }
  }

  override fun setItemClickListener(listener: ListItemClickListener<T>) {
    itemClick = listener
  }

  private class DefaultCallback<T : Any>(
    private val contentDiff: (T, T) -> Boolean = { t1, t2 -> t1 == t2 },
    private val itemDiff: (T, T) -> Boolean = { t1, t2 -> t1 == t2 }
  ) : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
      return itemDiff.invoke(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
      return contentDiff.invoke(oldItem, newItem)
    }
  }
}