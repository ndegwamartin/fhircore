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

package org.smartregister.fhircore.eir.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject
import org.smartregister.fhircore.eir.ui.patient.register.PatientRegisterActivity
import org.smartregister.fhircore.engine.ui.login.LoginService

class EirLoginService @Inject constructor() : LoginService {

  override lateinit var loginActivity: AppCompatActivity

  override fun navigateToHome() {
    val intent =
      Intent(loginActivity, PatientRegisterActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    loginActivity.run {
      startActivity(intent)
      finish()
    }
  }
}
