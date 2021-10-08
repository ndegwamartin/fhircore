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

package org.smartregister.fhircore.sharedtest.impl.fhir

import ca.uhn.fhir.rest.gclient.TokenClientParam
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.SyncDownloadContext
import com.google.android.fhir.db.impl.dao.LocalChangeToken
import com.google.android.fhir.db.impl.dao.SquashedLocalChange
import com.google.android.fhir.search.Search
import java.time.OffsetDateTime
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Resource

class FhirEngineImpl : FhirEngine {

  val mockedResourcesStore = mutableListOf<Resource>()

  override suspend fun count(search: Search): Long = 1

  override suspend fun getLastSyncTimeStamp(): OffsetDateTime? = OffsetDateTime.now()

  override suspend fun <R : Resource> load(clazz: Class<R>, id: String): R {
    val existingResource =
      mockedResourcesStore.find { it.hasId() && it.id == id } ?: throw ResourceNotFoundException(id)
    return existingResource as R
  }

  override suspend fun <R : Resource> remove(clazz: Class<R>, id: String) {
    mockedResourcesStore.removeAll { it.id == id }
  }

  override suspend fun <R : Resource> save(vararg resource: R) {
    mockedResourcesStore.addAll(resource)
  }

  @Suppress("UNCHECKED_CAST")
  override suspend fun <R : Resource> search(search: Search): List<R> =
    mockedResourcesStore.filter { search.filter(TokenClientParam(it.id), Identifier()) } as List<R>

  override suspend fun syncDownload(download: suspend (SyncDownloadContext) -> List<Resource>) {
    // Do nothing
  }

  override suspend fun syncUpload(
    upload: suspend (List<SquashedLocalChange>) -> List<LocalChangeToken>
  ) {
    // Do nothing
  }

  override suspend fun <R : Resource> update(resource: R) {
    // Replace old resource
    mockedResourcesStore.removeAll { it.hasId() && it.id == resource.id }
    mockedResourcesStore.add(resource)
  }
}
