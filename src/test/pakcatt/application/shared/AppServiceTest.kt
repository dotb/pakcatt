package pakcatt.application.shared

import junit.framework.TestCase
import org.junit.Test
import pakcatt.application.bulletinboard.persistence.BulletinBoardStore
import pakcatt.application.bulletinboard.persistence.MockedBulletinBoardPostRepository
import pakcatt.application.bulletinboard.persistence.MockedBulletinBoardThreadRepository
import pakcatt.application.last.persistence.LastEntryStore
import pakcatt.application.last.persistence.MockedLastEntryRepository
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.mailbox.persistence.MockedMailMessageRepository
import pakcatt.application.mainmenu.MainMenuApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.ResponseType
import pakcatt.util.StringUtils

open class AppServiceTest: TestCase() {

    protected val mainMenuApp = MainMenuApp("PAKCATT",
                                    MailboxStore(MockedMailMessageRepository()),
                                    BulletinBoardStore(MockedBulletinBoardThreadRepository(), MockedBulletinBoardPostRepository()),
                                    LastEntryStore(MockedLastEntryRepository()),
                                    "Welcome",
                                    20,
                                    80,
                                    2)

    protected val stringUtils = StringUtils()
    protected val appService = AppService(listOf(mainMenuApp))
    protected val escapeChar = 27.toChar()
    protected val startBold = "${escapeChar}[1m"
    protected val resetFormat = "${escapeChar}[0m"

    @Test
    fun `test starting a connection to the BBS`() {
        val request = testRequest()

        val response = appService.getDecisionOnConnectionRequest(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Welcome${stringUtils.EOL}You have 2 unread messages.${stringUtils.EOL}${stringUtils.EOL}menu> ", response.responseString())
    }

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

    protected fun testRequest(command: String = ""): AppRequest {
        return AppRequest("VK3LIT-1", "VK3LIT", "PAKCATT", command, "", "", true)
    }

}