package pakcatt.application.mailbox.edit

import pakcatt.application.mailbox.MailMessage
import pakcatt.application.mailbox.MailboxStore
import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.util.StringUtils

class EditBodyApp(private val mailMessage: MailMessage, private val mailboxStore: MailboxStore): SubApp() {

    private val stringUtils = StringUtils()
    private val eol = "\r\n"

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: LinkRequest): InteractionResponse {
        val chompedBody = stringUtils.chompString(request.message)
        return if (chompedBody == ".") { // Finish editing the body
            mailboxStore.storeMessage(mailMessage)
            InteractionResponse.sendText("Thanks! Your message has been stored.", NavigateBack(2))
        } else {
            mailMessage.body.append(chompedBody)
            mailMessage.body.append(eol)
            InteractionResponse.sendText("")
        }
    }

}