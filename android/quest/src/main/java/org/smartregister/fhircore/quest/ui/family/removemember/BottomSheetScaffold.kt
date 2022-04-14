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

package org.smartregister.fhircore.quest.ui.family.removemember

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.smartregister.fhircore.engine.ui.theme.AppTheme

class BottomSheetScaffold(
  private val bottomSheetHolder: BottomSheetHolder,
  private val onBottomSheetListener: OnClickedListItems
) : BottomSheetDialogFragment() {

  init {
    isCancelable = false
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        AppTheme {
          BottomSheetListView(
            bottomSheetHolder = bottomSheetHolder,
            onBottomSheetListener = onBottomSheetListener
          )
        }
      }
    }
  }
}

data class BottomSheetHolder(
  val title: String,
  val subTitle: String,
  val tvWarningTitle: String,
  val list: List<BottomSheetDataModel>,
  var reselect: Boolean = false
)

data class BottomSheetDataModel(
  val itemName: String,
  val itemDetail: String,
  val id: String,
  var selected: Boolean = false
)

interface OnClickedListItems {
  fun onSave(bottomSheetDataModel: BottomSheetDataModel)
  fun onCancel()
}
