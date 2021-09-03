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

open class AppServiceTest: TestCase() {

    protected val mainMenuApp = MainMenuApp("PAKCATT",
                                    MailboxStore(MockedMailMessageRepository()),
                                    BulletinBoardStore(MockedBulletinBoardThreadRepository(), MockedBulletinBoardPostRepository()),
                                    LastEntryStore(MockedLastEntryRepository()),
                                    "Welcome",
                                    20,
                                    80)

    protected val appService = AppService(listOf(mainMenuApp))
    protected val EOL = "\n\r"

    @Test
    fun testStartConnection() {
        val request = testRequest()

        val response = appService.getDecisionOnConnectionRequest(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Welcome${EOL}${EOL}menu> ", response.responseString())
    }

    protected fun testRequest(command: String = ""): AppRequest {
        return AppRequest("VK3LIT-1", "VK3LIT", "PAKCATT", command, "", "", true)
    }

}