package pakcatt.application.mailbox.edit

import pakcatt.application.mailbox.persistence.MailMessage
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.shared.SubApp
import pakcatt.network.packet.protocol.no_layer_three.model.LinkRequest
import pakcatt.network.packet.protocol.no_layer_three.model.LinkResponse

class EditSubjectApp(private val mailMessage: MailMessage, private val mailboxStore: MailboxStore): SubApp() {

    override fun returnCommandPrompt(): String {
        return "Subject:"
    }

    override fun handleReceivedMessage(request: LinkRequest): LinkResponse {
        mailMessage.subject = stringUtils.removeEOLChars(request.message)
        return LinkResponse.sendText("Compose your message and finish with . on a line of it's own.", EditBodyApp(mailMessage, mailboxStore))
    }

}