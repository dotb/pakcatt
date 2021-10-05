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
import pakcatt.application.filter.EOLInputFilter
import pakcatt.application.filter.EOLOutputFilter
import pakcatt.application.filter.MentionOutputFilter
import pakcatt.application.settings.persistence.MockedSettingStoreRepository
import pakcatt.application.settings.persistence.SettingStore
import pakcatt.util.StringUtils

open class AppServiceTest: TestCase() {

    protected val mockedMailMessageRepository = MockedMailMessageRepository()
    protected val mockedLastEntryRepository = MockedLastEntryRepository()
    protected val mockedSettingStoreRepository = MockedSettingStoreRepository()


    protected val mainMenuApp = MainMenuApp("PAKCATT",
                                    MailboxStore(mockedMailMessageRepository),
                                    BulletinBoardStore(MockedBulletinBoardThreadRepository(), MockedBulletinBoardPostRepository()),
                                    LastEntryStore(mockedLastEntryRepository),
                                    "Welcome",
                                    20,
                                    80,
                                    2,
                                     SettingStore(mockedSettingStoreRepository))

    protected val stringUtils = StringUtils()
    protected val appService = AppService(listOf(mainMenuApp),
    listOf(EOLInputFilter()),
    listOf(EOLOutputFilter(), MentionOutputFilter()))
    protected val escapeChar = 27.toChar()
    protected val startBold = "${escapeChar}[1m"
    protected val resetFormat = "${escapeChar}[0m"

    @Test
    fun `test starting a connection to the BBS with messages`() {
        mockedMailMessageRepository.messageCount = 3
        val request = testRequest()

        val response = appService.getDecisionOnConnectionRequest(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Welcome${stringUtils.EOL}You have 2 unread messages.${stringUtils.EOL}${stringUtils.EOL}menu> ", response.responseString())
    }

    @Test
    fun `test starting a connection to the BBS with no messages`() {
        mockedMailMessageRepository.messageCount = 0
        val request = testRequest()

        val response = appService.getDecisionOnConnectionRequest(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Welcome${stringUtils.EOL}${stringUtils.EOL}menu> ", response.responseString())
    }

    @Test
    fun `test starting a connection to the BBS with one messages`() {
        mockedMailMessageRepository.messageCount = 1
        val request = testRequest()

        val response = appService.getDecisionOnConnectionRequest(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Welcome${stringUtils.EOL}You have an unread message.${stringUtils.EOL}${stringUtils.EOL}menu> ", response.responseString())
    }

    protected fun testRequest(command: String = ""): AppRequest {
        return AppRequest("VK3LIT-1", "VK3LIT", "PAKCATT", command, "", "", true)
    }

}