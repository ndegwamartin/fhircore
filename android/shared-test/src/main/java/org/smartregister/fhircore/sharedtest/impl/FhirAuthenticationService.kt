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

package org.smartregister.fhircore.sharedtest.impl

import android.content.Context
import org.smartregister.fhircore.engine.auth.AuthenticationService
import org.smartregister.fhircore.engine.configuration.app.ApplicationConfiguration
import org.smartregister.fhircore.engine.configuration.app.applicationConfigurationOf
import org.smartregister.fhircore.sharedtest.impl.activity.LoginActivity

class FhirAuthenticationService(override val context: Context) : AuthenticationService(context) {

  override fun skipLogin(): Boolean = false

  override fun getLoginActivityClass(): Class<*> = LoginActivity::class.java

  override fun getAccountType(): String = "test.account.type"

  override fun clientSecret(): String = "test.client.secret"

  override fun clientId(): String = "test.client.id"

  override fun providerScope(): String = "openid"

  override fun getApplicationConfigurations(): ApplicationConfiguration =
    applicationConfigurationOf()
}
