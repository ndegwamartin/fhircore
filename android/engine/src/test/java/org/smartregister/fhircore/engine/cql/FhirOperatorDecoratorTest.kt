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

package org.smartregister.fhircore.engine.cql

import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.workflow.FhirOperator
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import org.hl7.fhir.r4.model.Library
import org.hl7.fhir.r4.model.MeasureReport
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.robolectric.util.ReflectionHelpers
import org.smartregister.fhircore.engine.robolectric.RobolectricTest

class FhirOperatorDecoratorTest : RobolectricTest() {

  private lateinit var fhirOperatorDecorator: FhirOperatorDecorator
  private lateinit var fhirOperator: FhirOperator

  @MockK private lateinit var fhirEngine: FhirEngine

  @RelaxedMockK private lateinit var fhirContext: FhirContext

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    fhirOperator = FhirOperator(fhirContext = fhirContext, fhirEngine = fhirEngine)
    fhirOperatorDecorator =
      FhirOperatorDecorator(fhirContext = fhirContext, fhirEngine = fhirEngine)
    fhirOperatorDecorator.operator = fhirOperator
  }

  @Test
  @Ignore
  fun testVerifyInit() {
    fhirOperatorDecorator.operator = null
    ReflectionHelpers.callInstanceMethod<Any>(fhirOperatorDecorator, "init")
    Assert.assertNotNull(fhirOperatorDecorator.operator)
  }

  @Test
  @Ignore
  fun testEvaluateMeasureShouldCallOriginalOperator() {
    every { fhirOperator.evaluateMeasure(any(), any(), any(), any(), any(), any(), any()) } returns
      MeasureReport().apply {
        status = MeasureReport.MeasureReportStatus.COMPLETE
        type = MeasureReport.MeasureReportType.INDIVIDUAL
      }

    val result =
      fhirOperatorDecorator.evaluateMeasure(
        "url",
        "start",
        "end",
        "report_type",
        "subject",
        "practitioner"
      )

    verify(exactly = 1) {
      fhirOperator.evaluateMeasure(
        "url",
        "start",
        "end",
        "report_type",
        "subject",
        "practitioner",
        null
      )
    }

    Assert.assertEquals(MeasureReport.MeasureReportStatus.COMPLETE, result.status)
    Assert.assertEquals(MeasureReport.MeasureReportType.INDIVIDUAL, result.type)
  }

  @Test
  fun testLoadLibShouldCallOriginalOperator() {
    val lib: Library = mockk<Library>(relaxed = true)
    fhirOperatorDecorator.operator = fhirOperator
    fhirOperatorDecorator.loadLib(lib)
    verify(exactly = 1) { fhirOperator.loadLib(any()) }
  }
}
