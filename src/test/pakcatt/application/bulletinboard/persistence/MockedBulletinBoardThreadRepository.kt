package pakcatt.application.bulletinboard.persistence

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*

class MockedBulletinBoardThreadRepository: BulletinBoardThreadRepository {
    override fun <S : BulletinBoardThread?> save(p0: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardThread?> saveAll(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findAll(): MutableList<BulletinBoardThread> {
        return mutableListOf(BulletinBoardThread("VK2VRO", Date(0), Date(0), "This is topic 1", 1),
                                BulletinBoardThread("VK3LIT", Date(0), Date(0), "This is topic 2", 2),
                                BulletinBoardThread("PAKCATT", Date(0), Date(0), "This is topic 3", 3),
                                BulletinBoardThread("VK2VRO", Date(0), Date(0), "This is topic 4", 4),
                                BulletinBoardThread("VK3LIT", Date(0), Date(0), "This is topic 5", 5))
    }

    override fun findAll(p0: Sort): MutableList<BulletinBoardThread> {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardThread?> findAll(p0: Example<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardThread?> findAll(p0: Example<S>, p1: Sort): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findAll(p0: Pageable): Page<BulletinBoardThread> {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardThread?> findAll(p0: Example<S>, p1: Pageable): Page<S> {
        TODO("Not yet implemented")
    }

    override fun findAllById(p0: MutableIterable<Int>): MutableIterable<BulletinBoardThread> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardThread?> count(p0: Example<S>): Long {
        TODO("Not yet implemented")
    }

    override fun delete(p0: BulletinBoardThread) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(p0: MutableIterable<BulletinBoardThread>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardThread?> findOne(p0: Example<S>): Optional<S> {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardThread?> exists(p0: Example<S>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardThread?> insert(p0: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : BulletinBoardThread?> insert(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun deleteById(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun existsById(p0: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun findById(p0: Int): Optional<BulletinBoardThread> {
        return Optional.of(BulletinBoardThread("VK2VRO", Date(0), Date(0), "This is topic 1", 1))
    }
}