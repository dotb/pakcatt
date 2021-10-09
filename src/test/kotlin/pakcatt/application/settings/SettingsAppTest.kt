package pakcatt.application.settings

import junit.framework.TestCase
import org.junit.Test
import pakcatt.application.shared.AppServiceTest
import pakcatt.application.shared.model.ResponseType

class SettingsAppTest: AppServiceTest() {

    @Test
    fun `test get settings`() {
        `test starting a connection to the BBS with messages`()
        mockedSettingStoreRepository.setSingleValueInDatabase()
        var request = testRequest("settings")
        var response = appService.getResponseForReceivedMessage(request)

        request = testRequest("list")
        response = appService.getResponseForReceivedMessage(request)
        TestCase.assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        TestCase.assertEquals("${stringUtils.EOL}" +
                "${startBold}Setting Name\tIs User-configurable?\tSetting Value${resetFormat}${stringUtils.EOL}" +
                "EOL\tY\tCRLF${stringUtils.EOL}" +
                "settings> ", response.responseString())
    }

    @Test
    fun `test set settings and reading back`() {
        `test starting a connection to the BBS with messages`()
        mockedSettingStoreRepository.resetInMemoryDatabase()
        var request = testRequest("settings")
        var response = appService.getResponseForReceivedMessage(request)

        request = testRequest("set EOL LF")
        response = appService.getResponseForReceivedMessage(request)

        TestCase.assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        TestCase.assertEquals(
                "Setting saved" +
                "${stringUtils.EOL}" +
                "settings> ", response.responseString())

        request = testRequest("list")
        response = appService.getResponseForReceivedMessage(request)

        TestCase.assertEquals(ResponseType.ACK_WITH_TEXT, response.responseType)
        TestCase.assertEquals("${stringUtils.EOL}" +
                "${startBold}Setting Name\tIs User-configurable?\tSetting Value${resetFormat}${stringUtils.EOL}" +
                "EOL\tY\tLF${stringUtils.EOL}" +
                "settings> ", response.responseString())
    }

}