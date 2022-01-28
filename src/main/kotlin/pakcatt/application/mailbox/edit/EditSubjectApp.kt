package pakcatt.application.mailbox.edit

import pakcatt.application.mailbox.persistence.MailMessage
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens

class EditSubjectApp(private val mailMessage: MailMessage, private val mailboxStore: MailboxStore): SubApp() {

    override fun returnCommandPrompt(): String {
        return "Subject:"
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        mailMessage.subject = stringUtils.removeEOLChars(request.message)
        return AppResponse.sendText("Compose your message and finish with . on a line of it's own.", EditBodyApp(mailMessage, mailboxStore))
    }

}