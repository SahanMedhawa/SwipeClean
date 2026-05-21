package com.example.swipeclean.domain.usecase

import com.example.swipeclean.domain.repository.BinRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class CalculateReclaimableSpace @Inject constructor(
    private val binRepository: BinRepository
) {
    operator fun invoke(): Flow<Long> = binRepository.observeReclaimableBytes()
}
