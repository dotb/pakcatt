package pakcatt.application.bulletinboard.persistence

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*

class MockedBulletinBoardPostRepository: BulletinBoardPostRepository {
    override fun findByThreadNumber(threadNumber: Int): List<BulletinBoardPost> {
        return mutableListOf(BulletinBoardPost("VK2VRO", Date(0), "", 1, 1),
                        BulletinBoardPost("VK3LIT", Date(0), "", 1, 2),
                        BulletinBoardPost("PACKATT", Date(0), "", 1, 2))
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