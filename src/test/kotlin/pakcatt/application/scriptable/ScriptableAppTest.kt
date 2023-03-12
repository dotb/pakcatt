package pakcatt.application.scriptable

import junit.framework.TestCase
import org.junit.Test
import pakcatt.application.scriptable.model.Script
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens

class ScriptableAppTest : TestCase() {

    private val scriptWorkingDir = "./"
    private val scriptTimeout: Long = 2
    private val scriptableScripts = listOf(Script("Example Date", "scripts/test_date_connect.sh", "scripts/test_date_request.sh"))
    private val subject = ScriptableApp(scriptableScripts, scriptWorkingDir, scriptTimeout)

    @Test
    fun testConnectionRequestSuccessWithMessage() {
        val validConnectionRequest = AppRequest("144.875Mhz", "REM1C", "MYCALL","MYCALL", "date")
        val expectedResponse = AppResponse.sendText("The date is 1 Jan")
        val connectionResponse = subject.decisionOnConnectionRequest(validConnectionRequest)
        assertEquals(expectedResponse.responseType, connectionResponse.responseType)
        assertEquals(expectedResponse.responseString(), connectionResponse.responseString())
        assertEquals(expectedResponse.nextApp(), connectionResponse.nextApp())
    }

    @Test
    fun testConnectionRequestSuccessWithACK() {
        val validConnectionRequest = AppRequest("144.875Mhz", "REM1C", "MYCALL", "MYCALL", "ack")
        val expectedResponse = AppResponse.acknowledgeOnly()
        val connectionResponse = subject.decisionOnConnectionRequest(validConnectionRequest)
        assertEquals(expectedResponse.responseType, connectionResponse.responseType)
        assertEquals(expectedResponse.responseString(), connectionResponse.responseString())
        assertEquals(expectedResponse.nextApp(), connectionResponse.nextApp())
    }

    @Test
    fun testConnectionRequestSuccessWithIgnore() {
        val validConnectionRequest = AppRequest("144.875Mhz", "REM1C", "NOTMYCALL", "NOTMYCALL", "ignore me")
        val expectedResponse = AppResponse.ignore()
        val connectionResponse = subject.decisionOnConnectionRequest(validConnectionRequest)
        assertEquals(expectedResponse.responseType, connectionResponse.responseType)
        assertEquals(expectedResponse.responseString(), connectionResponse.responseString())
        assertEquals(expectedResponse.nextApp(), connectionResponse.nextApp())
    }

    @Test
    fun testMessageWithReponse() {
        val validConnectionRequest = AppRequest("144.875Mhz", "REM1C", "MYCALL", "MYCALL", "date")
        val expectedResponse = AppResponse.sendText("The date is 1 Jan")
        val connectionResponse = subject.handleReceivedMessage(validConnectionRequest, ParsedCommandTokens().parseCommandLine(validConnectionRequest.message))
        assertEquals(expectedResponse.responseType, connectionResponse.responseType)
        assertEquals(expectedResponse.responseString(), connectionResponse.responseString())
        assertEquals(expectedResponse.nextApp(), connectionResponse.nextApp())
    }

    @Test
    fun testMessageWithoutResponse() {
        val validConnectionRequest = AppRequest("144.875Mhz", "REM1C", "MYCALL", "MYCALL", "ack")
        val expectedResponse = AppResponse.acknowledgeOnly()
        val connectionResponse = subject.handleReceivedMessage(validConnectionRequest, ParsedCommandTokens().parseCommandLine(validConnectionRequest.message))
        assertEquals(expectedResponse.responseType, connectionResponse.responseType)
        assertEquals(expectedResponse.responseString(), connectionResponse.responseString())
        assertEquals(expectedResponse.nextApp(), connectionResponse.nextApp())
    }

    @Test
    fun testMessageThatIsIgnored() {
        val validConnectionRequest = AppRequest("144.875Mhz", "REM1C", "NOTMYCALL", "NOTMYCALL", "ignore me")
        val expectedResponse = AppResponse.ignore()
        val connectionResponse = subject.handleReceivedMessage(validConnectionRequest, ParsedCommandTokens().parseCommandLine(validConnectionRequest.message))
        assertEquals(expectedResponse.responseType, connectionResponse.responseType)
        assertEquals(expectedResponse.responseString(), connectionResponse.responseString())
        assertEquals(expectedResponse.nextApp(), connectionResponse.nextApp())
    }

}