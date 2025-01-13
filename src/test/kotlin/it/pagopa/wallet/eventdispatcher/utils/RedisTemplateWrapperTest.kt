package it.pagopa.wallet.eventdispatcher.utils

import java.time.Duration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.ValueOperations

class RedisTemplateWrapperTest {

    private val defaultTtl = Duration.ofSeconds(1)
    private val keyspace = "keyspace"
    private val redisTemplate: RedisTemplate<String, String> = mock()
    private val opsForValue: ValueOperations<String, String> = mock()
    private val opsForStream: StreamOperations<String, String, String> = mock()

    class MockedRedisTemplateWrapper(
        defaultTtl: Duration,
        keyspace: String,
        redisTemplate: RedisTemplate<String, String>
    ) :
        RedisTemplateWrapper<String>(
            defaultTTL = defaultTtl,
            redisTemplate = redisTemplate,
            keyspace = keyspace
        ) {
        override fun getKeyFromEntity(value: String): String = value
    }

    private val mockedRedisTemplateWrapper =
        MockedRedisTemplateWrapper(
            defaultTtl = defaultTtl,
            redisTemplate = redisTemplate,
            keyspace = keyspace
        )

    @Test
    fun `Should save entity successfully with default TTL`() {
        // pre-requisites
        given(redisTemplate.opsForValue()).willReturn(opsForValue)
        doNothing().`when`(opsForValue).set(any(), any(), any<Duration>())
        val valueToSet = "test"
        val expectedKey = "$keyspace:$valueToSet"
        // test
        mockedRedisTemplateWrapper.save(valueToSet)
        // assertions
        verify(redisTemplate, times(1)).opsForValue()
        verify(opsForValue, times(1)).set(expectedKey, valueToSet, defaultTtl)
    }

    @Test
    fun `Should save entity successfully with custom TTL`() {
        // pre-requisites
        given(redisTemplate.opsForValue()).willReturn(opsForValue)
        doNothing().`when`(opsForValue).set(any(), any(), any<Duration>())
        val valueToSet = "test"
        val expectedKey = "$keyspace:$valueToSet"
        val customTTL = defaultTtl + Duration.ofSeconds(1)
        // test
        mockedRedisTemplateWrapper.save(value = valueToSet, ttl = customTTL)
        // assertions
        verify(redisTemplate, times(1)).opsForValue()
        verify(opsForValue, times(1)).set(expectedKey, valueToSet, customTTL)
    }

    @Test
    fun `Should write event to stream trimming old events`() {
        // pre-requisites
        val streamKey = "streamKey"
        val event = "event"
        val streamSize = 1L
        val recordId = RecordId.of(0, 0)
        given(redisTemplate.opsForStream<String, String>()).willReturn(opsForStream)
        given(opsForStream.trim(any(), any())).willReturn(0L)
        given(opsForStream.add(any())).willReturn(recordId)
        // test
        mockedRedisTemplateWrapper.writeEventToStreamTrimmingEvents(
            streamKey = streamKey,
            event = event,
            streamSize = streamSize
        )
        // assertions
        verify(redisTemplate, times(2)).opsForStream<String, String>()
        verify(opsForStream, times(1)).trim(streamKey, streamSize)
        verify(opsForStream, times(1)).add(ObjectRecord.create(streamKey, event))
    }

    @Test
    fun `Should throw IllegalArgumentException write event to stream with invalid trimming event size`() {
        // pre-requisites
        val streamKey = "streamKey"
        val event = "event"
        val streamSize = -1L
        val recordId = RecordId.of(0, 0)
        given(redisTemplate.opsForStream<String, String>()).willReturn(opsForStream)
        given(opsForStream.trim(any(), any())).willReturn(0L)
        given(opsForStream.add(any())).willReturn(recordId)
        // test
        val exception =
            assertThrows<IllegalArgumentException> {
                mockedRedisTemplateWrapper.writeEventToStreamTrimmingEvents(
                    streamKey = streamKey,
                    event = event,
                    streamSize = streamSize
                )
            }
        // assertions
        assertEquals("Invalid input $streamSize events to trim, it must be >=0", exception.message)
        verify(redisTemplate, times(0)).opsForStream<String, String>()
        verify(opsForStream, times(0)).trim(streamKey, streamSize)
        verify(opsForStream, times(0)).add(ObjectRecord.create(streamKey, event))
    }

    @Test
    fun `Should find all keys in keyspace`() {
        // pre-requisites
        given(redisTemplate.keys(any())).willReturn(setOf())
        // test
        mockedRedisTemplateWrapper.keysInKeyspace()
        // assertions
        verify(redisTemplate, times(1)).keys("$keyspace*")
    }

    @Test
    fun `Should find all values in keyspace`() {
        // pre-requisites
        val keys = setOf("key1", "key2")
        given(redisTemplate.keys(any())).willReturn(keys)
        given(redisTemplate.opsForValue()).willReturn(opsForValue)
        given(opsForValue.multiGet(any())).willReturn(listOf())
        // test
        mockedRedisTemplateWrapper.allValuesInKeySpace()
        // assertions
        verify(redisTemplate, times(1)).keys("$keyspace*")
        verify(redisTemplate, times(1)).opsForValue()
        verify(opsForValue, times(1)).multiGet(keys)
    }
}
