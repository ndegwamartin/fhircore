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

package org.smartregister.fhircore.engine.p2p.dao

import androidx.annotation.NonNull
import java.util.TreeSet

class P2PSenderTransferDao : BaseP2PTransferDao() {

  fun getDataType(): TreeSet<DataType> {
    return TreeSet<DataType>()
  }

  fun getJsonData(@NonNull dataType: DataType?, lastRecordId: Long, batchSize: Int): JsonData? {
    // TODO implement retrieval of data
    return null
  }
}
