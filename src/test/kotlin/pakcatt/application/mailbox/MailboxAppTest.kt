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
                "${startBold}  No\tDate          From\tTo\tSubject${resetFormat}${stringUtils.EOL}" +
                "  1\t01 Jan 10:00  PAKCATT\tVK3LIT\tSubject 1${stringUtils.EOL}" +
                "* 2\t01 Jan 10:16  VK2VRO\tVK3LIT\tSubject 2${stringUtils.EOL}" +
                "* 3\t01 Jan 12:30  VK3LIT\tVK2VRO\tSubject 3${stringUtils.EOL}" +
                "* 4\t02 Jan 13:46  PAKCATT\tVK3LIT\tSubject 4${stringUtils.EOL}" +
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
                "${startBold}  No\tDate          From\tTo\tSubject${resetFormat}${stringUtils.EOL}" +
                "* 2\t01 Jan 10:16  VK2VRO\tVK3LIT\tSubject 2${stringUtils.EOL}" +
                "* 3\t01 Jan 12:30  VK3LIT\tVK2VRO\tSubject 3${stringUtils.EOL}" +
                "* 4\t02 Jan 13:46  PAKCATT\tVK3LIT\tSubject 4${stringUtils.EOL}" +
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
        assertEquals("  1 01-01 10:00 PAKCATT->VK3LIT: Subject 1${stringUtils.EOL}" +
                "* 2 01-01 10:16 VK2VRO->VK3LIT: Subject 2${stringUtils.EOL}" +
                "* 3 01-01 12:30 VK3LIT->VK2VRO: Subject 3${stringUtils.EOL}" +
                "* 4 02-01 13:46 PAKCATT->VK3LIT: Subject 4${stringUtils.EOL}",
                response.responseString())
    }
}