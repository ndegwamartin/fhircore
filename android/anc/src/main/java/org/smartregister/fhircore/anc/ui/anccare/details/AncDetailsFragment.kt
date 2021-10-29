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

import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import com.google.android.fhir.FhirEngine
import kotlinx.android.synthetic.main.fragment_anc_details.*
import org.json.JSONObject
import org.smartregister.fhircore.anc.AncApplication
import org.smartregister.fhircore.anc.R
import org.smartregister.fhircore.anc.data.model.AncOverviewItem
import org.smartregister.fhircore.anc.data.model.AncPatientDetailItem
import org.smartregister.fhircore.anc.data.model.CarePlanItem
import org.smartregister.fhircore.anc.data.model.EncounterItem
import org.smartregister.fhircore.anc.data.model.UpcomingServiceItem
import org.smartregister.fhircore.anc.data.patient.PatientRepository
import org.smartregister.fhircore.anc.databinding.FragmentAncDetailsBinding
import org.smartregister.fhircore.engine.cql.LibraryEvaluator
import org.smartregister.fhircore.engine.cql.MeasureEvaluator
import org.smartregister.fhircore.engine.data.remote.fhir.resource.FhirResourceDataSource
import org.smartregister.fhircore.engine.ui.questionnaire.QuestionnaireActivity
import org.smartregister.fhircore.engine.util.FileUtil
import org.smartregister.fhircore.engine.util.extension.createFactory
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.common.collect.Lists
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.hl7.fhir.instance.model.api.IBaseResource
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder


class AncDetailsFragment : Fragment() {

  lateinit var patientId: String
  private lateinit var fhirEngine: FhirEngine

  lateinit var ancDetailsViewModel: AncDetailsViewModel

  private lateinit var patientRepository: PatientRepository

  private var carePlanAdapter = CarePlanAdapter()

  private val upcomingServicesAdapter = UpcomingServicesAdapter()

  private val lastSeen = EncounterAdapter()

  lateinit var binding: FragmentAncDetailsBinding

  lateinit var parser: IParser

  lateinit var fhirContext: FhirContext

  lateinit var fhirResourceDataSource: FhirResourceDataSource

  lateinit var libraryEvaluator: LibraryEvaluator

  lateinit var measureEvaluator: MeasureEvaluator

  lateinit var fileUtil: FileUtil

  var libraryData = ""
  var helperData = ""
  var valueSetData = ""
  var testData = ""
  val evaluatorId = "ANCRecommendationA2"
  val contextCQL = "patient"
  val contextLabel = "mom-with-anemia"

  var cqlBaseURL = ""
  var libraryURL = ""
  var measureEvaluateLibraryURL = ""
  var measureTypeURL = ""

  var cqlMasureReportURL = ""
  var cqlMeasureReportStartDate = ""
  var cqlMeasureReportEndDate = ""
  var cqlMeasureReportReportType = ""
  var cqlMeasureReportSubject = ""
  var cqlMeasureReportLibInitialString = ""
  var cqlHelperURL = ""
  var valueSetURL = ""
  var patientURL = ""
  var cqlConfigFileName = "configs/cql_configs.properties"

  var parametersEvaluate = MutableLiveData<String>()
  var parametersMeasure = MutableLiveData<String>()

  var allCQLDataLoaded = MutableLiveData<Boolean>()
  var allMeasureEvaluatorLoaded = MutableLiveData<Boolean>()

  lateinit var dir:File

  lateinit var libraryMeasure:IBaseBundle
  var measureEvaluateLibraryData=""

  var editTextMeasureReportingDateClicked=0

  val myCalendar = Calendar.getInstance()
  lateinit var valueSetBundle:IBaseBundle

  val dirCQLDirRoot="cql_libraries"
  val fileNameMainLibraryCql="main_library_cql"
  val fileNameHelperLibraryCql="helper_library_cql"
  val fileNameValueSetLibraryCql="value_set_library_cql"
  val fileNameMeasureLibraryCql="measure_library_cql"

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = DataBindingUtil.inflate(inflater, R.layout.fragment_anc_details, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    patientId = arguments?.getString(QuestionnaireActivity.QUESTIONNAIRE_ARG_PATIENT_KEY) ?: ""
    libraryEvaluator = LibraryEvaluator()
    measureEvaluator = MeasureEvaluator()

    fhirContext=FhirContext.forCached(FhirVersionEnum.R4)
    parser = fhirContext.newJsonParser()

    fhirResourceDataSource = FhirResourceDataSource.getInstance(AncApplication.getContext())

    fhirEngine = AncApplication.getContext().fhirEngine

    setupViews()

    patientRepository = getAncPatientRepository()

    ancDetailsViewModel =
      ViewModelProvider(
        viewModelStore,
        AncDetailsViewModel(patientRepository, patientId = patientId).createFactory()
      )[AncDetailsViewModel::class.java]

    binding.txtViewPatientId.text = patientId

    Timber.d(patientId)

    ancDetailsViewModel
      .fetchDemographics()
      .observe(viewLifecycleOwner, this::handlePatientDemographics)

    ancDetailsViewModel.fetchCarePlan().observe(viewLifecycleOwner, this::handleCarePlan)

    ancDetailsViewModel.fetchObservation().observe(viewLifecycleOwner, this::handleObservation)

    ancDetailsViewModel
      .fetchUpcomingServices()
      .observe(viewLifecycleOwner, this::handleUpcomingServices)
    ancDetailsViewModel.fetchCarePlan().observe(viewLifecycleOwner, this::handleCarePlan)

    fileUtil = FileUtil()
    cqlBaseURL =
      context?.let { fileUtil.getProperty("smart_register_base_url", it, cqlConfigFileName) }!!
    libraryURL =
      cqlBaseURL + context?.let { fileUtil.getProperty("cql_library_url", it, cqlConfigFileName) }
    cqlHelperURL =
      cqlBaseURL +
        context?.let { fileUtil.getProperty("cql_helper_library_url", it, cqlConfigFileName) }
    valueSetURL =
      cqlBaseURL + context?.let { fileUtil.getProperty("cql_value_set_url", it, cqlConfigFileName) }
    patientURL =
      cqlBaseURL + context?.let { fileUtil.getProperty("cql_patient_url", it, cqlConfigFileName) }

    measureEvaluateLibraryURL =
      context?.let {
        fileUtil.getProperty("cql_measure_report_library_value_sets_url", it, cqlConfigFileName)
      }!!

    measureTypeURL =
      context?.let {
        fileUtil.getProperty("cql_measure_report_resource_url", it, cqlConfigFileName)
      }!!

    cqlMasureReportURL =
      context?.let { fileUtil.getProperty("cql_measure_report_url", it, cqlConfigFileName) }!!

    cqlMeasureReportReportType =
      context?.let {
        fileUtil.getProperty("cql_measure_report_report_type", it, cqlConfigFileName)
      }!!

    cqlMeasureReportSubject =
      context?.let { fileUtil.getProperty("cql_measure_report_subject", it, cqlConfigFileName) }!!

    cqlMeasureReportLibInitialString =
      context?.let {
        fileUtil.getProperty("cql_measure_report_lib_initial_string", it, cqlConfigFileName)
      }!!


    showCQLCard()

    ancDetailsViewModel.fetchLastSeen().observe(viewLifecycleOwner, this::handleLastSeen)

    loadCQLMeasurePatientData()

    measureReportingEditTextPeriodsSetOnClickListener()

    buttonCQLMeasureEvaluateStartSetOnClickListener()

  }

  private fun handleObservation(ancOverviewItem: AncOverviewItem) {
    binding.txtViewEDDDoseDate.text = ancOverviewItem.edd
    binding.txtViewGAPeriod.text = ancOverviewItem.ga
    binding.txtViewFetusesCount.text = ancOverviewItem.noOfFetuses
    binding.txtViewRiskValue.text = ancOverviewItem.risk
  }

  private fun handleUpcomingServices(listEncounters: List<UpcomingServiceItem>) {
    when {
      listEncounters.isEmpty() -> {
        binding.txtViewNoUpcomingServices.visibility = View.VISIBLE
        binding.upcomingServicesListView.visibility = View.GONE
        binding.txtViewUpcomingServicesSeeAllHeading.visibility = View.GONE
        binding.imageViewUpcomingServicesSeeAllArrow.visibility = View.GONE
      }
      else -> {
        binding.txtViewNoUpcomingServices.visibility = View.GONE
        binding.upcomingServicesListView.visibility = View.VISIBLE
        binding.txtViewUpcomingServicesSeeAllHeading.visibility = View.VISIBLE
        binding.txtViewUpcomingServicesSeeAllHeading.visibility = View.VISIBLE
        populateUpcomingServicesList(listEncounters)
      }
    }
  }

  private fun handleLastSeen(listEncounters: List<EncounterItem>) {
    when {
      listEncounters.isEmpty() -> {
        binding.txtViewNoLastSeenServices.visibility = View.VISIBLE
        binding.lastSeenListView.visibility = View.GONE
      }
      else -> {
        binding.txtViewNoLastSeenServices.visibility = View.GONE
        binding.lastSeenListView.visibility = View.VISIBLE
        populateLastSeenList(listEncounters)
      }
    }
  }

  private fun setupViews() {
    binding.carePlanListView.apply {
      adapter = carePlanAdapter
      layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    binding.upcomingServicesListView.apply {
      adapter = upcomingServicesAdapter
      layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    binding.lastSeenListView.apply {
      adapter = lastSeen
      layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }
  }

  companion object {
    fun newInstance(bundle: Bundle = Bundle()) = AncDetailsFragment().apply { arguments = bundle }
  }

  private fun handlePatientDemographics(patient: AncPatientDetailItem) {
    with(patient) {
      val patientDetails =
        this.patientDetails.name +
          ", " +
          this.patientDetails.gender +
          ", " +
          this.patientDetails.age
      val patientId =
        this.patientDetailsHead.demographics + " ID: " + this.patientDetails.patientIdentifier
      binding.txtViewPatientDetails.text = patientDetails
      binding.txtViewPatientId.text = patientId
    }
  }

  private fun handleCarePlan(immunizations: List<CarePlanItem>) {
    when {
      immunizations.isEmpty() -> {
        binding.txtViewNoCarePlan.visibility = View.VISIBLE
        binding.txtViewCarePlanSeeAllHeading.visibility = View.GONE
        binding.imageViewSeeAllArrow.visibility = View.GONE
        binding.carePlanListView.visibility = View.GONE
      }
      else -> {
        binding.txtViewNoCarePlan.visibility = View.GONE
        binding.txtViewCarePlanSeeAllHeading.visibility = View.VISIBLE
        binding.imageViewSeeAllArrow.visibility = View.VISIBLE
        binding.carePlanListView.visibility = View.VISIBLE
        populateImmunizationList(immunizations)
      }
    }
  }

  private fun populateImmunizationList(listCarePlan: List<CarePlanItem>) {
    carePlanAdapter.submitList(listCarePlan)
  }

  private fun populateUpcomingServicesList(upcomingServiceItem: List<UpcomingServiceItem>) {
    upcomingServicesAdapter.submitList(upcomingServiceItem)
  }
  private fun populateLastSeenList(upcomingServiceItem: List<EncounterItem>) {
    lastSeen.submitList(upcomingServiceItem)
  }

    fun buttonCQLMeasureEvaluateStartSetOnClickListener(){
        button_CQL_Measure_Evaluate_Start.setOnClickListener {
           linearLayout_measure_reporting_dates.visibility=View.VISIBLE
           textView_CQLResults.visibility= View.GONE
        }
    }

  fun buttonCQLSetOnClickListener() {
    button_CQLEvaluate.setOnClickListener {
      button_CQL_Measure_Evaluate.isEnabled = false
      startProgressBarAndTextViewCQLResults()
      linearLayout_measure_reporting_dates.visibility=View.GONE
      allCQLDataLoaded.observe(
        viewLifecycleOwner, this::parametersCQLToggleFinalView)
    }
  }

  fun buttonCQLMeasureEvaluateSetOnClickListener() {
    button_CQL_Measure_Evaluate.setOnClickListener {
      linearLayout_measure_reporting_dates.visibility= View.GONE
      button_CQLEvaluate.isEnabled = false
      startProgressBarAndTextViewCQLResults()
      allMeasureEvaluatorLoaded.observe(
        viewLifecycleOwner, this::parametersCQLMeasureToggleFinalView)
    }
  }

  fun startProgressBarAndTextViewCQLResults() {
    progress_circular_cql.visibility = View.VISIBLE
    textView_CQLResults.visibility = View.GONE
  }

  fun loadCQLLibraryData() {
    dir = File(context?.getFilesDir(), "cql_libraries/main_library_cql")
    if (dir.exists()) {
      libraryData = context?.let { readFileFromInternalStorage(
        it,
        fileNameMainLibraryCql,
        dirCQLDirRoot) }.toString()
      loadCQLHelperData()
    }else {
      ancDetailsViewModel
        .fetchCQLLibraryData(parser, fhirResourceDataSource, libraryURL)
        .observe(viewLifecycleOwner, this::handleCQLLibraryData)
    }
  }

  fun loadCQLHelperData() {

    dir = File(context?.getFilesDir(), "cql_libraries/helper_library_cql")

    if (dir.exists()) {
      helperData = context?.let { readFileFromInternalStorage(
        it,
        fileNameHelperLibraryCql,
        dirCQLDirRoot) }.toString()

      loadCQLLibrarySources()

      loadCQLValueSetData()

    }else {
      ancDetailsViewModel
        .fetchCQLFhirHelperData(parser, fhirResourceDataSource, cqlHelperURL)
        .observe(viewLifecycleOwner, this::handleCQLHelperData)
    }

  }

  fun loadCQLValueSetData() {

    dir = File(context?.getFilesDir(), "cql_libraries/value_set_library_cql")

    if (dir.exists()) {
      valueSetData = context?.let { readFileFromInternalStorage(
        it,
        fileNameValueSetLibraryCql,
        dirCQLDirRoot) }.toString()
        postValueSetData(valueSetData)
    }else {
      ancDetailsViewModel
        .fetchCQLValueSetData(parser, fhirResourceDataSource, valueSetURL)
        .observe(viewLifecycleOwner, this::handleCQLValueSetData)
    }

  }

  fun postValueSetData(valueSetData:String){
    val valueSetStream: InputStream = ByteArrayInputStream(valueSetData.toByteArray())
    valueSetBundle = parser.parseResource(valueSetStream) as IBaseBundle
    allCQLDataLoaded.postValue(true)
  }


  fun loadMeasureEvaluateLibrary() {

    dir = File(context?.getFilesDir(), "cql_libraries/measure_library_cql")
    if (dir.exists()) {
      measureEvaluateLibraryData = context?.let { readFileFromInternalStorage(
        it,
        fileNameMeasureLibraryCql,
        dirCQLDirRoot) }.toString()

      val libraryStreamMeasure: InputStream =
        ByteArrayInputStream(measureEvaluateLibraryData.toByteArray())
      libraryMeasure = parser.parseResource(libraryStreamMeasure) as IBaseBundle
      allMeasureEvaluatorLoaded.postValue(true)
    }else {
      ancDetailsViewModel
        .fetchCQLMeasureEvaluateLibraryAndValueSets(
          parser,
          fhirResourceDataSource,
          measureEvaluateLibraryURL,
          measureTypeURL,
          cqlMeasureReportLibInitialString
        )
        .observe(viewLifecycleOwner, this::handleMeasureEvaluateLibrary)
    }

  }



  fun loadCQLMeasurePatientData(){
    ancDetailsViewModel
      .fetchCQLPatientData(parser, fhirResourceDataSource, "$patientURL$patientId/\$everything")
      .observe(viewLifecycleOwner, this::handleCQLMeasureLoadPatient)
  }


  fun handleCQLMeasureLoadPatient(auxPatientData:String){
    setPatientTestData(auxPatientData)
      loadCQLLibraryData()
      loadMeasureEvaluateLibrary()
  }


  fun handleCQLLibraryData(auxLibraryData: String) {
      libraryData = auxLibraryData
      context?.let {
        writeFileOnInternalStorage(
          it,
          fileNameMainLibraryCql,
          libraryData,
          dirCQLDirRoot)
    }
    loadCQLHelperData()
  }

  fun loadCQLLibrarySources(){
    val libraryStream: InputStream = ByteArrayInputStream(libraryData.toByteArray())
    val fhirHelpersStream: InputStream = ByteArrayInputStream(helperData.toByteArray())

    val library = parser.parseResource(libraryStream)
    val fhirHelpersLibrary = parser.parseResource(fhirHelpersStream)
    libraryResources= Lists.newArrayList(library, fhirHelpersLibrary)
  }

  lateinit var libraryResources: List<IBaseResource>


  fun handleCQLHelperData(auxHelperData: String) {

      helperData = auxHelperData
      context?.let {
        writeFileOnInternalStorage(
          it,
          fileNameHelperLibraryCql,
          helperData,
          dirCQLDirRoot
        )
      }

    loadCQLLibrarySources()

    loadCQLValueSetData()
  }

  fun handleCQLValueSetData(auxValueSetData: String) {

      valueSetData = auxValueSetData
      context?.let {
        writeFileOnInternalStorage(
          it,
          fileNameValueSetLibraryCql,
          valueSetData,
          dirCQLDirRoot
        )
      }

    postValueSetData(valueSetData)

  }

  fun handleCQL():String {
    return libraryEvaluator.runCql(
        libraryResources,
        valueSetBundle,
        patientDataIBase,
        fhirContext,
        evaluatorId,
        contextCQL,
        contextLabel
      )
  }

  var patientResourcesIBase = ArrayList<IBaseResource>()
  lateinit var patientDataIBase:IBaseBundle

  fun setPatientTestData(auxPatientData: String){
    if(testData.isEmpty()) {
      testData = libraryEvaluator.processCQLPatientBundle(auxPatientData)
      val patientDataStream: InputStream = ByteArrayInputStream(testData.toByteArray())
      patientDataIBase = parser.parseResource(patientDataStream) as IBaseBundle
      patientResourcesIBase.add(patientDataIBase)
    }
  }

  fun handleMeasureEvaluate() :String {
    var patientDetailsData=(binding.txtViewPatientDetails.text) as String
    return measureEvaluator.runMeasureEvaluate(
        patientResourcesIBase,
        libraryMeasure,
        fhirContext,
        cqlMasureReportURL,
        cqlMeasureReportStartDate,
        cqlMeasureReportEndDate,
        cqlMeasureReportReportType,
        patientDetailsData.
        substring(0 , patientDetailsData.indexOf(","))
    )
  }
  fun parametersCQLToggleFinalView(allCQLDataLoaded:Boolean) {
    if (allCQLDataLoaded) {
      linearLayout_measure_reporting_dates.visibility = View.GONE
      handleParametersQCLMeasure(handleCQL())
      button_CQL_Measure_Evaluate.isEnabled = true
    }
  }

  fun parametersCQLMeasureToggleFinalView(allMeasureReportDataLoaded:Boolean) {
    if (allMeasureReportDataLoaded) {
      handleParametersQCLMeasure(handleMeasureEvaluate())
      button_CQLEvaluate.isEnabled = true
    }
  }

  fun handleParametersQCLMeasure(parameters: String) {
    val jsonObject = JSONObject(parameters)
    textView_CQLResults.text = jsonObject.toString(4)
    progress_circular_cql.visibility = View.GONE
    textView_CQLResults.visibility = View.VISIBLE
  }


  fun handleMeasureEvaluateLibrary(auxMeasureEvaluateLibData: String) {

    measureEvaluateLibraryData = auxMeasureEvaluateLibData
      context?.let {
        writeFileOnInternalStorage(
          it,
          fileNameMeasureLibraryCql,
          measureEvaluateLibraryData,
          dirCQLDirRoot
        )
    }

    val libraryStreamMeasure: InputStream =
      ByteArrayInputStream(measureEvaluateLibraryData.toByteArray())
    libraryMeasure = parser.parseResource(libraryStreamMeasure) as IBaseBundle
    allMeasureEvaluatorLoaded.postValue(true)

  }

  val ANC_TEST_PATIENT_ID = "e8725b4c-6db0-4158-a24d-50a5ddf1c2ed"
  fun showCQLCard() {
    if (patientId == ANC_TEST_PATIENT_ID) {
      textView_EvaluateCQLHeader.visibility = View.VISIBLE
      cardView_CQLSection.visibility = View.VISIBLE
      buttonCQLSetOnClickListener()
      buttonCQLMeasureEvaluateSetOnClickListener()
    }
  }

  fun getAncPatientRepository(): PatientRepository {
    return PatientRepository(
      (requireActivity().application as AncApplication).fhirEngine,
      AncPatientItemMapper
    )
  }

  var date =
    OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
      myCalendar.set(Calendar.YEAR, year)
      myCalendar.set(Calendar.MONTH, monthOfYear)
      myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
      updateMeasureReportingDateEditTextAndParams()
    }


  fun measureReportingEditTextPeriodsSetOnClickListener() {

    editText_measure_reporting_date_from.setOnClickListener {
      editTextMeasureReportingDateClicked=1
      onclickListenerEditTextMeasureReporting()
    }

    editText_measure_reporting_date_to.setOnClickListener {
      editTextMeasureReportingDateClicked=2
      onclickListenerEditTextMeasureReporting()
    }

  }

  fun onclickListenerEditTextMeasureReporting(){
    context?.let { it1 ->
      DatePickerDialog(
        it1,
        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
        date, myCalendar[Calendar.YEAR],
        myCalendar[Calendar.MONTH],
        myCalendar[Calendar.DAY_OF_MONTH]
      ).show()
    }
  }


  private fun updateMeasureReportingDateEditTextAndParams() {
    val myFormat = "dd/MM/yy"
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    val dateTime=sdf.format(myCalendar.time);
    if(editTextMeasureReportingDateClicked==1) {
      editText_measure_reporting_date_from.setText(dateTime)
      cqlMeasureReportStartDate=dateTime
    }else{
      editText_measure_reporting_date_to.setText(dateTime)
      cqlMeasureReportEndDate=dateTime

    }
  }

  fun writeFileOnInternalStorage(context: Context,
                                 fileName: String?,
                                 body: String?,
                                 dirName:String) {

    val dir = File(context.getFilesDir(), dirName)
    if (!dir.exists()) {
      dir.mkdir()
    }
    try {
      val file = File(dir, fileName)
      val writer = FileWriter(file)
      writer.append(body)
      writer.flush()
      writer.close()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun readFileFromInternalStorage(context: Context,
                                 fileName: String?,
                                 dirName:String):String{
    val dir = File(context.getFilesDir(), dirName)
    val sb = StringBuilder()
    try {
      val file = File(dir, fileName)
      val fis=FileInputStream(file)
      val isr = InputStreamReader(fis)
      val bufferedReader = BufferedReader(isr)
      var line: String?
      while (bufferedReader.readLine().also { line = it } != null) {
        sb.append(line)
      }
      fis.close()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return sb.toString()
  }
}


