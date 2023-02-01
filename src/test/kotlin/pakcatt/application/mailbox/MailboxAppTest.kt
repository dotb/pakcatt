package pakcatt.application.mailbox

import org.junit.Test
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType

class MailboxAppTest: AppServiceTest() {

    @Test
    fun `test open mailbox and list messages`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest()
        request.message = "mail"

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Mail${stringUtils.EOL}mail> ", response.responseString())

        request = testRequest()
        request.message = "list"

        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}" +
                "${startBold}  No  Date          From    To      Subject${resetFormat}${stringUtils.EOL}" +
                "  1   01 Jan 10:00  PAKCATT VK3LIT  Subject 1${stringUtils.EOL}" +
                "* 2   01 Jan 10:16  VK2VRO  VK3LIT  Subject 2${stringUtils.EOL}" +
                "* 3   01 Jan 12:30  VK3LIT  VK2VRO  Subject 3${stringUtils.EOL}" +
                "* 4   02 Jan 13:46  PAKCATT VK3LIT  Subject 4${stringUtils.EOL}" +
                "4 messages${stringUtils.EOL}" +
                "${stringUtils.EOL}mail> ", response.responseString())
    }

    @Test
    fun `test open mailbox and list new messages with new messages`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest()
        request.message = "mail"

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Mail${stringUtils.EOL}mail> ", response.responseString())

        request = testRequest()
        request.message = "list unread"

        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}" +
                "${startBold}  No  Date          From    To      Subject${resetFormat}${stringUtils.EOL}" +
                "* 2   01 Jan 10:16  VK2VRO  VK3LIT  Subject 2${stringUtils.EOL}" +
                "* 3   01 Jan 12:30  VK3LIT  VK2VRO  Subject 3${stringUtils.EOL}" +
                "* 4   02 Jan 13:46  PAKCATT VK3LIT  Subject 4${stringUtils.EOL}" +
                "3 messages${stringUtils.EOL}" +
                "${stringUtils.EOL}mail> ", response.responseString())
    }

    @Test
    fun `test open mailbox and list new messages without new messages`() {
        `test starting a connection to the BBS with no messages`()
        var request = testRequest()
        request.message = "mail"

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Mail${stringUtils.EOL}mail> ", response.responseString())

        request = testRequest()
        request.message = "list new"

        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("0 messages${stringUtils.EOL}" +
                "${stringUtils.EOL}mail> ", response.responseString())
    }

    fun `test mail list async`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest("mail.list", false)
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("  1   01-01 10:00   PAKCATT VK3LIT  Subject 1${stringUtils.EOL}" +
                "* 2   01-01 10:16   VK2VRO  VK3LIT  Subject 2${stringUtils.EOL}" +
                "* 3   01-01 12:30   VK3LIT  VK2VRO  Subject 3${stringUtils.EOL}" +
                "* 4   02-01 13:46   PAKCATT VK3LIT  Subject 4${stringUtils.EOL}",
                response.responseString())
    }

    fun `test mail list with limit async`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest("mail.list.2", false)
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("* 3   01-01 12:30   VK3LIT  VK2VRO  Subject 3${stringUtils.EOL}" +
                "* 4   02-01 13:46   PAKCATT VK3LIT  Subject 4${stringUtils.EOL}",
            response.responseString())
    }

    fun `test mail list with unread limit async`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest("mail.list.unread.2", false)
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("* 3   01-01 12:30   VK3LIT  VK2VRO  Subject 3${stringUtils.EOL}" +
                "* 4   02-01 13:46   PAKCATT VK3LIT  Subject 4${stringUtils.EOL}",
            response.responseString())
    }

    @Test
    fun `test sending a mail message with existing messages`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest("mail")

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Mail${stringUtils.EOL}mail> ", response.responseString())

        request = testRequest("send vk3lit")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals("${stringUtils.EOL}Subject: ", response.responseString())

        request = testRequest("TEst")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals("Compose your message and finish with . on a line of it's own.${stringUtils.EOL}", response.responseString())

        request = testRequest("CR LF test.")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals("${stringUtils.EOL}", response.responseString())

        request = testRequest(".")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals("Thanks. Your message has been stored.\nmail> ", response.responseString())
    }

    @Test
    fun `test sending a mail message with no messages`() {
        `test starting a connection to the BBS with no messages`()
        var request = testRequest("mail")

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Mail${stringUtils.EOL}mail> ", response.responseString())

        request = testRequest("send vk3lit")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals("${stringUtils.EOL}Subject: ", response.responseString())

        request = testRequest("TEst")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals("Compose your message and finish with . on a line of it's own.${stringUtils.EOL}", response.responseString())

        request = testRequest("CR LF test.")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals("${stringUtils.EOL}", response.responseString())

        request = testRequest(".")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals("Thanks. Your message has been stored.${stringUtils.EOL}mail> ", response.responseString())
    }

}