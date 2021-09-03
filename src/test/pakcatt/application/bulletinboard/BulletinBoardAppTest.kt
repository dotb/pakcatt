package pakcatt.application.bulletinboard


import org.junit.Test
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType

class BulletinBoardAppTest: AppServiceTest() {


    @Test
    fun testOpenBoardAndListTopics() {
        var request = testRequest()
        request.message = "board"

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Bulletin Board${EOL}board> ", response.responseString())

        request = testRequest()
        request.message = "list"

        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${EOL}" +
                "  No  \tUpdated     \tTopic${EOL}" +
                "  2  \t01 Jan 10:00\tThis is topic 2${EOL}" +
                "  3  \t01 Jan 10:00\tThis is topic 3${EOL}" +
                "  5  \t01 Jan 10:00\tThis is topic 5${EOL}" +
                "  6  \t01 Jan 10:00\tThis is topic 6${EOL}" +
                "  7  \t01 Jan 10:00\tThis is topic 7${EOL}" +
                "  8  \t01 Jan 10:00\tThis is topic 8${EOL}" +
                "  9  \t01 Jan 10:00\tThis is topic 9${EOL}" +
                "  10  \t01 Jan 10:00\tThis is topic 10${EOL}" +
                "  1  \t01 Jan 12:46\tThis is topic 1${EOL}" +
                "  4  \t10 Jul 02:00\tThis is topic 4${EOL}" +
                "10 threads${EOL}" +
                "${EOL}" +
                "board> ", response.responseString())
    }

    @Test
    fun testOpenBoardThenTopicThenListPosts() {
        var request = testRequest("board")

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Bulletin Board\n\rboard> ", response.responseString())

        request = testRequest("open 1")

        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${EOL}" +
                "board/1 This is topic 1> ", response.responseString())


        request = testRequest("list")

        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${EOL}" +
                "No\tPosted       By\t\tSize${EOL}" +
                "---------------------------------${EOL}" +
                "1\t01 Jan 10:00  VK2VRO\t573B${EOL}" +
                "Sed ut perspiciatis${EOL}" +
                "unde omnis iste natus${EOL}" +
                "error sit voluptatem accusantium${EOL}" +
                "do...${EOL}${EOL}" +
                "---------------------------------${EOL}" +
                "2\t01 Jan 10:16  VK3LIT\t572B${EOL}" +
                "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium dolor...${EOL}${EOL}" +
                "---------------------------------${EOL}" +
                "2\t01 Jan 10:33  PACKATT\t567B${EOL}" +
                "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium dolor...${EOL}${EOL}" +
                "---------------------------------${EOL}" +
                "3 posts${EOL}" +
                "${EOL}" +
                "board/1 This is topic 1> ", response.responseString())
    }

}