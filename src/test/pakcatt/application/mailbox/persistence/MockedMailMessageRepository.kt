package pakcatt.application.mailbox.persistence

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*

class MockedMailMessageRepository: MailMessageRepository {
    override fun findByFromCallsignOrToCallsign(fromCallsign: String, toCallsign: String): List<MailMessage> {
        return mutableListOf(
            MailMessage("PAKCATT", "VK3LIT", Date(0), "Subject 1", "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque" +
                    "\n\rlaudantium, totam rem aperiam, eaque ipsa quae ab illo inventore" +
                    "\n\rveritatis et quasi architecto beatae vitae dicta sunt explicabo." +
                    "\n\rNemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia" +
                    "\n\rconsequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est," +
                    "\n\rqui dolorem ipsum quia dolor sit amet, consectetur,adipisci velit, sed quia non numquam eius" +
                    "\n\rmodi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.", false, 1),
            MailMessage("VK2VRO", "VK3LIT", Date(1000000), "Subject 2", "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque" +
                    "\n\rlaudantium, totam rem aperiam, eaque ipsa quae ab illo inventore" +
                    "\n\rveritatis et quasi architecto beatae vitae dicta sunt explicabo." +
                    "\n\rNemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia" +
                    "\n\rconsequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est," +
                    "\n\rqui dolorem ipsum quia dolor sit amet, consectetur,adipisci velit, sed quia non numquam eius" +
                    "\n\rmodi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.", false, 2),
            MailMessage("VK3LIT", "VK2VRO", Date(9000000), "Subject 3", "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque" +
                    "\n\rlaudantium, totam rem aperiam, eaque ipsa quae ab illo inventore" +
                    "\n\rveritatis et quasi architecto beatae vitae dicta sunt explicabo." +
                    "\n\rNemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia" +
                    "\n\rconsequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est," +
                    "\n\rqui dolorem ipsum quia dolor sit amet, consectetur,adipisci velit, sed quia non numquam eius" +
                    "\n\rmodi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.", false, 3))
    }

    override fun <S : MailMessage?> save(p0: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : MailMessage?> saveAll(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findAll(): MutableList<MailMessage> {
        TODO("Not yet implemented")
    }

    override fun findAll(p0: Sort): MutableList<MailMessage> {
        TODO("Not yet implemented")
    }

    override fun <S : MailMessage?> findAll(p0: Example<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun <S : MailMessage?> findAll(p0: Example<S>, p1: Sort): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findAll(p0: Pageable): Page<MailMessage> {
        TODO("Not yet implemented")
    }

    override fun <S : MailMessage?> findAll(p0: Example<S>, p1: Pageable): Page<S> {
        TODO("Not yet implemented")
    }

    override fun findAllById(p0: MutableIterable<Int>): MutableIterable<MailMessage> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun <S : MailMessage?> count(p0: Example<S>): Long {
        TODO("Not yet implemented")
    }

    override fun delete(p0: MailMessage) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(p0: MutableIterable<MailMessage>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun <S : MailMessage?> findOne(p0: Example<S>): Optional<S> {
        TODO("Not yet implemented")
    }

    override fun <S : MailMessage?> exists(p0: Example<S>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <S : MailMessage?> insert(p0: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : MailMessage?> insert(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun deleteById(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun existsById(p0: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun findById(p0: Int): Optional<MailMessage> {
        TODO("Not yet implemented")
    }
}