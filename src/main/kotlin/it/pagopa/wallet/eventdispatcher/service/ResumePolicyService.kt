package it.pagopa.wallet.eventdispatcher.service

import java.time.Instant

interface ResumePolicyService {
    fun getResumeTimestamp(): Instant
    fun saveResumeTimestamp(timestamp: Instant)
}
