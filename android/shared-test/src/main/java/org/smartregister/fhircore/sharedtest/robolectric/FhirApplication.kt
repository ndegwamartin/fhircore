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

package org.smartregister.fhircore.sharedtest.robolectric

import android.app.Application
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.sync.Sync
import com.google.android.fhir.sync.SyncJob
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.ResourceType
import org.robolectric.annotation.Config
import org.smartregister.fhircore.engine.auth.AuthCredentials
import org.smartregister.fhircore.engine.auth.AuthenticationService
import org.smartregister.fhircore.engine.configuration.app.ApplicationConfiguration
import org.smartregister.fhircore.engine.configuration.app.ConfigurableApplication
import org.smartregister.fhircore.engine.configuration.app.applicationConfigurationOf
import org.smartregister.fhircore.engine.util.SecureSharedPreference
import org.smartregister.fhircore.engine.util.SharedPreferenceHelper
import org.smartregister.fhircore.sharedtest.fake.FakeKeyStore
import org.smartregister.fhircore.sharedtest.impl.FhirAuthenticationService
import org.smartregister.fhircore.sharedtest.impl.fhir.FhirEngineImpl
import org.smartregister.fhircore.sharedtest.robolectric.shadows.ShadowNpmPackageProvider

@Config(shadows = [ShadowNpmPackageProvider::class])
class FhirApplication : Application(), ConfigurableApplication {

  override var applicationConfiguration: ApplicationConfiguration = applicationConfigurationOf()

  override var fhirEngine: FhirEngine = spyk(FhirEngineImpl())

  override var resourceSyncParams: Map<ResourceType, Map<String, String>> = mapOf()

  override lateinit var workerContextProvider: SimpleWorkerContext

  override lateinit var syncJob: SyncJob

  override lateinit var authenticationService: AuthenticationService

  override lateinit var sharedPreferenceHelper: SharedPreferenceHelper

  override fun onCreate() {
    super.onCreate()
    fhirApplication = this
    initializeSharedPreferenceHelper()
    authenticationService = spyk(FhirAuthenticationService(this))
    syncJob = spyk(Sync.basicSyncJob(this))
    workerContextProvider = mockk(relaxed = true) { SimpleWorkerContext() }
  }

  private fun initializeSharedPreferenceHelper() {
    FakeKeyStore.setup
    val preferenceHelper = spyk(SharedPreferenceHelper(this))
    val secureSharedPreference = spyk(SecureSharedPreference(this))
    val sessionToken = "same-gibberish-string-as-token"
    every { secureSharedPreference.retrieveCredentials() } returns
      AuthCredentials(username = "demo", password = "Amani123", sessionToken = sessionToken)
    every { secureSharedPreference.deleteCredentials() }
    every { secureSharedPreference.retrieveSessionUsername() } returns "demo"
    every { secureSharedPreference.retrieveSessionToken() } returns sessionToken
    every { preferenceHelper.secureSharedPreference } returns secureSharedPreference
    sharedPreferenceHelper = preferenceHelper
  }

  override fun configureApplication(applicationConfiguration: ApplicationConfiguration) {
    this.applicationConfiguration = applicationConfiguration
  }

  override fun schedulePeriodicSync() {
    // Do nothing
  }

  companion object {
    private lateinit var fhirApplication: FhirApplication

    fun getContext() = fhirApplication
  }
}
