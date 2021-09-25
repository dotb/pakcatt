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
                "* 1\t01 Jan 10:00  PAKCATT\tVK3LIT\tSubject 1${stringUtils.EOL}" +
                "* 2\t01 Jan 10:16  VK2VRO\tVK3LIT\tSubject 2${stringUtils.EOL}" +
                "* 3\t01 Jan 12:30  VK3LIT\tVK2VRO\tSubject 3${stringUtils.EOL}" +
                "3 messages${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "mail> ", response.responseString())
    }

}