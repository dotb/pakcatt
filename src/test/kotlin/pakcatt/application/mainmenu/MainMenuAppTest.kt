package pakcatt.application.mainmenu

import org.junit.Test
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType

class MainMenuAppTest: AppServiceTest() {

    @Test
    fun `test unknown command`() {
        `test starting a connection to the BBS`()
        val request = testRequest("unknown")
        val response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("¯\\_(ツ)_/¯ type help for commands${stringUtils.EOL}menu> ", response.responseString())
    }



    @Test
    fun `test help command`() {
        `test starting a connection to the BBS`()
        var request = testRequest("help")
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}help\t- Display this list of commands" +
                "${stringUtils.EOL}board\t- Open the Bulletin Board" +
                "${stringUtils.EOL}mail\t- Open your mailbox" +
                "${stringUtils.EOL}last\t- last [callsign] - See when others were last seen" +
                "${stringUtils.EOL}tell\t- tell <callsign> - Send a quick APRS message to someone." +
                "${stringUtils.EOL}sqrt\t- sqrt <number> - Calculate the square root of an argument" +
                "${stringUtils.EOL}hello\t- Just a friendly welcome :-)" +
                "${stringUtils.EOL}ping\t- I'll reply with a pong" +
                "${stringUtils.EOL}pong\t- I'll reply with a pong" +
                "${stringUtils.EOL}beep\t- Send a beep instruction to your terminal" +
                "${stringUtils.EOL}bold\t- Test the bold control character on your terminal" +
                "${stringUtils.EOL}styles\t- Test the styles supported by your terminal" +
                "${stringUtils.EOL}nop\t- I'll do nothing, just acknowledge your request" +
                "${stringUtils.EOL}ignore\t- I'll receive your command but won't acknowledge it." +
                "${stringUtils.EOL}settings\t- View your environment settings." +
                "${stringUtils.EOL}${stringUtils.EOL}menu> ", response.responseString())
    }

}