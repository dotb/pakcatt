package pakcatt.application.bulletinboard.persistence

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import pakcatt.util.StringUtils
import java.util.*
import java.util.function.Function

class MockedBulletinBoardPostRepository: BulletinBoardPostRepository {

    private val stringUtils = StringUtils()
    override fun findByThreadNumber(threadNumber: Int): List<BulletinBoardPost> {
        return generateListOfPosts()
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
        return generateListOfPosts()
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

    override fun findAllById(ids: MutableIterable<Int>): MutableList<BulletinBoardPost> {
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

    override fun deleteAllById(ids: MutableIterable<Int>) {
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

    override fun <S : BulletinBoardPost?, R : Any?> findBy(
        example: Example<S>,
        queryFunction: Function<FluentQuery.FetchableFluentQuery<S>, R>
    ): R {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardPost?> insert(p0: S): S {
        return p0
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

    private fun generateListOfPosts(): MutableList<BulletinBoardPost> {
        return mutableListOf(
            BulletinBoardPost("VK3LIT", Date(0), "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque" +
                    "${stringUtils.EOL}laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore" +
                    "${stringUtils.EOL}veritatis et @VK3lit architecto beatae vitae dicta sunt explicabo." +
                    "${stringUtils.EOL}Nemo enim ipsam voluptatem quia @VK2VRO sit aspernatur aut odit aut fugit, sed quia" +
                    "${stringUtils.EOL}consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est," +
                    "${stringUtils.EOL}qui dolorem ipsum quia dolor sit amet, consectetur,adipisci velit, sed quia non numquam eius" +
                    "${stringUtils.EOL}modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.", 1, 0),
            BulletinBoardPost("VK2VRO", Date(1000000), "Sed ut perspiciatis" +
                    "${stringUtils.EOL}unde omnis iste natus" +
                    "${stringUtils.EOL}error sit voluptatem accusantium" +
                    "${stringUtils.EOL}doloremque laudantium, totam rem aperiam," +
                    "${stringUtils.EOL}eaque ipsa quae ab illo inventore veritatis et" +
                    "${stringUtils.EOL}quasi architecto beatae vitae dicta sunt explicabo." +
                    "${stringUtils.EOL}Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.", 1, 1),
            BulletinBoardPost("PACKATT", Date(2000000), "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.", 1, 2))
    }

}