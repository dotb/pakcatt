package pakcatt.application.shared.list

import java.lang.Integer.max
import java.lang.Integer.min

enum class LimitType {
    LIST_HEAD, LIST_TAIL
}

class ListLimiter<T>(val limitCount: Int, val limitType: LimitType) {

    private val allItems = ArrayList<T>(0)

    fun addItem(item: T) {
        allItems.add(item)
    }

    fun addItems(itemList: List<T>) {
        allItems.addAll(itemList)
    }

    fun getAllItems(): List<T> {
        return allItems
    }

    fun getLimitedList(): List<LimitedItem<T>> {
        val totalItems = allItems.size
        val rangeOfIndexesToInclude = when (limitType) {
            LimitType.LIST_HEAD -> 0 until min(totalItems, limitCount)
            LimitType.LIST_TAIL -> max(0, totalItems - limitCount) until totalItems
        }

        val limitedList = ArrayList<LimitedItem<T>>(0)
        for (i in rangeOfIndexesToInclude) {
            val item = allItems[i]
            val originalItemIndex = allItems.indexOf(item)
            limitedList.add(LimitedItem(item, originalItemIndex))
        }
        return limitedList
    }

}