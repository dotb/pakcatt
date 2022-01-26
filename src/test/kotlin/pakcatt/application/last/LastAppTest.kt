package pakcatt.application.last

import org.junit.Test
import pakcatt.application.last.persistence.LastEntry
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType
import java.util.*

class LastAppTest: AppServiceTest() {


    @Test
    fun `test last app returns results in the correct order for sync requests`() {
        `test starting a connection to the BBS with messages`()

        var request = testRequest("last")
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("VK3LIT: Thursday, 01 January 1970, 10:16${stringUtils.EOL}" +
                                "VK2VRO: Thursday, 01 January 1970, 12:46${stringUtils.EOL}" +
                                "VK4XSS: Thursday, 01 January 1970, 15:33${stringUtils.EOL}" +
                                "VK3DUB: Thursday, 01 January 1970, 23:53${stringUtils.EOL}" +
                                "VK3FUR: Friday, 02 January 1970, 13:46${stringUtils.EOL}" +
                                "${stringUtils.EOL}menu> ", response.responseString())
    }

    @Test
    fun `test last app returns results in the correct order for async requests`() {
        `test starting a connection to the BBS with messages`()

        var request = testRequest("last", false)
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("VK3LIT: 01-01-1970@10:16${stringUtils.EOL}" +
                                "VK2VRO: 01-01-1970@12:46${stringUtils.EOL}" +
                                "VK4XSS: 01-01-1970@15:33${stringUtils.EOL}" +
                                "VK3DUB: 01-01-1970@23:53${stringUtils.EOL}" +
                                "VK3FUR: 02-01-1970@13:46${stringUtils.EOL}",
                                response.responseString())
    }

    @Test
    fun `test last app returns an individual result`() {
        mockedLastEntryRepository.lastSingleEntry = LastEntry("VK3LIT", Date(1000000))
        `test starting a connection to the BBS with messages`()

        var request = testRequest()
        request.message = "last vk3lit"

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("VK3LIT: Thursday, 01 January 1970, 10:16${stringUtils.EOL}" +
                                "${stringUtils.EOL}menu> ", response.responseString())
    }

    @Test
    fun `test last app returns an an unknown result`() {
        `test starting a connection to the BBS with messages`()

        var request = testRequest()
        request.message = "last unknown"

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Haven't seen UNKNOWN${stringUtils.EOL}" +
                "${stringUtils.EOL}menu> ", response.responseString())
    }

}