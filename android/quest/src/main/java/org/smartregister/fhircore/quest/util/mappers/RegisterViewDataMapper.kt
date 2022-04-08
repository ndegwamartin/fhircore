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

package org.smartregister.fhircore.quest.util.mappers

import android.content.Context
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import org.smartregister.fhircore.engine.domain.model.RegisterData
import org.smartregister.fhircore.engine.domain.util.DataMapper
import org.smartregister.fhircore.engine.ui.theme.BlueTextColor
import org.smartregister.fhircore.engine.ui.theme.DueLightColor
import org.smartregister.fhircore.engine.ui.theme.OverdueDarkRedColor
import org.smartregister.fhircore.engine.ui.theme.OverdueLightColor
import org.smartregister.fhircore.quest.R
import org.smartregister.fhircore.quest.ui.patient.register.model.RegisterViewData

class RegisterViewDataMapper @Inject constructor(@ApplicationContext val context: Context) :
  DataMapper<RegisterData, RegisterViewData> {
  override fun transformInputToOutputModel(inputModel: RegisterData): RegisterViewData {
    return when (inputModel) {
      is RegisterData.DefaultRegisterData ->
        RegisterViewData(
          id = inputModel.id,
          title = listOf(inputModel.name, inputModel.age).joinToString(", "),
          subtitle =
            inputModel.gender.name.lowercase().replaceFirstChar {
              it.uppercase()
            } // TODO make transalatable
        )
      is RegisterData.FamilyRegisterData -> {
        val serviceText =
          when {
            inputModel.servicesOverdue != 0 -> inputModel.servicesOverdue.toString()
            inputModel.servicesDue != 0 -> inputModel.servicesDue.toString()
            else -> null
          }
        RegisterViewData(
          id = inputModel.id,
          title = inputModel.name,
          subtitle = inputModel.address,
          status = context.getString(R.string.date_last_visited, inputModel.lastSeen),
          serviceActionable = false,
          serviceButtonBackgroundColor =
            if (inputModel.servicesOverdue != 0) OverdueDarkRedColor else Color.White,
          serviceButtonForegroundColor =
            if (inputModel.servicesOverdue != 0) Color.White else BlueTextColor,
          serviceMemberIcons =
            inputModel
              .members
              .filter { it.pregnant != null && it.pregnant!! }
              .map { R.drawable.ic_pregnant }
              .plus(inputModel.members.filter { it.age.toInt() <= 5 }.map { R.drawable.ic_kids }),
          serviceText = serviceText,
          borderedServiceButton = inputModel.servicesDue != 0 && inputModel.servicesOverdue == 0,
          serviceButtonBorderColor = BlueTextColor,
          showDivider = true,
          showServiceButton = !serviceText.isNullOrEmpty()
        )
      }
      is RegisterData.AncRegisterData ->
        RegisterViewData(
          id = inputModel.id,
          title = listOf(inputModel.name, inputModel.age).joinToString(),
          subtitle = inputModel.address,
          status = context.getString(R.string.date_last_visited, inputModel.visitStatus.name),
          serviceActionable = true,
          serviceButtonBackgroundColor =
            if (inputModel.servicesOverdue == 0) DueLightColor else OverdueLightColor,
          serviceButtonForegroundColor =
            if (inputModel.servicesOverdue == 0) BlueTextColor else OverdueDarkRedColor,
          serviceText = context.getString(R.string.anc_visit),
          showServiceButton = inputModel.servicesOverdue != 0 || inputModel.servicesDue != 0
        )
      else -> throw UnsupportedOperationException()
    }
  }
}
