/*
 * Copyright 2021 Ona Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smartregister.fhircore.engine.di

import android.accounts.AccountManager
import android.content.Context
import com.google.android.fhir.DatabaseErrorStrategy
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineConfiguration
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.ServerConfiguration
import com.google.android.fhir.sync.Authenticator
import com.google.android.fhir.sync.Sync
import com.google.android.fhir.sync.SyncJob
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.smartregister.fhircore.engine.auth.TokenManagerService
import org.smartregister.fhircore.engine.configuration.ConfigurationRegistry
import org.smartregister.fhircore.engine.configuration.app.ConfigService
import org.smartregister.fhircore.engine.sync.SyncBroadcaster
import org.smartregister.fhircore.engine.util.SharedPreferencesHelper

@InstallIn(SingletonComponent::class)
@Module(includes = [NetworkModule::class, DispatcherModule::class, CqlModule::class])
class EngineModule {

  @Singleton
  @Provides
  fun provideFhirEngine(
    @ApplicationContext context: Context,
    tokenManagerService: TokenManagerService,
    configService: ConfigService
  ): FhirEngine {

    FhirEngineProvider.init(
      FhirEngineConfiguration(
        enableEncryptionIfSupported = true,
        DatabaseErrorStrategy.UNSPECIFIED,
        ServerConfiguration(
          baseUrl = configService.provideAuthConfiguration().fhirServerBaseUrl,
          authenticator =
            object : Authenticator {
              override fun getAccessToken() =
                tokenManagerService.getBlockingActiveAuthToken() as String
            }
        )
      )
    )

    return FhirEngineProvider.getInstance(context)
  }

  @Singleton
  @Provides
  fun provideSyncJob(@ApplicationContext context: Context) = Sync.basicSyncJob(context)

  @Provides
  fun provideSyncBroadcaster(
    configurationRegistry: ConfigurationRegistry,
    sharedPreferencesHelper: SharedPreferencesHelper,
    configService: ConfigService,
    syncJob: SyncJob,
    fhirEngine: FhirEngine
  ) =
    SyncBroadcaster(
      configurationRegistry = configurationRegistry,
      sharedPreferencesHelper = sharedPreferencesHelper,
      configService = configService,
      fhirEngine = fhirEngine,
      syncJob = syncJob
    )

  @Singleton @Provides fun provideWorkerContextProvider() = SimpleWorkerContext()

  @Singleton
  @Provides
  fun provideApplicationManager(@ApplicationContext context: Context): AccountManager =
    AccountManager.get(context)
}
