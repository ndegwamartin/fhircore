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

package org.smartregister.fhircore.engine.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.smartregister.fhircore.engine.auth.AuthCredentials
import org.smartregister.fhircore.engine.util.extension.decodeJson
import org.smartregister.fhircore.engine.util.extension.encodeJson

class SecureSharedPreference(val context: Context) {

  var preferences: SharedPreferences =
    EncryptedSharedPreferences.create(
      context,
      SECURE_STORAGE_FILE_NAME,
      MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

  fun saveCredentials(authCredentials: AuthCredentials) {
    preferences.edit {
      putString(KEY_LATEST_CREDENTIALS_PREFERENCE, authCredentials.encodeJson())
      putString(KEY_LATEST_SESSION_TOKEN_PREFERENCE, authCredentials.sessionToken)
    }
  }

  fun deleteCredentials() {
    preferences.edit {
      remove(KEY_LATEST_CREDENTIALS_PREFERENCE)
      remove(KEY_LATEST_SESSION_TOKEN_PREFERENCE)
    }
  }

  fun retrieveSessionToken() = preferences.getString(KEY_LATEST_SESSION_TOKEN_PREFERENCE, null)

  fun retrieveSessionUsername() = retrieveCredentials()?.username

  fun retrieveCredentials(): AuthCredentials? {
    return preferences
      .getString(KEY_LATEST_CREDENTIALS_PREFERENCE, null)
      ?.decodeJson<AuthCredentials>()
  }
}
