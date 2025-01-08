package it.pagopa.wallet.eventdispatcher.utils

import java.time.Duration
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate

/**
 * This class is a [RedisTemplate] wrapper class, used to centralize commons RedisTemplate
 * operations
 *
 * @param <V> - the RedisTemplate value type </V>
 */
abstract class RedisTemplateWrapper<V>(
    private val redisTemplate: RedisTemplate<String, V>,
    private val keyspace: String,
    private val defaultTTL: Duration
) {

    /**
     * Save the input entity into Redis. The entity TTL will be set to the default configured one
     *
     * @param value - the entity to be saved
     * @param ttl - the TTL to be used for save operation (with default ttl as value)
     */
    fun save(value: V, ttl: Duration = defaultTTL) {
        redisTemplate.opsForValue()[compoundKeyWithKeyspace(getKeyFromEntity(value)), value] = ttl
    }

    /**
     * Save key to hold the string value if key is absent (SET with NX).
     *
     * @param value the entity to be saved
     * @param ttl the TTL for the entity to be saved. This parameter will override the default TTL
     *   value
     * @return returns false if it already exists, true if it does not exist.
     */
    fun saveIfAbsent(value: V, ttl: Duration = defaultTTL): Boolean? {
        return redisTemplate
            .opsForValue()
            .setIfAbsent(compoundKeyWithKeyspace(getKeyFromEntity(value)), value, ttl)
    }

    /**
     * Retrieve entity for the given key
     *
     * @param key - the key of the entity to be found
     * @return the found entity, if any
     */
    fun findById(key: String): V? {
        return redisTemplate.opsForValue()[compoundKeyWithKeyspace(key)]
    }

    /**
     * Delete the entity for the given key
     *
     * @param key - the entity key to be deleted
     * @return true if the key has been removed
     */
    fun deleteById(key: String): Boolean {
        return redisTemplate.delete(compoundKeyWithKeyspace(key))
    }

    /**
     * Write an event to the stream with the specified key
     *
     * @param streamKey the stream key where send the event to
     * @param event the event to be sent
     * @return the [RecordId] associated to the written event
     */
    fun writeEventToStream(streamKey: String, event: V): RecordId? {
        return redisTemplate.opsForStream<Any, Any>().add(ObjectRecord.create(streamKey, event))
    }

    /**
     * Write an event to the stream with the specified key trimming events before writing the new
     * events so that stream has the wanted size
     *
     * @param streamKey the stream key where send the event to
     * @param event the event to be sent
     * @param streamSize the wanted length of the stream
     * @return the [RecordId] associated to the written event
     */
    fun writeEventToStreamTrimmingEvents(streamKey: String, event: V, streamSize: Long): RecordId? {
        require(streamSize >= 0) { "Invalid input $streamSize events to trim, it must be >=0" }
        redisTemplate.opsForStream<Any, Any>().trim(streamKey, streamSize)
        return redisTemplate.opsForStream<Any, Any>().add(ObjectRecord.create(streamKey, event))
    }

    /**
     * Trim events from the stream with input key to the wanted size
     *
     * @param streamKey the stream key from which trim events
     * @param streamSize the wanted stream size
     * @return the number or removed events from the stream
     */
    fun trimEvents(streamKey: String, streamSize: Long): Long? {
        return redisTemplate.opsForStream<Any, Any>().trim(streamKey, streamSize)
    }

    /**
     * Acknowledge input record ids for group inside streamKey stream
     *
     * @param streamKey the stream key
     * @param groupId the group id for which perform acknowledgment operation
     * @param recordIds records for which perform ack operation
     * @return the number of stream operations performed
     */
    fun acknowledgeEvents(streamKey: String, groupId: String, recordIds: Set<String>): Long? {
        return redisTemplate
            .opsForStream<Any, Any>()
            .acknowledge(streamKey, groupId, *recordIds.toTypedArray())
    }

    /**
     * Create a consumer group positioned at the latest event offset for the stream with input id
     *
     * @param streamKey the stream key for which create the group
     * @param groupName the group name
     * @return OK if operation was successful
     */
    fun createGroup(streamKey: String, groupName: String): String? {
        return redisTemplate.opsForStream<Any, Any>().createGroup(streamKey, groupName)
    }

    /**
     * Create a consumer group positioned at the latest event offset for the stream with input id
     *
     * @param streamKey the stream key for which create the group
     * @param groupName the group name
     * @param readOffset the offset from which start the receiver group
     * @return OK if operation was successful
     */
    fun createGroup(streamKey: String, groupName: String, readOffset: ReadOffset): String? {
        return redisTemplate.opsForStream<Any, Any>().createGroup(streamKey, readOffset, groupName)
    }

    /**
     * Destroy stream consumer group for the stream with input id
     *
     * @param streamKey the stream for which remove the group
     * @param groupName the group name to be destroyed
     * @return true iff the operation is completed successfully
     */
    fun destroyGroup(streamKey: String, groupName: String): Boolean? {
        return redisTemplate.opsForStream<Any, Any>().destroyGroup(streamKey, groupName)
    }

    /**
     * Get all the keys in keyspace
     *
     * @return a set populated with all the keys in keyspace
     */
    fun keysInKeyspace(): Set<String> = redisTemplate.keys("$keyspace*")

    /**
     * Get all the values in keyspace
     *
     * @return a list populated with all the entries in keyspace
     */
    fun allValuesInKeySpace(): MutableList<V>? =
        redisTemplate.opsForValue().multiGet(keysInKeyspace())

    /**
     * Unwrap this returning the underling used [RedisTemplate] instance
     *
     * @return this wrapper associated RedisTemplate instance
     */
    fun unwrap(): RedisTemplate<String, V> {
        return redisTemplate
    }

    /**
     * Get the Redis key from the input entity
     *
     * @param value - the entity value from which retrieve the Redis key
     * @return the key associated to the input entity
     */
    protected abstract fun getKeyFromEntity(value: V): String

    private fun compoundKeyWithKeyspace(key: String): String {
        return "$keyspace:$key"
    }
}
