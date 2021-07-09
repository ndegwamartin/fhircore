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

package org.smartregister.fhircore.util

import java.util.UUID
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Flag
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.RiskAssessment
import java.util.stream.Collectors

object QuestionnaireUtils {
  fun asCodeableConcept(q: Questionnaire.QuestionnaireItemComponent): CodeableConcept {
    return CodeableConcept().apply {
      this.text = q.text
      this.coding = q.code
    }
  }

  fun asObs(
    q: Questionnaire.QuestionnaireItemComponent,
    qr: QuestionnaireResponse.QuestionnaireResponseItemComponent,
    subject: Patient
  ): Observation {
    val obs = Observation()
    obs.id = UUID.randomUUID().toString().toLowerCase()
    obs.effective = DateTimeType.now()
    obs.code = asCodeableConcept(q)
    // obs.hasMember = Reference().apply { this.reference = "Observation/" + parent.id}
    obs.status = Observation.ObservationStatus.FINAL
    obs.value = if (qr.hasAnswer()) qr.answer[0].value else null
    obs.subject = Reference().apply { this.reference = "Patient/" + subject.id }

    return obs
  }

  // todo revisit this logic when ResourceMapper is stable
  fun extractObservations(
    questionnaireResponse: QuestionnaireResponse,
    questionnaire: Questionnaire,
    patient: Patient
  ): MutableList<Observation> {
    var observations = mutableListOf<Observation>()

    for (i in 0 until questionnaire.item.size) {
      // questionnaire and questionnaireResponse mapping go parallel
      val qItem = questionnaire.item[i]
      val qrItem = questionnaireResponse.item[i]

      if (qItem.definition?.contains("Observation") == true) {
        // get main group obs. only 1 level of obs nesting allowed for now //todo
        val main = asObs(qItem, qrItem, patient)

        // loop over all individual obs
        for (j in 0 until qItem.item.size) {
          val mainItem = qItem.item[j]
          val mainRespItem = qrItem.item[j]

          if (mainRespItem.hasAnswer()) {
            val obs = asObs(mainItem, mainRespItem, patient)

            // add reference to each comorbidity to main group obs
            main.addHasMember(Reference().apply { this.reference = "Observation/" + obs.id })

            observations.add(obs)
          }
        }

        observations.add(main)
      }
    }

    return observations
  }

  fun extractFlagExtension(questionnaire: Questionnaire, riskAssessment: RiskAssessment): Extension? {
    // no risk then no flag
    if (riskAssessment.prediction[0].relativeRisk.equals(0)) {
      return null
    }

    // if no flagging is needed return
    val qItem = questionnaire.item.map {
      itemWithExtension(it, "flag-detail")
    }.singleOrNull {it != null} ?:return null

    return qItem.extension.single { it.url.contains("flag-detail") }
  }

  private fun itemWithDefinition(questionnaireItem: Questionnaire.QuestionnaireItemComponent, definition: String): Questionnaire.QuestionnaireItemComponent? {
    if(questionnaireItem.definition?.contains(definition) == true){
      return questionnaireItem
    }

    for (i in questionnaireItem.item) {
      var qit = itemWithDefinition(i, definition)
      if(qit != null){
        return qit
      }
    }

    return null
  }

  private fun itemWithExtension(questionnaireItem: Questionnaire.QuestionnaireItemComponent, extension: String): Questionnaire.QuestionnaireItemComponent? {
    if(questionnaireItem.extension.singleOrNull { ro -> ro.url.contains(extension) } != null){
      return questionnaireItem
    }

    for (i in questionnaireItem.item) {
      var qit = itemWithExtension(i, extension)
      if(qit != null){
        return qit
      }
    }

    return null
  }

  fun extractRiskAssessment(observations: List<Observation>, questionnaire: Questionnaire): RiskAssessment? {
    val qItem = questionnaire.item.map {
      itemWithDefinition(it, "RiskAssessment")
    }.singleOrNull {it != null} ?:return null

    var riskScore = 0
    val risk = RiskAssessment()

    observations.forEach {
      val isRiskObs = it.extension.singleOrNull { ro -> ro.url.contains("RiskAssessment") } != null

        if(it.hasValue() && isRiskObs) {
          riskScore++

          risk.addBasis(Reference().apply { this.reference = "Observation/" + it.id })
        }
      }

    risk.status = RiskAssessment.RiskAssessmentStatus.FINAL
    risk.id = UUID.randomUUID().toString()
    risk.subject = observations[0].subject
    risk.occurrence = DateTimeType.now()
    risk.addPrediction().apply { 
        this.relativeRisk = riskScore.toBigDecimal()
        this.outcome.text = qItem.text
        this.outcome.coding = qItem.code
      }
    
    return risk
  } 
  
}
