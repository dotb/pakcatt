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
import pakcatt.application.filter.LastAppInputFilter
import pakcatt.application.filter.MentionOutputFilter
import pakcatt.application.last.LastApp
import pakcatt.util.StringUtils

open class AppServiceTest: TestCase() {

    protected val mockedLastEntryRepository = MockedLastEntryRepository()
    protected val lastEntryStore = LastEntryStore(mockedLastEntryRepository)
    protected val mockedMailMessageRepository = MockedMailMessageRepository()
    protected val mailboxStore = MailboxStore(mockedMailMessageRepository)
    protected val mockedBulletinBoardThreadRepository = MockedBulletinBoardThreadRepository()
    protected val mockedBulletinBoardPostRepository = MockedBulletinBoardPostRepository()
    protected val bulletinBoardStore = BulletinBoardStore(mockedBulletinBoardThreadRepository, mockedBulletinBoardPostRepository)
    protected val boardPromptTopicLength = 20
    protected val boardSummaryLength = 80
    protected val boardPostListLength = 2


    protected val mainMenuApp = MainMenuApp("PAKCATT",
                                    mailboxStore,
                                    bulletinBoardStore,
                                    lastEntryStore,
                                    "Welcome",
                                    boardPromptTopicLength,
                                    boardSummaryLength,
                                    boardPostListLength)

    protected val stringUtils = StringUtils()
    protected val appService = AppService(listOf(mainMenuApp),
                                            listOf(EOLInputFilter(), LastAppInputFilter(lastEntryStore)),
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

    protected fun testRequest(command: String = "", channelIsInteractive: Boolean = true): AppRequest {
        return AppRequest("VK3LIT-1", "VK3LIT", "PAKCATT", command, channelIsInteractive, "", "", true)
    }

}