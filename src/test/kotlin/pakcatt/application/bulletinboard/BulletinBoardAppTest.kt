package pakcatt.application.bulletinboard


import org.junit.Test
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType

class BulletinBoardAppTest: AppServiceTest() {

    @Test
    fun `test open the board and list threads`() {
        `test starting a connection to the BBS with messages`()
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
                "${startBold}  No  Updated       Topic${resetFormat}${stringUtils.EOL}" +
                "  2   01 Jan 10:00  This is topic 2${stringUtils.EOL}" +
                "  3   01 Jan 10:00  This is topic 3${stringUtils.EOL}" +
                "  5   01 Jan 10:00  This is topic 5${stringUtils.EOL}" +
                "  6   01 Jan 10:00  This is topic 6${stringUtils.EOL}" +
                "  7   01 Jan 10:00  This is topic 7${stringUtils.EOL}" +
                "  8   01 Jan 10:00  This is topic 8${stringUtils.EOL}" +
                "  9   01 Jan 10:00  This is topic 9${stringUtils.EOL}" +
                "  10  01 Jan 10:00  This is topic 10${stringUtils.EOL}" +
                "  1   01 Jan 12:46  This is topic 1${stringUtils.EOL}" +
                "  4   10 Jul 02:00  This is topic 4${stringUtils.EOL}" +
                "10 threads${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "board> ", response.responseString())
    }

    @Test
    fun `test open the board and then a topic and list posts with a default length`() {
        `test starting a connection to the BBS with messages`()
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
                "${startBold}  No  Posted        By     Size${resetFormat}${stringUtils.EOL}" +
                "${startBold}  1   01 Jan 10:16  VK2VRO 567B${resetFormat}${stringUtils.EOL}" +
                "Sed ut perspiciatis${stringUtils.EOL}" +
                "unde omnis iste natus${stringUtils.EOL}" +
                "error sit voluptatem accusantium${stringUtils.EOL}" +
                "dolor...${stringUtils.EOL}${stringUtils.EOL}" +
                "${startBold}  2   01 Jan 10:33  PACKATT567B${resetFormat}${stringUtils.EOL}" +
                "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium dolor...${stringUtils.EOL}${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "3 posts in: This is topic 1${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "board/1 This is topic 1> ", response.responseString())
    }

    @Test
    fun `test open the board and then a topic and list posts with a specified length`() {
        `test starting a connection to the BBS with messages`()
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
                "${startBold}  No  Posted        By     Size${resetFormat}${stringUtils.EOL}" +
                "${startBold}  2   01 Jan 10:33  PACKATT567B${resetFormat}${stringUtils.EOL}" +
                "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium dolor...${stringUtils.EOL}${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "3 posts in: This is topic 1${stringUtils.EOL}" +
                "${stringUtils.EOL}" +
                "board/1 This is topic 1> ", response.responseString())
    }

    @Test
    fun `test open a topic and read a post that exists and one that does not exist`() {
        `test starting a connection to the BBS with messages`()
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
        request = testRequest("open 0")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque" +
                "${stringUtils.EOL}laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore" +
                "${stringUtils.EOL}veritatis et $startBold@VK3LIT$resetFormat architecto beatae vitae dicta sunt explicabo." +
                "${stringUtils.EOL}Nemo enim ipsam voluptatem quia @VK2VRO sit aspernatur aut odit aut fugit, sed quia" +
                "${stringUtils.EOL}consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est," +
                "${stringUtils.EOL}qui dolorem ipsum quia dolor sit amet, consectetur,adipisci velit, sed quia non numquam eius" +
                "${stringUtils.EOL}modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem." +
                "${stringUtils.EOL}board/1 This is topic 1> ", response.responseString())

        // We should be able to read the last post
        request = testRequest("open 2")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem." +
                "${stringUtils.EOL}board/1 This is topic 1> ", response.responseString())

        // Try read a post that does not exist
        request = testRequest("open 3")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Post not found${stringUtils.EOL}" +
                "board/1 This is topic 1> ", response.responseString())

    }

    @Test
    fun `test help command shows help`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest("board")
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Bulletin Board${stringUtils.EOL}board> ", response.responseString())

        request = testRequest("help")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}help\t- Display this list of commands" +
                                "${stringUtils.EOL}list\t- List the threads" +
                                "${stringUtils.EOL}open\t- Open a thread" +
                                "${stringUtils.EOL}post\t- Post a new thread" +
                                "${stringUtils.EOL}back\t- Return to the main menu" +
                                "${stringUtils.EOL}${stringUtils.EOL}board> ", response.responseString())
    }

    @Test
    fun `test unknown command`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest("board")
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Bulletin Board${stringUtils.EOL}board> ", response.responseString())

        request = testRequest("unknown")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Say what? Type help for, help${stringUtils.EOL}board> ", response.responseString())
    }

    @Test
    fun `test synonyms for exiting a context`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest("board")
        var response = appService.getResponseForReceivedMessage(request)

        request = testRequest("exit")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Bye${stringUtils.EOL}menu> ", response.responseString())


        request = testRequest("board")
        response = appService.getResponseForReceivedMessage(request)
        request = testRequest("quit")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Bye${stringUtils.EOL}menu> ", response.responseString())
    }

    @Test
    fun `test open the board and list topics from async device`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest("board.list", false)
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("2 01-01 10:00 This is topic 2${stringUtils.EOL}" +
                "3 01-01 10:00 This is topic 3${stringUtils.EOL}" +
                "5 01-01 10:00 This is topic 5${stringUtils.EOL}" +
                "6 01-01 10:00 This is topic 6${stringUtils.EOL}" +
                "7 01-01 10:00 This is topic 7${stringUtils.EOL}" +
                "8 01-01 10:00 This is topic 8${stringUtils.EOL}" +
                "9 01-01 10:00 This is topic 9${stringUtils.EOL}" +
                "10 01-01 10:00 This is topic 10${stringUtils.EOL}" +
                "1 01-01 12:46 This is topic 1${stringUtils.EOL}" +
                "4 10-07 02:00 This is topic 4${stringUtils.EOL}"
                , response.responseString())
    }

    @Test
    fun `test open the board and list topics with a limit from async device`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest("board.list.4", false)
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("9 01-01 10:00 This is topic 9${stringUtils.EOL}" +
                "10 01-01 10:00 This is topic 10${stringUtils.EOL}" +
                "1 01-01 12:46 This is topic 1${stringUtils.EOL}" +
                "4 10-07 02:00 This is topic 4${stringUtils.EOL}"
            , response.responseString())
    }

    @Test
    fun `test open the board and then a topic and list posts with a default length from an async device`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest("board.open.1.list", false)
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("1 01-01 10:16 VK2VRO 567B Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium dolor...${stringUtils.EOL}" +
                "2 01-01 10:33 PACKATT 567B Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium dolor...${stringUtils.EOL}",
                response.responseString())
    }

    @Test
    fun `test open a topic and add a new post to it`() {
        `test starting a connection to the BBS with messages`()
        var request = testRequest("board")
        var response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("Launching Bulletin Board${stringUtils.EOL}board> ", response.responseString())

        request = testRequest("open 1")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}" +
                "board/1 This is topic 1> ", response.responseString())

        request = testRequest("post")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals("Compose your post and finish with . on a line of it's own.${stringUtils.EOL}", response.responseString())

        request = testRequest("This is a test")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals("${stringUtils.EOL}", response.responseString())

        request = testRequest(".")
        response = appService.getResponseForReceivedMessage(request)
        assertEquals("Thanks. Your post has been stored.${stringUtils.EOL}board/1 This is topic 1> ", response.responseString())
    }

}