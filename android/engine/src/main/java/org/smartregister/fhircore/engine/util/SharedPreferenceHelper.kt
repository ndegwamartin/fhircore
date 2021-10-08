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

class SharedPreferenceHelper(val context: Context) {

  val secureSharedPreference = SecureSharedPreference(context)

  val sharedPreferences: SharedPreferences =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  /** @see [SharedPreferences.getString] */
  fun read(key: String, defaultValue: String?) = sharedPreferences.getString(key, defaultValue)

  /** @see [SharedPreferences.Editor.putString] */
  fun write(key: String, value: Any?) {
    with(sharedPreferences.edit()) {
      when (value) {
        is String -> putString(key, value)
        is Long -> putLong(key, value)
        is Int -> putInt(key, value)
        is Float -> putFloat(key, value)
        is Boolean -> putBoolean(key, value)
      }
      commit()
    }
  }
}
