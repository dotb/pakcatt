package pakcatt.application.last

import org.junit.Test
import pakcatt.application.last.persistence.LastEntry
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType
import java.util.*

class LastAppTest: AppServiceTest() {


    @Test
    fun `test last app returns results in the correct order for interactive requests`() {
        `test starting a connection to the BBS with messages`()

        var request = testRequest("last")
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("VK3LIT: Thursday, 01 January 1970, 10:16 via 144.875Mhz${stringUtils.EOL}" +
                                "VK2VRO: Thursday, 01 January 1970, 12:46 via 144.875Mhz${stringUtils.EOL}" +
                                "VK4XSS: Thursday, 01 January 1970, 15:33 via unknown${stringUtils.EOL}" +
                                "VK3DUB: Thursday, 01 January 1970, 23:53 via 144.875Mhz${stringUtils.EOL}" +
                                "VK3FUR: Friday, 02 January 1970, 13:46 via 144.875Mhz${stringUtils.EOL}" +
                                "${stringUtils.EOL}menu> ", response.responseString())
    }

    @Test
    fun `test last app returns results in the correct order for async requests`() {
        `test starting a connection to the BBS with messages`()

        var request = testRequest("last", false)
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("VK3LIT: 01-01-1970@10:16 via 144.875Mhz${stringUtils.EOL}" +
                                "VK2VRO: 01-01-1970@12:46 via 144.875Mhz${stringUtils.EOL}" +
                                "VK4XSS: 01-01-1970@15:33 via unknown${stringUtils.EOL}" +
                                "VK3DUB: 01-01-1970@23:53 via 144.875Mhz${stringUtils.EOL}" +
                                "VK3FUR: 02-01-1970@13:46 via 144.875Mhz${stringUtils.EOL}",
                                response.responseString())
    }

    @Test
    fun `test last app returns an individual result`() {
        mockedLastEntryRepository.lastEntryIsAvailable = true
        `test starting a connection to the BBS with messages`()

        var request = testRequest()
        request.message = "last vk3lit"

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("VK3LIT: Thursday, 01 January 1970, 10:16 via 144.875Mhz${stringUtils.EOL}" +
                                "${stringUtils.EOL}menu> ", response.responseString())
    }

    @Test
    fun `test last app returns an an unknown result`() {
        mockedLastEntryRepository.lastEntryIsAvailable = false
        `test starting a connection to the BBS with messages`()

        var request = testRequest()
        request.message = "last unknown"

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Haven't seen UNKNOWN${stringUtils.EOL}" +
                "${stringUtils.EOL}menu> ", response.responseString())
    }

    @Test
    fun `test last entry is updated with and without a sub app engaged`() {
        assertEquals(null, mockedLastEntryRepository.lastInsertedEntry)
        `test starting a connection to the BBS with messages`()
        assertEquals("VK3LIT", mockedLastEntryRepository.lastInsertedEntry?.callsign)

        var previousLastEntryDate = mockedLastEntryRepository.lastInsertedEntry?.lastSeen
        Thread.sleep(1000) // Yuck, I know
        var request = testRequest()
        request.message = "mail"
        appService.getResponseForReceivedMessage(request)
        assertEquals("VK3LIT", mockedLastEntryRepository.lastInsertedEntry?.callsign)
        assert(null != previousLastEntryDate && previousLastEntryDate.before(mockedLastEntryRepository.lastInsertedEntry?.lastSeen))

        previousLastEntryDate = mockedLastEntryRepository.lastInsertedEntry?.lastSeen
        Thread.sleep(1000) // Yuck, I know
        request.remoteCallsign = "VK2VRO-0"
        request.remoteCallsignWithoutSSID = "VK2VRO"
        request.message = "list"
        appService.getResponseForReceivedMessage(request)
        assert(null != previousLastEntryDate && previousLastEntryDate.before(mockedLastEntryRepository.lastInsertedEntry?.lastSeen))

    }

}