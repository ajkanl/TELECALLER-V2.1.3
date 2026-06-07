package com.example.domain.repository

import com.example.data.local.entity.DebtorEntity
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.PromiseToPayEntity
import com.example.domain.security.TelecallerAgent
import kotlinx.coroutines.flow.SharedFlow

interface FirebaseSyncRepository {
    val syncEvents: SharedFlow<String>
    suspend fun syncBulkDataFromCloud(updatedList: List<DebtorEntity>)

    suspend fun pullDataFromFirestore(
        securitySettingsStore: com.example.domain.security.SecuritySettingsStore
    ): Boolean

    suspend fun pushAllDataToFirestore(
        debtors: List<DebtorEntity>,
        callLogs: List<CallLogEntity>,
        promises: List<PromiseToPayEntity>,
        agents: List<TelecallerAgent>,
        securitySettings: Map<String, Any>,
        syncSettings: Map<String, Any>
    ): Boolean
}

