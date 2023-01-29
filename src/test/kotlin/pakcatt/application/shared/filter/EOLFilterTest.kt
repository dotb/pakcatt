package pakcatt.application.shared.filter

import org.junit.Test
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType

class EOLFilterTest: AppServiceTest() {

    @Test
    fun `test automatic EOL detection`() {
        var request = testRequest("settings\r\n")
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Settings\r\nsettings> ", response.responseString())

        request = testRequest("list")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Your terminal EOL is set to CRLF\r\n" +
                "settings> ", response.responseString())
        assert(response.responseString().contains("\r\n"))
        assert(!response.responseString().contains("\n\r"))
        assert(!response.responseString().contains("LF\nsettings"))
        assert(!response.responseString().contains("LF\rsettings"))

        request = testRequest("list\n\r")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Your terminal EOL is set to LFCR\n\r" +
                "settings> ", response.responseString())
        assert(response.responseString().contains("\n\r"))
        assert(!response.responseString().contains("\r\n"))
        assert(!response.responseString().contains("LF\nsettings"))
        assert(!response.responseString().contains("LF\rsettings"))

        request = testRequest("list\n")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Your terminal EOL is set to LF\n" +
                "settings> ", response.responseString())
        assert(response.responseString().contains("\n"))
        assert(!response.responseString().contains("\r\n"))
        assert(!response.responseString().contains("\n\r"))
        assert(!response.responseString().contains("LF\rsettings"))

        request = testRequest("list\r")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Your terminal EOL is set to CR\r" +
                "settings> ", response.responseString())
        assert(response.responseString().contains("\r"))
        assert(!response.responseString().contains("\r\n"))
        assert(!response.responseString().contains("\n\r"))
        assert(!response.responseString().contains("LF\nsettings"))
    }

}