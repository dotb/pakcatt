package pakcatt.application.mailbox.edit

import pakcatt.application.mailbox.persistence.MailMessage
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.shared.SubApp
import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.util.StringUtils

class EditSubjectApp(private val mailMessage: MailMessage, private val mailboxStore: MailboxStore): SubApp() {

    private val stringUtils = StringUtils()

    override fun returnCommandPrompt(): String {
        return "Subject:"
    }

    override fun handleReceivedMessage(request: LinkRequest): InteractionResponse {
        mailMessage.subject = stringUtils.chompString(request.message)
        return InteractionResponse.sendText("Compose your message and finish with . on a line of it's own.", EditBodyApp(mailMessage, mailboxStore))
    }

}