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

package org.smartregister.fhircore.engine.ui.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import org.smartregister.fhircore.engine.configuration.app.ConfigurableApplication
import org.smartregister.fhircore.engine.util.SHARED_PREFERENCE_LANG
import org.smartregister.fhircore.engine.util.SharedPreferenceHelper
import org.smartregister.fhircore.engine.util.extension.assertIsConfigurable
import org.smartregister.fhircore.engine.util.extension.setAppLocale

abstract class BaseMultiLanguageActivity : AppCompatActivity() {

  lateinit var sharedPreferenceHelper: SharedPreferenceHelper

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    application.assertIsConfigurable()
  }

  override fun attachBaseContext(baseContext: Context) {
    sharedPreferenceHelper =
      (baseContext.applicationContext as ConfigurableApplication).sharedPreferenceHelper
    val lang =
      sharedPreferenceHelper.read(SHARED_PREFERENCE_LANG, Locale.ENGLISH.toLanguageTag())
        ?: Locale.ENGLISH.toLanguageTag()
    baseContext.setAppLocale(lang).run {
      super.attachBaseContext(baseContext)
      applyOverrideConfiguration(this)
    }
  }
}
