package pakcatt.application.settings.persistence

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

class MockedSettingStoreRepository: SettingsRepository {
    override fun findBySettingOwnerAndKey(settingOwner: String, key: String): List<UserSetting> {
        return listOf(UserSetting("EOL", "CRLF", emptyList(), true, "VK3LIT"))
    }

    override fun findBySettingOwner(settingOwner: String): List<UserSetting> {
        return listOf(UserSetting("EOL", "CRLF", emptyList(), true, "VK3LIT"))
    }

    override fun <S : UserSetting?> save(p0: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : UserSetting?> saveAll(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findById(p0: Int): Optional<UserSetting> {
        TODO("Not yet implemented")
    }

    override fun existsById(p0: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun findAll(): MutableList<UserSetting> {
        TODO("Not yet implemented")
    }

    override fun findAll(p0: Sort): MutableList<UserSetting> {
        TODO("Not yet implemented")
    }

    override fun <S : UserSetting?> findAll(p0: Example<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun <S : UserSetting?> findAll(p0: Example<S>, p1: Sort): MutableList<S> {
        TODO("Not yet implemented")
    }

    override fun findAll(p0: Pageable): Page<UserSetting> {
        TODO("Not yet implemented")
    }

    override fun <S : UserSetting?> findAll(p0: Example<S>, p1: Pageable): Page<S> {
        TODO("Not yet implemented")
    }

    override fun findAllById(p0: MutableIterable<Int>): MutableIterable<UserSetting> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun <S : UserSetting?> count(p0: Example<S>): Long {
        TODO("Not yet implemented")
    }

    override fun deleteById(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun delete(p0: UserSetting) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(p0: MutableIterable<UserSetting>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun <S : UserSetting?> findOne(p0: Example<S>): Optional<S> {
        TODO("Not yet implemented")
    }

    override fun <S : UserSetting?> exists(p0: Example<S>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <S : UserSetting?> insert(p0: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : UserSetting?> insert(p0: MutableIterable<S>): MutableList<S> {
        TODO("Not yet implemented")
    }

}