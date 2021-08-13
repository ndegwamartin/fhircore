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

import android.content.Context
import org.smartregister.fhircore.eir.ui.login.LoginActivity
import org.smartregister.fhircore.engine.auth.AuthenticationService

class EirAuthenticationService(override val context: Context) : AuthenticationService(context) {

  override fun getLoginActivityClass() = LoginActivity::class.java

  override fun getAccountType() = context.getString(R.string.authenticator_account_type)

  override fun clientSecret() = BuildConfig.OAUTH_CLIENT_SECRET

  override fun clientId() = BuildConfig.OAUTH_CIENT_ID

  override fun providerScope() = BuildConfig.OAUTH_SCOPE

  override fun getApplicationConfigurations() = EirApplication.getContext().eirConfigurations()
}
