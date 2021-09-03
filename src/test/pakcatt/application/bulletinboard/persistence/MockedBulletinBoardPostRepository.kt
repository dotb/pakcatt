package pakcatt.application.bulletinboard.persistence

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*

class MockedBulletinBoardPostRepository: BulletinBoardPostRepository {
    override fun findByThreadNumber(threadNumber: Int): List<BulletinBoardPost> {
        return mutableListOf(BulletinBoardPost("VK2VRO", Date(0), "Sed ut perspiciatis" +
                                "\n\runde omnis iste natus" +
                                "\n\rerror sit voluptatem accusantium" +
                                "\n\rdoloremque laudantium, totam rem aperiam," +
                                "\n\reaque ipsa quae ab illo inventore veritatis et" +
                                "\n\rquasi architecto beatae vitae dicta sunt explicabo." +
                                "\n\rNemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.", 1, 1),
                        BulletinBoardPost("VK3LIT", Date(1000000), "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque" +
                                "\n\rlaudantium, totam rem aperiam, eaque ipsa quae ab illo inventore" +
                                "\n\rveritatis et quasi architecto beatae vitae dicta sunt explicabo." +
                                "\n\rNemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia" +
                                "\n\rconsequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est," +
                                "\n\rqui dolorem ipsum quia dolor sit amet, consectetur,adipisci velit, sed quia non numquam eius" +
                                "\n\rmodi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.", 1, 2),
                        BulletinBoardPost("PACKATT", Date(2000000), "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.", 1, 2))
    }

    override fun <S : BulletinBoardPost?> save(p0: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardPost?> saveAll(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findAll(): MutableList<BulletinBoardPost> {
        TODO("Not yet implemented")
    }

    override fun findAll(p0: Sort): MutableList<BulletinBoardPost> {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardPost?> findAll(p0: Example<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardPost?> findAll(p0: Example<S>, p1: Sort): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findAll(p0: Pageable): Page<BulletinBoardPost> {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardPost?> findAll(p0: Example<S>, p1: Pageable): Page<S> {
        TODO("Not yet implemented")
    }

    override fun findAllById(p0: MutableIterable<Int>): MutableIterable<BulletinBoardPost> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardPost?> count(p0: Example<S>): Long {
        TODO("Not yet implemented")
    }

    override fun delete(p0: BulletinBoardPost) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(p0: MutableIterable<BulletinBoardPost>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardPost?> findOne(p0: Example<S>): Optional<S> {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardPost?> exists(p0: Example<S>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardPost?> insert(p0: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardPost?> insert(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun deleteById(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun existsById(p0: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun findById(p0: Int): Optional<BulletinBoardPost> {
        TODO("Not yet implemented")
    }
}