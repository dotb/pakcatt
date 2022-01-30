package pakcatt.application.shared.list

import junit.framework.TestCase
import org.junit.Test

class ListLimiterTest : TestCase() {

    val listOfStrings = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

    @Test
    fun `test items are limited by head`() {
        val listLimiter = ListLimiter<String>(2, LimitType.LIST_HEAD)
        listLimiter.addItems(listOfStrings)
        val stringBuilder = StringBuilder()
        for (limitedItem in listLimiter.getLimitedList()) {
            stringBuilder.append(limitedItem.item)
        }
        assertEquals("12", stringBuilder.toString())
    }

    @Test
    fun `test items are limited by tail`() {
        val listLimiter = ListLimiter<String>(2, LimitType.LIST_TAIL)
        listLimiter.addItems(listOfStrings)
        val stringBuilder = StringBuilder()
        for (limitedItem in listLimiter.getLimitedList()) {
            stringBuilder.append(limitedItem.item)
        }
        assertEquals("910", stringBuilder.toString())
    }

    @Test
    fun `test all items are listed when a null limit count is specified by head`() {
        val listLimiter = ListLimiter<String>(null, LimitType.LIST_HEAD)
        listLimiter.addItems(listOfStrings)
        val stringBuilder = StringBuilder()
        for (limitedItem in listLimiter.getLimitedList()) {
            stringBuilder.append(limitedItem.item)
        }
        assertEquals("12345678910", stringBuilder.toString())
    }

    @Test
    fun `test all items are listed when a null limit count is specified by tail`() {
        val listLimiter = ListLimiter<String>(null, LimitType.LIST_TAIL)
        listLimiter.addItems(listOfStrings)
        val stringBuilder = StringBuilder()
        for (limitedItem in listLimiter.getLimitedList()) {
            stringBuilder.append(limitedItem.item)
        }
        assertEquals("12345678910", stringBuilder.toString())
    }


    @Test
    fun `test limiter can handle out of bounds head`() {
        val listLimiter = ListLimiter<String>(12, LimitType.LIST_HEAD)
        listLimiter.addItems(listOfStrings)
        val stringBuilder = StringBuilder()
        for (limitedItem in listLimiter.getLimitedList()) {
            stringBuilder.append(limitedItem.item)
        }
        assertEquals("12345678910", stringBuilder.toString())
    }

    @Test
    fun `test limiter can handle out of bounds tail`() {
        val listLimiter = ListLimiter<String>(12, LimitType.LIST_TAIL)
        listLimiter.addItems(listOfStrings)
        val stringBuilder = StringBuilder()
        for (limitedItem in listLimiter.getLimitedList()) {
            stringBuilder.append(limitedItem.item)
        }
        assertEquals("12345678910", stringBuilder.toString())
    }

    @Test
    fun `test limiter can handle empty list head`() {
        val listLimiter = ListLimiter<String>(12, LimitType.LIST_HEAD)
        val stringBuilder = StringBuilder()
        for (item in listLimiter.getLimitedList()) {
            stringBuilder.append(item)
        }
        assertEquals("", stringBuilder.toString())
    }

    @Test
    fun `test limiter can handle empty list tail`() {
        val listLimiter = ListLimiter<String>(12, LimitType.LIST_TAIL)
        val stringBuilder = StringBuilder()
        for (item in listLimiter.getLimitedList()) {
            stringBuilder.append(item)
        }
        assertEquals("", stringBuilder.toString())
    }

    @Test
    fun `test limiter returns all added items`() {
        val listLimiter = ListLimiter<String>(12, LimitType.LIST_TAIL)
        listLimiter.addItems(listOfStrings)
        listLimiter.addItem("One more")
        assertEquals(11, listLimiter.getAllItems().size)
    }

}