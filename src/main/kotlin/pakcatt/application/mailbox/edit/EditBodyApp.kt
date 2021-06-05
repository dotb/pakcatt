package pakcatt.application.mailbox.edit

import pakcatt.application.mailbox.persistence.MailMessage
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.util.StringUtils

class EditBodyApp(private val mailMessage: MailMessage, private val mailboxStore: MailboxStore): SubApp() {

    private val stringUtils = StringUtils()
    private val eol = "\r\n"
    private var composedBody = StringBuilder()

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: LinkRequest): InteractionResponse {
        val chompedBody = stringUtils.removeEOLChars(request.message)
        return if (chompedBody == ".") { // Finish editing the body
            mailMessage.body = composedBody.toString()
            mailboxStore.storeMessage(mailMessage)
            InteractionResponse.sendText("Thanks. Your message has been stored.", NavigateBack(2))
        } else {
            composedBody.append(chompedBody)
            composedBody.append(eol)
            InteractionResponse.acknowledgeOnly()
        }
    }

}