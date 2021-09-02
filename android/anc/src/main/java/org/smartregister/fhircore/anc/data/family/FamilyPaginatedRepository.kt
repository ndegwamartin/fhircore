package org.smartregister.fhircore.anc.data.family

import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.Search
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.search
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.CarePlan
import org.hl7.fhir.r4.model.Patient
import org.smartregister.fhircore.anc.data.family.model.FamilyItem
import org.smartregister.fhircore.anc.sdk.PatientExtended
import org.smartregister.fhircore.anc.ui.family.register.Family
import org.smartregister.fhircore.anc.ui.family.register.FamilyItemMapper
import org.smartregister.fhircore.engine.data.domain.util.DomainMapper
import org.smartregister.fhircore.engine.data.domain.util.PaginatedDataSource
import org.smartregister.fhircore.engine.data.domain.util.RegisterRepository
import org.smartregister.fhircore.engine.data.local.repository.patient.PatientRepository
import org.smartregister.fhircore.engine.util.DefaultDispatcherProvider
import org.smartregister.fhircore.engine.util.DispatcherProvider

// todo Patient is not the only class
// todo remove repository from PaginatedDataSource
// todo we may not need multiple layers
class FamilyPaginatedRepository(
  override val fhirEngine: FhirEngine,
  override val domainMapper: DomainMapper<Family, FamilyItem>,
  private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider
) : RegisterRepository<Family, FamilyItem>{

  override val defaultPageSize: Int
    get() = 50

  override suspend fun loadData(
    query: String,
    pageNumber: Int,
    primaryFilterCallback: (Search) -> Unit,
    vararg secondaryFilterCallbacks: (String, Search) -> Unit
  ): List<FamilyItem> {
    return withContext(dispatcherProvider.io()) {
      val patients =
        fhirEngine.search<Patient> {
          primaryFilterCallback(this)
          secondaryFilterCallbacks.forEach { filterCallback: (String, Search) -> Unit ->
            if (query.isNotEmpty() && query.isNotBlank()) {
              filterCallback(query, this)
            }
          }
          sort(Patient.NAME, Order.ASCENDING)
          count = 500// todo defaultPageSize
          from = pageNumber * defaultPageSize
        }

      patients.filter { //todo
        it.extension.any{ it.value.toString().contains("family", true) }
      }.map { p ->
        val carePlans = searchCarePlan(p.id).toMutableList()

        val members = fhirEngine.search<Patient>{
          filter(Patient.LINK){
            this.value = p.id
          }
        }

        members.forEach {
          carePlans.addAll(searchCarePlan(it.id))
        }

        FamilyItemMapper.mapToDomainModel(Family(p, members, carePlans))
      }
    }
  }

  private suspend fun searchCarePlan(id: String): List<CarePlan> {
    return fhirEngine.search {
      filter(CarePlan.PATIENT) {
        this.value = id
      }
    }
  }
}
