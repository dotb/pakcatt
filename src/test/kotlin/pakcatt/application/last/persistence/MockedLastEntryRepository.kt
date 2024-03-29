package pakcatt.application.last.persistence

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import java.util.*
import java.util.function.Function

class MockedLastEntryRepository: LastEntryRepository {
    var lastEntryIsAvailable = true
    var lastInsertedEntry: LastEntry? = null

    override fun <S : LastEntry?> save(p0: S): S {
        lastInsertedEntry = p0
        return p0
    }

    override fun <S : LastEntry?> saveAll(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findById(p0: String): Optional<LastEntry> {
        return when (lastEntryIsAvailable) {
            false -> Optional.empty()
            true -> Optional.of(LastEntry("VK3LIT", Date(1000000), "144.875Mhz"))
        }
    }

    override fun existsById(p0: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun findAll(): MutableList<LastEntry> {
        return mutableListOf(LastEntry("VK3LIT", Date(1000000), "144.875Mhz"),
                LastEntry("VK2VRO", Date(10000000), "144.875Mhz"),
                LastEntry("VK3FUR", Date(100000000), "144.875Mhz"),
                LastEntry("VK3DUB", Date(50000000), "144.875Mhz"),
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

    override fun findAllById(ids: MutableIterable<String>): MutableList<LastEntry> {
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

    override fun deleteAllById(ids: MutableIterable<String>) {
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

    override fun <S : LastEntry?, R : Any?> findBy(
        example: Example<S>,
        queryFunction: Function<FluentQuery.FetchableFluentQuery<S>, R>
    ): R {
        TODO("Not yet implemented")
    }

    override fun <S : LastEntry?> insert(p0: S): S {
        lastInsertedEntry = p0
        return p0
    }

    override fun <S : LastEntry?> insert(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }
}