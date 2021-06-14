package pakcatt.application.mailbox.edit

import pakcatt.application.mailbox.persistence.MailMessage
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.network.packet.protocol.no_layer_three.model.LinkRequest
import pakcatt.network.packet.protocol.no_layer_three.model.LinkResponse

class EditBodyApp(private val mailMessage: MailMessage, private val mailboxStore: MailboxStore): SubApp() {

    private var composedBody = StringBuilder()

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: LinkRequest): LinkResponse {
        val bodyText = request.message
        return if (stringUtils.removeEOLChars(bodyText) == ".") {
            // Finish editing the body
            mailMessage.body = composedBody.toString()
            mailboxStore.storeMessage(mailMessage)
            LinkResponse.sendText("Thanks. Your message has been stored.", NavigateBack(2))
        } else {
            composedBody.append(bodyText)
            LinkResponse.acknowledgeOnly()
        }
    }

}