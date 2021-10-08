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

package org.smartregister.fhircore.eir

import android.app.Application
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.sync.Sync
import com.google.android.fhir.sync.SyncJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.ResourceType
import org.smartregister.fhircore.engine.auth.AuthenticationService
import org.smartregister.fhircore.engine.configuration.app.ApplicationConfiguration
import org.smartregister.fhircore.engine.configuration.app.ConfigurableApplication
import org.smartregister.fhircore.engine.configuration.app.applicationConfigurationOf
import org.smartregister.fhircore.engine.util.DefaultDispatcherProvider
import org.smartregister.fhircore.engine.util.SharedPreferenceHelper
import org.smartregister.fhircore.engine.util.extension.initializeWorkerContext
import org.smartregister.fhircore.engine.util.extension.runPeriodicSync
import timber.log.Timber

class EirApplication : Application(), ConfigurableApplication {

  private val defaultDispatcherProvider = DefaultDispatcherProvider

  override lateinit var syncJob: SyncJob

  override lateinit var workerContextProvider: SimpleWorkerContext

  override lateinit var applicationConfiguration: ApplicationConfiguration

  override lateinit var authenticationService: AuthenticationService

  override lateinit var sharedPreferenceHelper: SharedPreferenceHelper

  override val fhirEngine: FhirEngine by lazy { FhirEngineProvider.getInstance(this) }

  override val resourceSyncParams: Map<ResourceType, Map<String, String>>
    get() =
      mapOf(
        ResourceType.Patient to emptyMap(),
        ResourceType.Immunization to emptyMap(),
        ResourceType.Questionnaire to emptyMap(),
        ResourceType.StructureMap to mapOf(),
        ResourceType.RelatedPerson to mapOf()
      )

  override fun onCreate() {
    super.onCreate()
    configureApplication(
      applicationConfigurationOf(
        oauthServerBaseUrl = BuildConfig.OAUTH_BASE_URL,
        fhirServerBaseUrl = BuildConfig.FHIR_BASE_URL,
        clientId = BuildConfig.OAUTH_CIENT_ID,
        clientSecret = BuildConfig.OAUTH_CLIENT_SECRET,
        languages = listOf("en", "sw")
      )
    )

    sharedPreferenceHelper = SharedPreferenceHelper(this)
    authenticationService = EirAuthenticationService(this)
    syncJob = Sync.basicSyncJob(this)

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    CoroutineScope(defaultDispatcherProvider.io()).launch {
      workerContextProvider = this@EirApplication.initializeWorkerContext()!!
    }

    schedulePeriodicSync()
  }

  override fun schedulePeriodicSync() {
    this.runPeriodicSync<EirFhirSyncWorker>()
  }

  override fun configureApplication(applicationConfiguration: ApplicationConfiguration) {
    this.applicationConfiguration = applicationConfiguration
  }
}
