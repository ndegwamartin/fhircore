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

package org.smartregister.fhircore.quest

import android.app.Application
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.sync.Sync
import com.google.android.fhir.sync.SyncJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.ResourceType
import org.smartregister.fhircore.engine.auth.AuthenticationService
import org.smartregister.fhircore.engine.configuration.app.ApplicationConfiguration
import org.smartregister.fhircore.engine.configuration.app.ConfigurableApplication
import org.smartregister.fhircore.engine.configuration.app.applicationConfigurationOf
import org.smartregister.fhircore.engine.util.DefaultDispatcherProvider
import org.smartregister.fhircore.engine.util.SharedPreferenceHelper
import org.smartregister.fhircore.engine.util.USER_QUESTIONNAIRE_PUBLISHER_SHARED_PREFERENCE_KEY
import org.smartregister.fhircore.engine.util.extension.initializeWorkerContext
import org.smartregister.fhircore.engine.util.extension.runPeriodicSync
import timber.log.Timber

class QuestApplication : Application(), ConfigurableApplication {

  override val fhirEngine: FhirEngine by lazy { constructFhirEngine() }

  override lateinit var workerContextProvider: SimpleWorkerContext

  override lateinit var syncJob: SyncJob

  override lateinit var applicationConfiguration: ApplicationConfiguration

  override lateinit var authenticationService: AuthenticationService

  override lateinit var sharedPreferenceHelper: SharedPreferenceHelper

  override val resourceSyncParams: Map<ResourceType, Map<String, String>>
    get() {
      return mapOf(
        ResourceType.Patient to mapOf(),
        ResourceType.Questionnaire to buildQuestionnaireFilterMap(),
        ResourceType.CarePlan to mapOf()
      )
    }

  private val defaultDispatcherProvider = DefaultDispatcherProvider

  private fun buildQuestionnaireFilterMap(): MutableMap<String, String> {
    val questionnaireFilterMap: MutableMap<String, String> = HashMap()
    val publisher =
      sharedPreferenceHelper.read(
        key = USER_QUESTIONNAIRE_PUBLISHER_SHARED_PREFERENCE_KEY,
        defaultValue = null
      )
    if (publisher != null) questionnaireFilterMap[Questionnaire.SP_PUBLISHER] = publisher
    return questionnaireFilterMap
  }

  private fun constructFhirEngine(): FhirEngine {
    return FhirEngineProvider.getInstance(this)
  }

  override fun configureApplication(applicationConfiguration: ApplicationConfiguration) {
    this.applicationConfiguration = applicationConfiguration
  }

  override fun schedulePeriodicSync() {
    this.runPeriodicSync<QuestFhirSyncWorker>()
  }

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
    syncJob = Sync.basicSyncJob(this)
    authenticationService = QuestAuthenticationService(this)
    sharedPreferenceHelper = SharedPreferenceHelper(this)

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    CoroutineScope(defaultDispatcherProvider.io()).launch {
      workerContextProvider = this@QuestApplication.initializeWorkerContext()!!
    }

    schedulePeriodicSync()
  }
}
