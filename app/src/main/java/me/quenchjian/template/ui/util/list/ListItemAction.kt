package me.quenchjian.template.ui.util.list

interface ListItemAction<T> {

  fun indexOf(item: T): Int

  fun insertItems(position: Int, items: List<T>)
  fun insertItem(position: Int, item: T) = insertItems(position, listOf(item))

  fun addItems(items: List<T>)
  fun addItem(item: T) = addItems(listOf(item))

  fun addItemsTop(items: List<T>) = insertItems(0, items)
  fun addItemTop(item: T) = insertItem(0, item)

  fun getItems(): List<T>
  fun getItem(position: Int): T
  fun getSize(): Int

  fun updateItems(items: List<T>)
  fun updateItem(item: T) = updateItems(listOf(item))

  fun removeItems(items: List<T>)
  fun removeItem(item: T) = removeItems(listOf(item))
  fun removeItems(vararg positions: Int) = removeItems(positions.map { getItem(it) })
  fun removeItem(position: Int) = removeItems(position)

  fun refresh(items: List<T>)
  fun clear(notifyChange: Boolean = true)

  fun getSelectedPosition(): List<Int>
  fun getSelectedItem(): List<T>
  fun select(vararg positions: Int)
  fun select(vararg items: T) = select(*items.map { indexOf(it) }.toIntArray())
  fun unselect(vararg positions: Int)
  fun clearSelection()

  /**
   * check position is between 0 and [getSize] - 1
   * @return position
   */
  @Throws(IndexOutOfBoundsException::class)
  fun checkPosition(position: Int): Int {
    if (position !in 0 until getSize()) {
      throw IndexOutOfBoundsException("Index out of range: $position")
    }
    return position
  }

  fun setItemClickListener(listener: ListItemClickListener<T>)
}