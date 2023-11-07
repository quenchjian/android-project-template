package me.quenchjian.template.ui.util.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

abstract class ListItemAdapter<T : Any, VH : ItemViewHolder> : BaseAdapter(), ListItemAction<T> {

  private val items = mutableListOf<T>()
  private val selectedPositions = mutableListOf<Int>()
  private var itemClick: ListItemClickListener<T> = { _, _ -> }

  override fun getCount(): Int {
    return getSize()
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  final override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val holder: VH
    if (convertView == null) {
      val inflater = LayoutInflater.from(parent.context)
      holder = onCreateViewHolder(inflater, parent, getItemViewType(position))
      holder.rootView.tag = holder
      holder.onItemViewClick { pos -> itemClick.invoke(pos, getItem(pos)) }
    } else {
      @Suppress("UNCHECKED_CAST")
      holder = convertView.tag as VH
    }
    holder.positionInAdapter = position
    onBindViewHolder(holder, position)
    return holder.rootView
  }

  abstract fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VH

  open fun onBindViewHolder(holder: VH, position: Int) {
    val item = getItem(position)
    onBindViewHolder(holder, item)
    if (item is ListItem) {
      item.selected = selectedPositions.contains(position)
      when (item.selected) {
        true -> holder.toggleSelectStyle()
        false -> holder.resetDefaultStyle()
      }
    }
  }

  open fun onBindViewHolder(holder: VH, item: T) {}

  override fun insertItems(position: Int, items: List<T>) {
    if (items.isNotEmpty()) {
      this.items.addAll(checkPosition(position), items)
      notifyDataSetChanged()
    }
  }

  override fun addItems(items: List<T>) {
    if (items.isNotEmpty()) {
      this.items.addAll(items)
      notifyDataSetChanged()
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
      }
    }
    notifyDataSetChanged()
  }

  override fun removeItems(items: List<T>) {
    if (items.isNotEmpty()) {
      this.items.removeAll(items)
      notifyDataSetChanged()
    }
  }

  override fun refresh(items: List<T>) {
    this.items.clear()
    this.items.addAll(items)
    notifyDataSetChanged()
  }

  override fun clear(notifyChange: Boolean) {
    items.clear()
    if (notifyChange) {
      notifyDataSetChanged()
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
    selectedPositions.clear()
    positions.forEach { selectedPositions.add(it) }
    notifyDataSetChanged()
  }

  override fun unselect(vararg positions: Int) {
    positions.forEach { selectedPositions.remove(it) }
    notifyDataSetChanged()
  }

  override fun clearSelection() {
    selectedPositions.clear()
    notifyDataSetChanged()
  }

  override fun setItemClickListener(listener: ListItemClickListener<T>) {
    itemClick = listener
  }
}