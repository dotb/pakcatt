package pakcatt.application.shared.list

import java.lang.Integer.max
import java.lang.Integer.min

enum class LimitType {
    LIST_HEAD, LIST_TAIL
}

/**
 * Limit a list of items by count, and specify if the top of bottom of the list
 * should be kept. A limitCount of null will return the whole list.
 */
class ListLimiter<T>(private val limitCount: Int?, private val limitType: LimitType) {

    private val allItems = ArrayList<T>(0)

    fun addItem(item: T): ListLimiter<T> {
        allItems.add(item)
        return this
    }

    fun addItems(itemList: List<T>): ListLimiter<T> {
        allItems.addAll(itemList)
        return this
    }

    fun getAllItems(): List<T> {
        return allItems
    }

    fun getLimitedList(): List<LimitedItem<T>> {
        if (null != limitCount) {
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
        } else {
            return allItems.mapIndexed { index, t -> LimitedItem(t, index) }
        }
    }

}