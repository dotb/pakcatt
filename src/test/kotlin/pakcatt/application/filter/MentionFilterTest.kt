package pakcatt.application.filter

import org.junit.Test
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType

class MentionFilterTest: AppServiceTest() {

    @Test
    fun `test callsign mentions are highlighted`() {
        var request = testRequest("board")
        appService.getResponseForReceivedMessage(request)

        request = testRequest("open 1")
        appService.getResponseForReceivedMessage(request)

        request = testRequest("open 0")
        val response = appService.getResponseForReceivedMessage(request)

        assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        assertEquals("${stringUtils.EOL}Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque" +
                "${stringUtils.EOL}laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore" +
                "${stringUtils.EOL}veritatis et $startBold@VK3LIT$resetFormat architecto beatae vitae dicta sunt explicabo." +
                "${stringUtils.EOL}Nemo enim ipsam voluptatem quia @VK2VRO sit aspernatur aut odit aut fugit, sed quia" +
                "${stringUtils.EOL}consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est," +
                "${stringUtils.EOL}qui dolorem ipsum quia dolor sit amet, consectetur,adipisci velit, sed quia non numquam eius" +
                "${stringUtils.EOL}modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem." +
                "${stringUtils.EOL}board/1 This is topic 1> ", response.responseString())
    }

}