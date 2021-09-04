package pakcatt.application.bulletinboard


import org.junit.Test
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType

class BulletinBoardAppTest: AppServiceTest() {


    @Test
    fun `test open the board and list topics`() {
        var request = testRequest()
        request.message = "board"

        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Bulletin Board${stringUtils.EOL}board> ", response.responseString())

        request = testRequest()
        request.message = "list"

        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}" +
                "${startBold}  No  \tUpdated     \tTopic${resetFormat}${stringUtils.EOL}" +
                "  2  \t01 Jan 10:00\tThis is topic 2${stringUtils.EOL}" +
                "  3  \t01 Jan 10:00\tThis is topic 3${stringUtils.EOL}" +
                "  5  \t01 Jan 10:00\tThis is topic 5${stringUtils.EOL}" +
                "  6  \t01 Jan 10:00\tThis is topic 6${stringUtils.EOL}" +
                "  7  \t01 Jan 10:00\tThis is topic 7${stringUtils.EOL}" +
                "  8  \t01 Jan 10:00\tThis is topic 8${stringUtils.EOL}" +
                "  9  \t01 Jan 10:00\tThis is topic 9${stringUtils.EOL}" +
                "  10  \t01 Jan 10:00\tThis is topic 10${stringUtils.EOL}" +
                "  1  \t01 Jan 12:46\tThis is topic 1${stringUtils.EOL}" +
                "  4  \t10 Jul 02:00\tThis is topic 4${stringUtils.EOL}" +
                "10 threads${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "board> ", response.responseString())
    }

    @Test
    fun `test open the board and then a topic and list posts with a default length`() {
        var request = testRequest("board")
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Bulletin Board${stringUtils.EOL}board> ", response.responseString())

        request = testRequest("open 1")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}" +
                "board/1 This is topic 1> ", response.responseString())

        request = testRequest("list")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}" +
                "${startBold}No\tPosted       By\t\tSize${resetFormat}${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "${startBold}1)\t01 Jan 10:16  VK2VRO\t573B${resetFormat}${stringUtils.EOL}" +
                "Sed ut perspiciatis${stringUtils.EOL}" +
                "unde omnis iste natus${stringUtils.EOL}" +
                "error sit voluptatem accusantium${stringUtils.EOL}" +
                "do...${stringUtils.EOL}${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "${startBold}2)\t01 Jan 10:33  PACKATT\t567B${resetFormat}${stringUtils.EOL}" +
                "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium dolor...${stringUtils.EOL}${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "3 posts in: This is topic 1${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "board/1 This is topic 1> ", response.responseString())
    }

    @Test
    fun `test open the board and then a topic and list posts with a specified length`() {
        var request = testRequest("board")
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Bulletin Board${stringUtils.EOL}board> ", response.responseString())

        request = testRequest("open 1")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}" +
                "board/1 This is topic 1> ", response.responseString())

        request = testRequest("list 1")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}" +
                "${startBold}No\tPosted       By\t\tSize${resetFormat}${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "${startBold}2)\t01 Jan 10:33  PACKATT\t567B${resetFormat}${stringUtils.EOL}" +
                "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium dolor...${stringUtils.EOL}${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "3 posts in: This is topic 1${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "board/1 This is topic 1> ", response.responseString())
    }

    @Test
    fun `test open a topic and read a post that exists and one that does not exist`() {
        var request = testRequest("board")
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Bulletin Board${stringUtils.EOL}board> ", response.responseString())

        request = testRequest("open 1")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}" +
                "board/1 This is topic 1> ", response.responseString())

        // Read a post that exists
        request = testRequest("read 0")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque" +
                "${stringUtils.EOL}laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore" +
                "${stringUtils.EOL}veritatis et quasi architecto beatae vitae dicta sunt explicabo." +
                "${stringUtils.EOL}Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia" +
                "${stringUtils.EOL}consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est," +
                "${stringUtils.EOL}qui dolorem ipsum quia dolor sit amet, consectetur,adipisci velit, sed quia non numquam eius" +
                "${stringUtils.EOL}modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem." +
                "${stringUtils.EOL}board/1 This is topic 1> ", response.responseString())

        // We should be able to read the last post
        request = testRequest("read 2")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem." +
                "${stringUtils.EOL}board/1 This is topic 1> ", response.responseString())

        // Try read a post that does not exist
        request = testRequest("read 3")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Post not found${stringUtils.EOL}" +
                "board/1 This is topic 1> ", response.responseString())

    }

}