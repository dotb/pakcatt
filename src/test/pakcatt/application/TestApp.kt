package pakcatt.application

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.application.shared.RootApp
import pakcatt.network.packet.link.model.LinkResponse
import pakcatt.util.StringUtils

@Component
@Profile("test")
class TestApp: RootApp() {

    companion object {
        const val longResponseString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis accumsan metus et ipsum tempus pharetra. Donec et erat at libero consequat vehicula et eget erat. Sed tortor velit, ullamcorper sit amet nisi a, viverra ornare nunc. Integer et turpis id arcu eleifend euismod nec ut quam. Fusce nec lectus suscipit, venenatis est at, tristique magna. Aliquam tempor laoreet convallis. Proin egestas et purus vel rhoncus. Proin auctor vehicula justo, non malesuada lacus dapibus eget. Nam felis ipsum, dictum ut nunc id, maximus faucibus orci. Donec nec eros blandit, tristique quam id, placerat tortor. Quisque consequat sem at augue elementum, et maximus eros lacinia. Nullam eu orci iaculis nisi eleifend tempor. Etiam id convallis urna. Mauris rhoncus neque nibh, vitae malesuada neque euismod eget. Pellentesque quis lectus turpis." +
                                        "${StringUtils.EOL}Nullam quam metus, ultricies a nisl ut, cursus porta arcu. Phasellus eu magna at lectus pretium rutrum. Nulla facilisi. Curabitur dapibus porttitor odio quis eleifend. Donec feugiat laoreet ante, a dictum dui consectetur a. Nullam sollicitudin vel metus et vulputate. Nullam ut maximus nisi. Aliquam gravida ultricies nibh quis luctus. Vestibulum dignissim, nibh vitae egestas ultricies, felis enim lobortis arcu, vitae rutrum mi risus quis dolor. Sed dolor nibh, tristique in nisl non, dignissim finibus felis. Integer efficitur nec ligula in rutrum. Pellentesque vehicula bibendum nisi quis blandit." +
                                        "${StringUtils.EOL}Sed consectetur sed dolor nec tempus. Vivamus semper sem mi, et malesuada nisi cursus id. Aenean nec sapien nisi. Sed id mauris dui. Vivamus fringilla posuere neque, vitae posuere nulla pretium eu. Curabitur odio neque, faucibus nec urna vel, vestibulum bibendum libero. Cras tincidunt non ligula tincidunt venenatis. Mauris commodo sagittis dui. Suspendisse lacinia non augue in gravida. Aliquam sed orci elit. Donec id suscipit arcu. Fusce mollis nisl eget tellus euismod, molestie tristique ipsum imperdiet. Etiam eget lorem vel sapien placerat ultricies." +
                                        "${StringUtils.EOL}Aliquam sit amet orci augue. Donec luctus posuere lorem quis malesuada. Nam at ex a odio fringilla vestibulum id eget dolor. Maecenas tempus, ex in gravida convallis, nisi neque tempor quam, nec venenatis mi libero eu ante. Donec eget nunc massa. Nulla tincidunt tortor sodales tincidunt porttitor. Mauris non consequat eros. Praesent facilisis ut leo at finibus. Morbi vestibulum enim dolor, quis ornare nibh tempor a."
    }

    override fun returnCommandPrompt(): String {
        return "test>"
    }

    override fun decisionOnConnectionRequest(request: LinkRequest): LinkResponse {
        return LinkResponse.acknowledgeOnly()
    }

    override fun handleReceivedMessage(request: LinkRequest): LinkResponse {
        return when (stringUtils.removeEOLChars(request.message)) {
            "nop" -> LinkResponse.acknowledgeOnly()
            "Hello!" -> LinkResponse.sendText("Hi, there! *wave*")
            "ping" -> LinkResponse.sendText("pong")
            "longtest" -> LinkResponse.sendText(largeResponse())
            else ->  LinkResponse.sendText("Test")
        }
    }

    private fun largeResponse(): String {
        return longResponseString
    }

}