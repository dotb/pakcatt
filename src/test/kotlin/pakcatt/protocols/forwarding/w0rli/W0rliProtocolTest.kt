package pakcatt.protocols.forwarding.w0rli

import junit.framework.TestCase
import org.junit.Test
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType
import pakcatt.util.ByteUtils

class W0rliProtocolTest: AppServiceTest() {

    private val byteUtils = ByteUtils()

    @Test
    fun `test connect and get forward prompt`() {
        `test start a connection for forwarding`()
        // Send an SID
        sendMessageAndTestResult( "[appname-0.1-M$]", "> ${stringUtils.EOL}")
        // Offer a message that is accepted
        sendMessageAndTestResult( "SP VK3AT @ VK3AT.#MEL.VIC.AU.OC < VK3LE", "OK${stringUtils.EOL}")
        sendMessageAndTestResult( "message subject${stringUtils.EOL}message routing headers${stringUtils.EOL}message body${stringUtils.EOL}" + byteUtils.intToByte(0x1A), "> ${stringUtils.EOL}")
        // Offer a message that is rejected
        sendMessageAndTestResult( "SP VK2IO @ VK2IO.#SYD.NSW.AU.OC < VK3LE", "NO${stringUtils.EOL}> ${stringUtils.EOL}")
        // Do you have messages for me?
        sendMessageAndTestResult( "F>", "SP PAKCAT @ PAKCAT.#MEL.VIC.AU.OC < VK3AT")
        // Accept the message
        sendMessageAndTestResult( "OK", "message subject${stringUtils.EOL}message routing headers${stringUtils.EOL}message body${stringUtils.EOL}" + byteUtils.intToByte(0x1A))
        // Any other messages for us?
        var request = testRequest()
        request.message = "F>"
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.DISCONNECT, response.responseType)
    }


    private fun sendMessageAndTestResult(messageSent: String, expectedResult: String) {
        var request = testRequest()
        request.message = messageSent
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals(expectedResult, response.responseString())
    }


    @Test
    private fun `test start a connection for forwarding`() {
        mockedMailMessageRepository.messageCount = 3
        val request = testRequest()
        val response = appService.getDecisionOnConnectionRequest(request)
        TestCase.assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        TestCase.assertEquals(
            "[PAKCATT-0.3-M\$]${stringUtils.EOL}Welcome${stringUtils.EOL}You have 2 unread messages.${stringUtils.EOL}${stringUtils.EOL}menu> ",
            response.responseString()
        )
    }

}