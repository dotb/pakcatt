package pakcatt.protocols.forwarding.w0rli

import org.junit.Test
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType

class W0rliProtocolTest : AppServiceTest() {

    @Test
    fun `test connect and get foward prompt`() {
        `test starting a connection to the BBS with messages`()

        // Send an SID
        sendMessageAndTestResult( "[appname-0.1-M$]", "> ")
        // Offer a message that is accepted
        sendMessageAndTestResult( "SP VK3AT @ VK3AT.#MEL.VIC.AU.OC < VK3LE", "OK")
        sendMessageAndTestResult( "message subject\nmessage routing headers\nmessage body\n" + byteUtils.intToByte(0x1A), "> ")
        // Offer a message that is rejected
        sendMessageAndTestResult( "SP VK2IO @ VK2IO.#SYD.NSW.AU.OC < VK3LE", "NO\n> ")
        //
    }


    private fun sendMessageAndTestResult(messageSent: String, expectedResult: String) {
        var request = testRequest()
        request.message = messageSent
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals(expectedResult, response.responseString())
    }


}