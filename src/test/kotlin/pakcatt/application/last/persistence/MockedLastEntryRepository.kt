package pakcatt.application.last.persistence

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*

class MockedLastEntryRepository: LastEntryRepository {

    var lastSingleEntry: LastEntry? = null

    override fun <S : LastEntry?> save(p0: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : LastEntry?> saveAll(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findById(p0: String): Optional<LastEntry> {
        val lastEntry = lastSingleEntry
        return when (lastEntry) {
            null -> Optional.empty()
            else -> Optional.of(lastEntry)
        }
    }

    override fun existsById(p0: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun findAll(): MutableList<LastEntry> {
        return mutableListOf(LastEntry("VK3LIT", Date(1000000)),
                LastEntry("VK2VRO", Date(10000000)),
                LastEntry("VK3FUR", Date(100000000)),
                LastEntry("VK3DUB", Date(50000000)),
                LastEntry("VK4XSS", Date(20000000)))
    }

    override fun findAll(p0: Sort): MutableList<LastEntry> {
        TODO("Not yet implemented")
    }

    override fun <S : LastEntry?> findAll(p0: Example<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun <S : LastEntry?> findAll(p0: Example<S>, p1: Sort): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findAll(p0: Pageable): Page<LastEntry> {
        TODO("Not yet implemented")
    }

    override fun <S : LastEntry?> findAll(p0: Example<S>, p1: Pageable): Page<S> {
        TODO("Not yet implemented")
    }

    override fun findAllById(p0: MutableIterable<String>): MutableIterable<LastEntry> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun <S : LastEntry?> count(p0: Example<S>): Long {
        TODO("Not yet implemented")
    }

    override fun deleteById(p0: String) {
        TODO("Not yet implemented")
    }

    override fun delete(p0: LastEntry) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(p0: MutableIterable<LastEntry>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun <S : LastEntry?> findOne(p0: Example<S>): Optional<S> {
        TODO("Not yet implemented")
    }

    override fun <S : LastEntry?> exists(p0: Example<S>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <S : LastEntry?> insert(p0: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : LastEntry?> insert(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }
}