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

package org.smartregister.fhircore.anc.ui.anccare.details

import android.view.View
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.google.android.fhir.FhirEngine
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.android.synthetic.main.fragment_anc_details.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import org.smartregister.fhircore.anc.R
import org.smartregister.fhircore.anc.coroutine.CoroutineTestRule
import org.smartregister.fhircore.anc.data.model.AncPatientDetailItem
import org.smartregister.fhircore.anc.data.model.AncPatientItem
import org.smartregister.fhircore.anc.data.model.CarePlanItem
import org.smartregister.fhircore.anc.data.model.UpcomingServiceItem
import org.smartregister.fhircore.anc.data.patient.PatientRepository
import org.smartregister.fhircore.anc.robolectric.FragmentRobolectricTest
import org.smartregister.fhircore.anc.shadow.AncApplicationShadow
import org.smartregister.fhircore.engine.data.remote.fhir.resource.FhirResourceDataSource
import org.smartregister.fhircore.engine.util.FileUtil
import java.io.ByteArrayInputStream
import java.io.InputStream

@ExperimentalCoroutinesApi
@Config(shadows = [AncApplicationShadow::class])
internal class AncDetailsFragmentTest : FragmentRobolectricTest() {

  private lateinit var fhirEngine: FhirEngine
  private lateinit var patientDetailsViewModel: AncDetailsViewModel
  private lateinit var patientDetailsActivity: AncDetailsActivity
  private lateinit var patientRepository: PatientRepository
  private lateinit var fragmentScenario: FragmentScenario<AncDetailsFragment>
  private lateinit var patientDetailsFragment: AncDetailsFragment
  private lateinit var carePlanAdapter: CarePlanAdapter
  private lateinit var upcomingServicesAdapter: UpcomingServicesAdapter
  private lateinit var lastSeen: EncounterAdapter

  @get:Rule var coroutinesTestRule = CoroutineTestRule()
  @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()

  private val patientId = "samplePatientId"
  var ancPatientDetailItem = spyk<AncPatientDetailItem>()

  @MockK lateinit var parser: IParser
  @MockK lateinit var fhirResourceDataSource: FhirResourceDataSource

  var fileUtil=FileUtil()

  var libraryData = fileUtil.readJsonFile("test/resources/cql/library.json")

  var valueSetData = fileUtil.readJsonFile("test/resources/cql/valueSet.json")
  val valueSetDataStream: InputStream = ByteArrayInputStream(valueSetData.toByteArray())

  var patientData = fileUtil.readJsonFile("test/resources/cql/patient.json")
  val patientDataStream: InputStream = ByteArrayInputStream(patientData.toByteArray())

  var helperData = fileUtil.readJsonFile("test/resources/cql/helper.json")

  val parameters = "{\"parameters\":\"parameters\"}"

  @Before
  fun setUp() {

    MockKAnnotations.init(this, relaxUnitFun = true)

    fhirEngine = mockk(relaxed = true)
    patientRepository = mockk()
    carePlanAdapter = mockk()
    upcomingServicesAdapter = mockk()
    lastSeen = mockk()

    every { carePlanAdapter.submitList(any()) } returns Unit
    every { upcomingServicesAdapter.submitList(any()) } returns Unit
    every { lastSeen.submitList(any()) } returns Unit
    every { ancPatientDetailItem.patientDetails } returns
      AncPatientItem(patientId, "Mandela Nelson", "M", "26")
    every { ancPatientDetailItem.patientDetailsHead } returns AncPatientItem()
    coEvery { patientRepository.fetchDemographics(patientId) } returns ancPatientDetailItem

    patientDetailsViewModel =
      spyk(
        AncDetailsViewModel(patientRepository, coroutinesTestRule.testDispatcherProvider, patientId)
      )

    patientDetailsActivity =
      Robolectric.buildActivity(AncDetailsActivity::class.java).create().get()
    fragmentScenario =
      launchFragmentInContainer(
        factory =
          object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
              val fragment = spyk(AncDetailsFragment.newInstance())
              every { fragment.activity } returns patientDetailsActivity
              fragment.ancDetailsViewModel = patientDetailsViewModel

              return fragment
            }
          }
      )

    fragmentScenario.onFragment {
      patientDetailsFragment = it
      ReflectionHelpers.setField(patientDetailsFragment, "carePlanAdapter", carePlanAdapter)
      ReflectionHelpers.setField(
        patientDetailsFragment,
        "upcomingServicesAdapter",
        upcomingServicesAdapter
      )
      ReflectionHelpers.setField(patientDetailsFragment, "lastSeen", lastSeen)
    }

    patientDetailsFragment.libraryResources=ArrayList()
    patientDetailsFragment.fhirContext= FhirContext.forCached(FhirVersionEnum.R4)
    patientDetailsFragment.parser=patientDetailsFragment.fhirContext.newJsonParser()
    patientDetailsFragment.valueSetBundle=patientDetailsFragment.parser.parseResource(valueSetDataStream) as IBaseBundle
    patientDetailsFragment.patientDataIBase =
      patientDetailsFragment.parser.parseResource(patientDataStream) as IBaseBundle
    patientDetailsFragment.libraryData=libraryData
    patientDetailsFragment.helperData=helperData
  }

  @Test
  fun testHandleCarePlanShouldVerifyExpectedCalls() {

    ReflectionHelpers.callInstanceMethod<Any>(
      patientDetailsFragment,
      "handleCarePlan",
      ReflectionHelpers.ClassParameter(List::class.java, listOf<CarePlanItem>())
    )

    // No CarePlan available text displayed
    val noVaccinesTextView =
      patientDetailsFragment.view?.findViewById<TextView>(R.id.txtView_noCarePlan)

    // CarePlan list is not displayed
    val immunizationsListView =
      patientDetailsFragment.view?.findViewById<RecyclerView>(R.id.carePlanListView)

    Assert.assertEquals(View.VISIBLE, noVaccinesTextView?.visibility)
    Assert.assertEquals(View.GONE, immunizationsListView?.visibility)

    ReflectionHelpers.callInstanceMethod<Any>(
      patientDetailsFragment,
      "handleCarePlan",
      ReflectionHelpers.ClassParameter(
        List::class.java,
        listOf(CarePlanItem("1111", "", due = true, overdue = false))
      )
    )

    Assert.assertEquals(View.GONE, noVaccinesTextView?.visibility)
    Assert.assertEquals(View.VISIBLE, immunizationsListView?.visibility)

    verify(exactly = 1) { carePlanAdapter.submitList(any()) }
  }

  @Test
  fun testHandleEncounterShouldVerifyExpectedCalls() {

    ReflectionHelpers.callInstanceMethod<Any>(
      patientDetailsFragment,
      "handleUpcomingServices",
      ReflectionHelpers.ClassParameter(List::class.java, listOf<UpcomingServiceItem>())
    )

    // No CarePlan available text displayed
    val noVaccinesTextView =
      patientDetailsFragment.view?.findViewById<TextView>(R.id.txtView_noUpcomingServices)

    // CarePlan list is not displayed
    val immunizationsListView =
      patientDetailsFragment.view?.findViewById<RecyclerView>(R.id.upcomingServicesListView)

    Assert.assertEquals(View.VISIBLE, noVaccinesTextView?.visibility)
    Assert.assertEquals(View.GONE, immunizationsListView?.visibility)

    ReflectionHelpers.callInstanceMethod<Any>(
      patientDetailsFragment,
      "handleUpcomingServices",
      ReflectionHelpers.ClassParameter(
        List::class.java,
        listOf(UpcomingServiceItem("1111", "ABC", "2020-02-01"))
      )
    )

    Assert.assertEquals(View.GONE, noVaccinesTextView?.visibility)
    Assert.assertEquals(View.VISIBLE, immunizationsListView?.visibility)

    verify(exactly = 1) { upcomingServicesAdapter.submitList(any()) }
  }

  @Test
  fun testHandleLastSceneShouldVerifyExpectedCalls() {

    ReflectionHelpers.callInstanceMethod<Any>(
      patientDetailsFragment,
      "handleLastSeen",
      ReflectionHelpers.ClassParameter(List::class.java, listOf<UpcomingServiceItem>())
    )

    // No CarePlan available text displayed
    val noVaccinesTextView =
      patientDetailsFragment.view?.findViewById<TextView>(R.id.txtView_noLastSeenServices)

    // CarePlan list is not displayed
    val immunizationsListView =
      patientDetailsFragment.view?.findViewById<RecyclerView>(R.id.lastSeenListView)

    Assert.assertEquals(View.VISIBLE, noVaccinesTextView?.visibility)
    Assert.assertEquals(View.GONE, immunizationsListView?.visibility)

    ReflectionHelpers.callInstanceMethod<Any>(
      patientDetailsFragment,
      "handleLastSeen",
      ReflectionHelpers.ClassParameter(
        List::class.java,
        listOf(UpcomingServiceItem("1111", "ABC", "2020-02-01"))
      )
    )

    Assert.assertEquals(View.GONE, noVaccinesTextView?.visibility)
    Assert.assertEquals(View.VISIBLE, immunizationsListView?.visibility)

    verify(exactly = 1) { lastSeen.submitList(any()) }
  }

  override fun getFragmentScenario(): FragmentScenario<out Fragment> {
    return fragmentScenario
  }

  override fun getFragment(): Fragment {
    return patientDetailsFragment
  }

  @Test
  fun testThatDemographicViewsAreUpdated() {

    val item =
      AncPatientDetailItem(
        AncPatientItem(patientIdentifier = "1", name = "demo", gender = "M", age = "20"),
        AncPatientItem(demographics = "2")
      )

    ReflectionHelpers.callInstanceMethod<Any>(
      patientDetailsFragment,
      "handlePatientDemographics",
      ReflectionHelpers.ClassParameter(AncPatientDetailItem::class.java, item)
    )

    Assert.assertEquals("demo, M, 20", patientDetailsFragment.binding.txtViewPatientDetails.text)
    Assert.assertEquals("2 ID: 1", patientDetailsFragment.binding.txtViewPatientId.text)
  }

  @Test
  fun testHandleCQLMeasureLoadPatient(){
    every { patientDetailsFragment.setPatientTestData(any()) } returns Unit
    every { patientDetailsFragment.loadCQLLibraryData() } returns Unit
    every { patientDetailsFragment.loadMeasureEvaluateLibrary() } returns Unit
    patientDetailsFragment.handleCQLMeasureLoadPatient("test")
  }

  @Test
  fun testSetPatientTestData(){
    every {
      patientDetailsFragment
        .libraryEvaluator.processCQLPatientBundle(any())
    } returns valueSetData
    patientDetailsFragment.testData=""
    patientDetailsFragment.setPatientTestData("")
    Assert.assertEquals(valueSetData, patientDetailsFragment.testData)
  }

  @Test
  fun testHandleCQLLibraryData() {
    val auxLibraryData = "auxLibraryData"
    every { patientDetailsFragment.loadCQLHelperData() } returns Unit
    every { patientDetailsFragment.fileUtil.writeFileOnInternalStorage(any(),any(),any(),any()) } returns Unit
    patientDetailsFragment.handleCQLLibraryData(auxLibraryData)
    Assert.assertEquals(auxLibraryData, patientDetailsFragment.libraryData)
  }

  @Test
  fun testHandleCQLHelperData() {
    val auxHelperData = "auxHelperData"
    every { patientDetailsFragment.loadCQLValueSetData() } returns Unit
    every { patientDetailsFragment.loadCQLLibrarySources() } returns Unit
    every { patientDetailsFragment.fileUtil.writeFileOnInternalStorage(any(),any(),any(),any()) } returns Unit

    patientDetailsFragment.handleCQLHelperData("auxHelperData")
    Assert.assertEquals(auxHelperData, patientDetailsFragment.helperData)
  }

  @Test
  fun testPostValueSetData(){
    patientDetailsFragment.postValueSetData(valueSetData)
    Assert.assertNotNull(patientDetailsFragment.valueSetBundle)
  }

  @Test
  fun testLoadCQLLibrarySources() {
    patientDetailsFragment.loadCQLLibrarySources()
    Assert.assertNotNull(patientDetailsFragment.libraryResources)
  }

  @Test
  fun testHandleCQLValueSetData() {
    val auxValueSetData = "auxValueSetData"
    every { patientDetailsFragment.fileUtil.writeFileOnInternalStorage(any(),any(),any(),any()) } returns Unit
    every { patientDetailsFragment.postValueSetData(any()) } returns Unit
    patientDetailsFragment.handleCQLValueSetData(auxValueSetData)
    Assert.assertEquals(auxValueSetData, patientDetailsFragment.valueSetData)
  }


  @Test
  fun testHandleCQL() {
    val parameters = "{\"parameters\":\"parameters\"}"

    every {
      patientDetailsFragment.libraryEvaluator.runCql(
        patientDetailsFragment.libraryResources,
        patientDetailsFragment.valueSetBundle,
        patientDetailsFragment.patientDataIBase,
        any(),
        any(),
        any(),
        any()
      )
    } returns parameters
    patientDetailsFragment.handleCQL()
    Assert.assertNotNull(parameters)
  }

  @Test
  fun testHandleMeasureEvaluate() {
    every {
      patientDetailsFragment.measureEvaluator.runMeasureEvaluate(
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any()
      )
    } returns parameters
    Assert.assertNotNull(parameters)
  }

  @Test
  fun testLoadCQLLibraryData() {

    every { patientDetailsFragment.dir.exists() } returns true
    every { patientDetailsFragment.loadCQLHelperData() } returns Unit
    patientDetailsFragment.loadCQLLibraryData()

    every { patientDetailsFragment.dir.exists() } returns false
    val auxCQLLibraryData = "auxCQLLibraryData"
    val libraryData = MutableLiveData<String>()
    libraryData.postValue(auxCQLLibraryData)

    coroutinesTestRule.runBlockingTest {
      coEvery {
        patientDetailsViewModel.fetchCQLLibraryData(parser, fhirResourceDataSource, any())
      } returns libraryData
    }
    patientDetailsFragment.loadCQLLibraryData()
    Assert.assertNotNull(libraryData.value)
    Assert.assertEquals(auxCQLLibraryData, libraryData.value)
  }

  @Test
  fun testLoadMeasureEvaluateLibraryToggleButtons(){
    every { patientDetailsFragment.buttonCQLMeasureEvaluateStartSetOnClickListener() } returns Unit
    every { patientDetailsFragment.measureReportingEditTextPeriodsSetOnClickListener() } returns Unit
    patientDetailsFragment.loadMeasureEvaluateLibraryToggleButtons()
    Assert.assertEquals(true,patientDetailsFragment.button_CQLEvaluate.isEnabled)
  }

  @Test
  fun testLoadMeasureEvaluateLibrary() {

    every { patientDetailsFragment.dir.exists() } returns true
    every { patientDetailsFragment.fileUtil
      .readFileFromInternalStorage(any(),any(),any()) } returns valueSetData
    every { patientDetailsFragment.loadMeasureEvaluateLibraryToggleButtons() } returns Unit

    patientDetailsFragment.loadMeasureEvaluateLibrary()

    Assert.assertNotNull(patientDetailsFragment.libraryMeasure)

    every { patientDetailsFragment.dir.exists() } returns false
    val auxCQLMeasureEvaluateData = "loadMeasureEvaluateLibraryData"
    val libraMeasureEvaluateData = MutableLiveData<String>()
    libraMeasureEvaluateData.postValue(auxCQLMeasureEvaluateData)
    coroutinesTestRule.runBlockingTest {
      coEvery {
        patientDetailsViewModel.fetchCQLMeasureEvaluateLibraryAndValueSets(
          parser,
          fhirResourceDataSource,
          any(),
          any(),
          any()
        )
      } returns libraMeasureEvaluateData
    }
    patientDetailsFragment.loadMeasureEvaluateLibrary()
    Assert.assertNotNull(libraMeasureEvaluateData.value)
    Assert.assertEquals(auxCQLMeasureEvaluateData, libraMeasureEvaluateData.value)
  }

  @Test
  fun testLoadCQLHelperData() {

    every { patientDetailsFragment.dir.exists() } returns true
    every { patientDetailsFragment.loadCQLLibrarySources() } returns Unit
    every { patientDetailsFragment.loadCQLValueSetData() } returns Unit

    patientDetailsFragment.loadCQLHelperData()

    every { patientDetailsFragment.dir.exists() } returns false
    val auxCQLHelperData = "auxCQLHelperData"
    val helperData = MutableLiveData<String>()
    helperData.postValue(auxCQLHelperData)

    coroutinesTestRule.runBlockingTest {
      coEvery {
        patientDetailsViewModel.fetchCQLFhirHelperData(parser, fhirResourceDataSource, any())
      } returns helperData
    }
    patientDetailsFragment.loadCQLHelperData()
    Assert.assertNotNull(helperData.value)
    Assert.assertEquals(auxCQLHelperData, helperData.value)
  }

  @Test
  fun testLoadCQLValueSetData() {

    every { patientDetailsFragment.dir.exists() } returns true
    every { patientDetailsFragment.postValueSetData(any()) } returns Unit

    patientDetailsFragment.loadCQLValueSetData()

    every { patientDetailsFragment.dir.exists() } returns false
    val auxCQLValueSetData = "auxCQLValueSetData"
    val valueSetData = MutableLiveData<String>()
    valueSetData.postValue(auxCQLValueSetData)

    coroutinesTestRule.runBlockingTest {
      coEvery {
        patientDetailsViewModel.fetchCQLValueSetData(parser, fhirResourceDataSource, any())
      } returns valueSetData
    }
    patientDetailsFragment.loadCQLValueSetData()
    Assert.assertNotNull(valueSetData.value)
    Assert.assertEquals(auxCQLValueSetData, valueSetData.value)

  }


  @Test
  fun testLoadCQLMeasurePatientData() {
    val auxCQLPatientData = "auxCQLPatientData"
    val patientData = MutableLiveData<String>()
    patientData.postValue(auxCQLPatientData)

    coroutinesTestRule.runBlockingTest {
      coEvery {
        patientDetailsViewModel.fetchCQLPatientData(parser, fhirResourceDataSource, any())
      } returns patientData
    }
    patientDetailsFragment.loadCQLMeasurePatientData()
    Assert.assertNotNull(patientData.value)
    Assert.assertEquals(auxCQLPatientData, patientData.value)
  }

  @Test
  fun showCQLCardTest() {
    val ANC_TEST_PATIENT_ID = "e8725b4c-6db0-4158-a24d-50a5ddf1c2ed"
    patientDetailsFragment.patientId = ANC_TEST_PATIENT_ID
    every { patientDetailsFragment.buttonCQLSetOnClickListener() } returns Unit
    every { patientDetailsFragment.buttonCQLMeasureEvaluateSetOnClickListener() } returns Unit
    patientDetailsFragment.showCQLCard()
    Assert.assertEquals(patientDetailsFragment.textView_EvaluateCQLHeader.visibility, View.VISIBLE)
    Assert.assertEquals(ANC_TEST_PATIENT_ID, patientDetailsFragment.patientId)
  }

  @Test
  fun testButtonCQLMeasureEvaluateStartSetOnClickListener(){
    patientDetailsFragment.buttonCQLMeasureEvaluateStartSetOnClickListener()
    Assert.assertEquals(true, patientDetailsFragment.button_CQL_Measure_Evaluate_Start.hasOnClickListeners())
  }

  @Test
  fun testButtonCQLSetOnClickListener() {
    every { patientDetailsFragment.parametersCQLToggleFinalView() } returns Unit
    patientDetailsFragment.buttonCQLSetOnClickListener()
    Assert.assertEquals(true, patientDetailsFragment.button_CQLEvaluate.hasOnClickListeners())
  }

  @Test
  fun testButtonCQLMeasureEvaluateSetOnClickListener() {
    every { patientDetailsFragment.parametersCQLMeasureToggleFinalView() } returns Unit
    patientDetailsFragment.buttonCQLMeasureEvaluateSetOnClickListener()
    Assert.assertEquals(
      true,
      patientDetailsFragment.button_CQL_Measure_Evaluate.hasOnClickListeners()
    )
  }

  @Test
  fun testMeasureReportingEditTextPeriodsSetOnClickListener(){
    patientDetailsFragment.measureReportingEditTextPeriodsSetOnClickListener()
    Assert.assertEquals(
      true,
      patientDetailsFragment.editText_measure_reporting_date_from.hasOnClickListeners()
    )

    Assert.assertEquals(
      true,
      patientDetailsFragment.editText_measure_reporting_date_to.hasOnClickListeners()
    )
  }

  @Test
  fun testOnclickListenerEditTextMeasureReporting(){
    patientDetailsFragment.onclickListenerEditTextMeasureReporting()
  }

  @Test
  fun testUpdateMeasureReportingDateEditTextAndParams(){
    patientDetailsFragment.editTextMeasureReportingDateClicked=1
    patientDetailsFragment.updateMeasureReportingDateEditTextAndParams()

    Assert.assertNotNull(patientDetailsFragment.editText_measure_reporting_date_from)

    patientDetailsFragment.editTextMeasureReportingDateClicked=0
    patientDetailsFragment.updateMeasureReportingDateEditTextAndParams()

    Assert.assertNotNull(patientDetailsFragment.editText_measure_reporting_date_to)
  }

  @Test
  fun testHandleParametersQCLMeasure() {
    val dummyJson = "{ \"id\": 0, \"name\": \"Dominique Prince\" }"
    val jsonObject = JSONObject(dummyJson)
    val auxText = jsonObject.toString(4)

    patientDetailsFragment.handleParametersQCLMeasure(dummyJson)
    Assert.assertEquals(patientDetailsFragment.textView_CQLResults.text, auxText)
  }

  @Test
  fun testParametersQCLToggleFinalView() {
  every { patientDetailsFragment.handleCQL() } returns parameters
  every { patientDetailsFragment.handleParametersQCLMeasure(any()) } returns Unit
    patientDetailsFragment.parametersCQLToggleFinalView()
    Assert.assertEquals(true, patientDetailsFragment.button_CQL_Measure_Evaluate.isEnabled)
  }

  @Test
  fun testParametersCQLMeasureToggleFinalView() {
    every { patientDetailsFragment.handleMeasureEvaluate() } returns parameters
    every { patientDetailsFragment.handleParametersQCLMeasure(any()) } returns Unit
    patientDetailsFragment.parametersCQLMeasureToggleFinalView()
    Assert.assertEquals(false, patientDetailsFragment.button_CQLEvaluate.isEnabled)
  }

  @Test
  fun testHandleMeasureEvaluateLibrary(){
    every { patientDetailsFragment.dir.exists() } returns true
    every { patientDetailsFragment.fileUtil
      .writeFileOnInternalStorage(any(),any(),any(),any()) } returns Unit
    every { patientDetailsFragment.buttonCQLMeasureEvaluateStartSetOnClickListener() } returns Unit
    every { patientDetailsFragment.measureReportingEditTextPeriodsSetOnClickListener() } returns Unit
    every { patientDetailsFragment.toggleViewEndLoadCQLMeasureData() } returns Unit
    patientDetailsFragment.handleMeasureEvaluateLibrary(valueSetData)

    Assert.assertNotNull(patientDetailsFragment.libraryMeasure)
  }

  @Test
  fun testToggleViewStartLoadCQLMeasureData(){
    patientDetailsFragment.toggleViewStartLoadCQLMeasureData()
    Assert.assertEquals(false,patientDetailsFragment.button_CQLEvaluate.isEnabled)
  }

  @Test
  fun testToggleViewEndLoadCQLMeasureData(){
    patientDetailsFragment.toggleViewEndLoadCQLMeasureData()
    Assert.assertEquals(true,patientDetailsFragment.button_CQLEvaluate.isEnabled)
  }

}
