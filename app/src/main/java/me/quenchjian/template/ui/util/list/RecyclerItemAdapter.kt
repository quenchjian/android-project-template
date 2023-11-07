package me.quenchjian.template.ui.util.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class RecyclerItemAdapter<T: Any, VH : RecyclerView.ViewHolder>
  : RecyclerView.Adapter<VH>(), ListItemAction<T> {

  private val items = mutableListOf<T>()
  private val selectedPositions = mutableListOf<Int>()
  private var itemClick: ListItemClickListener<T> = { _, _ -> }

  final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
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

  override fun getItemCount(): Int = getSize()

  override fun insertItems(position: Int, items: List<T>) {
    if (items.isNotEmpty()) {
      this.items.addAll(checkPosition(position), items)
      notifyItemRangeInserted(position, items.size)
    }
  }

  override fun addItems(items: List<T>) {
    if (items.isNotEmpty()) {
      val positionStart = getSize()
      this.items.addAll(items)
      notifyItemRangeInserted(positionStart, items.size)
    }
  }

  override fun getItems(): List<T> {
    return items
  }

  override fun getItem(position: Int): T {
    return items[checkPosition(position)]
  }

  override fun getSize(): Int {
    return items.size
  }

  override fun updateItems(items: List<T>) {
    items.forEach { item ->
      val position = indexOf(item)
      if (position >= 0) {
        this.items[position] = item
        notifyItemChanged(position)
      }
    }
  }

  @SuppressLint("NotifyDataSetChanged")
  override fun removeItems(items: List<T>) {
    if (items.isNotEmpty()) {
      this.items.removeAll(items)
      notifyDataSetChanged()
    }
  }

  @SuppressLint("NotifyDataSetChanged")
  override fun refresh(items: List<T>) {
    this.items.clear()
    this.items.addAll(items)
    notifyDataSetChanged()
  }

  override fun clear(notifyChange: Boolean) {
    val size = getSize()
    items.clear()
    if (notifyChange) {
      notifyItemRangeRemoved(0, size)
    }
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
}