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
                "  No\tUpdated       By\tTopic${EOL}" +
                "  1\t01 Jan 10:00  VK2VRO\tThis is to...${EOL}" +
                "  2\t01 Jan 10:00  VK3LIT\tThis is to...${EOL}" +
                "  3\t01 Jan 10:00  PAKCATT\tThis is to...${EOL}" +
                "  4\t01 Jan 10:00  VK2VRO\tThis is to...${EOL}" +
                "  5\t01 Jan 10:00  VK3LIT\tThis is to...${EOL}" +
                "5 threads${EOL}" +
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
                "No\tUpdated       By\tMessage${EOL}" +
                "1\t01 Jan 10:00  VK2VRO\t${EOL}" +
                "2\t01 Jan 10:00  VK3LIT\t${EOL}" +
                "2\t01 Jan 10:00  PACKATT\t${EOL}" +
                "3 posts${EOL}" +
                "${EOL}" +
                "board/1 This is topic 1> ", response.responseString())
    }

}