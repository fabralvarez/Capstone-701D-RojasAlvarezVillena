package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.adapters.MedicationSearchAdapter
import com.example.vitalarmapp.adapters.MedicationSearchItem
import com.example.vitalarmapp.databinding.ActivityAddMedsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.search.SearchView
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddMedsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedsBinding
    private val gson: Gson by lazy { Gson() }
    private var searchJob: Job? = null
    private lateinit var searchAdapter: MedicationSearchAdapter
    private var selectedMedication: MedicationSearchItem? = null
    private val medicationTranslations: Map<String, String> = mapOf(
        "acetaminophen" to "Paracetamol",
        "ibuprofen" to "Ibuprofeno",
        "aspirin" to "Aspirina",
        "amoxicillin" to "Amoxicilina",
        "omeprazole" to "Omeprazol",
        "metformin" to "Metformina",
        "atorvastatin" to "Atorvastatina",
        "simvastatin" to "Simvastatina",
        "losartan" to "Losartán",
        "lisinopril" to "Lisinopril",
        "hydrochlorothiazide" to "Hidroclorotiazida",
        "albuterol" to "Salbutamol",
        "doxycycline" to "Doxiciclina",
        "clopidogrel" to "Clopidogrel",
        "warfarin" to "Warfarina",
        "furosemide" to "Furosemida",
        "levothyroxine" to "Levotiroxina",
        "insulin" to "Insulina",
        "prednisone" to "Prednisona"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupSearchBar()
        setupSearch()
    }

    private fun setupSearchBar() {
        binding.searchBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_search) {
                openSearchView()
                true
            } else {
                false
            }
        }
    }


    private fun setupSearch() {
        binding.searchView.setupWithSearchBar(binding.searchBar)
        binding.searchBar.setOnClickListener { openSearchView() }

        binding.searchBar.setNavigationOnClickListener {
            if (binding.searchView.isShowing) {
                binding.searchView.hide()
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        binding.searchView.addTransitionListener { _, _, newState ->
            when (newState) {
                SearchView.TransitionState.HIDDEN -> {
                    binding.searchBar.visibility = View.VISIBLE
                    binding.searchView.visibility = View.GONE
                }

                SearchView.TransitionState.SHOWN, SearchView.TransitionState.SHOWING -> {
                    binding.searchBar.visibility = View.GONE
                    binding.searchView.visibility = View.VISIBLE
                }

                else -> Unit
            }
        }
        binding.searchView.editText.hint = getString(R.string.add_meds_search_placeholder)

        searchAdapter = MedicationSearchAdapter(emptyList()) { medication ->
            onMedicationSelected(medication)
        }
        binding.rvMedicationResults.apply {
            layoutManager = LinearLayoutManager(this@AddMedsActivity)
            adapter = searchAdapter
        }

        binding.searchView.editText.addTextChangedListener { editable ->
            onQueryChanged(editable?.toString().orEmpty())
        }

        updateAddMedicationState()
    }

    private fun openSearchView() {
        if (!binding.searchView.isShowing) {
            binding.searchView.show()
        }
        binding.searchView.editText.requestFocus()
        binding.searchView.editText.post {
            val inputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(
                binding.searchView.editText,
                InputMethodManager.SHOW_IMPLICIT
            )
        }
    }

    private fun onQueryChanged(query: String) {
        selectedMedication = null
        searchJob?.cancel()
        showLoading(false)

        if (query.length < 2) {
            searchAdapter.updateData(emptyList())
            showStatusMessage(getString(R.string.add_meds_search_start))
            updateAddMedicationState()
            return
        }

        searchJob = lifecycleScope.launch {
            delay(350)
            searchMedications(query)
        }
    }

    private suspend fun searchMedications(query: String) {
        showLoading(true, getString(R.string.add_meds_search_loading))
        val result = withContext(Dispatchers.IO) {
            runCatching { fetchMedications(query) }
        }

        showLoading(false)

        result.onSuccess { items ->
            if (items.isEmpty()) {
                showStatusMessage(getString(R.string.add_meds_search_no_results, query))
            } else {
                showStatusMessage(null)
            }
            searchAdapter.updateData(items)
        }.onFailure {
            showStatusMessage(getString(R.string.add_meds_search_error))
            searchAdapter.updateData(emptyList())
            Snackbar.make(binding.root, R.string.add_meds_search_error, Snackbar.LENGTH_LONG).show()
        }

        updateAddMedicationState()
    }

    private fun fetchMedications(query: String): List<MedicationSearchItem> {
        val encodedQuery = URLEncoder.encode(
            "patient.drug.medicinalproduct:\"$query\"",
            StandardCharsets.UTF_8.toString()
        )
        val urlBuilder = StringBuilder("https://api.fda.gov/drug/event.json")
            .append("?limit=20&search=")
            .append(encodedQuery)

        if (BuildConfig.OPEN_FDA_API_KEY.isNotBlank()) {
            urlBuilder.append("&api_key=").append(BuildConfig.OPEN_FDA_API_KEY)
        }

        val connection = URL(urlBuilder.toString()).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        return try {
            val responseStream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                throw IllegalStateException("OpenFDA error ${connection.responseCode}")
            }

            responseStream.bufferedReader().use { reader ->
                gson.fromJson(reader, OpenFdaResponse::class.java)
            }.toMedicationItems()
        } finally {
            connection.disconnect()
        }
    }

    private fun onMedicationSelected(item: MedicationSearchItem) {
        selectedMedication = item
        binding.searchBar.text = getString(R.string.add_meds_selected_format, item.name)
        binding.searchView.editText.setText(item.name)
        binding.searchView.hide()
        hideKeyboard()
        updateAddMedicationState()
    }

    private fun showLoading(isLoading: Boolean, status: String? = null) {
        binding.progressBar.isVisible = isLoading
        if (isLoading) {
            showStatusMessage(status)
        }
    }

    private fun showStatusMessage(message: String?) {
        binding.tvSearchStatus.isVisible = !message.isNullOrBlank()
        binding.tvSearchStatus.text = message
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun updateAddMedicationState() {
        val hasSelectionOrText = selectedMedication != null ||
                binding.searchView.editText.text.isNullOrBlank().not()
        binding.addMedicationButton.isEnabled = binding.progressBar.isVisible.not() &&
                hasSelectionOrText
    }

    override fun onDestroy() {
        searchJob?.cancel()
        super.onDestroy()
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, AddMedsActivity::class.java)
    }

    private fun OpenFdaResponse?.toMedicationItems(): List<MedicationSearchItem> {
        val uniqueItems = LinkedHashMap<String, MedicationSearchItem>()

        this?.results.orEmpty().forEach { result ->
            result.patient?.drug.orEmpty().forEach { drug ->
                val rawName = drug.displayName() ?: return@forEach
                val translatedName = translateMedicationName(rawName)
                val displayName = if (translatedName.equals(rawName, ignoreCase = true)) {
                    rawName
                } else {
                    "$translatedName ($rawName)"
                }
                val description = translateDescription(
                    drug.drugIndication?.takeIf { it.isNotBlank() }
                        ?: drug.openFda?.pharmClassEpc?.firstOrNull()
                        ?: drug.openFda?.pharmClassMoa?.firstOrNull()
                        ?: drug.openFda?.route?.firstOrNull()
                )

                if (!uniqueItems.containsKey(displayName)) {
                    uniqueItems[displayName] = MedicationSearchItem(displayName, description)
                }
            }
        }

        return uniqueItems.values.toList()
    }

    private fun Drug.displayName(): String? {
        return openFda?.genericName?.firstOrNull()
            ?: openFda?.brandName?.firstOrNull()
            ?: openFda?.substanceName?.firstOrNull()
            ?: medicinalProduct
    }

    private fun translateMedicationName(name: String): String {
        val normalized = name.lowercase()
        val translated = medicationTranslations.entries.firstOrNull { (english, _) ->
            normalized.contains(english)
        }?.value

        return translated ?: name
    }

    private fun translateDescription(description: String?): String? {
        if (description.isNullOrBlank()) return description

        var translated = description
        medicationTranslations.forEach { (english, spanish) ->
            translated = translated.replace(english, spanish, ignoreCase = true)
        }
        return translated
    }
}

private data class OpenFdaResponse(
    val results: List<DrugEvent>?
)

private data class DrugEvent(
    val patient: PatientInfo?
)

private data class PatientInfo(
    val drug: List<Drug>?
)

private data class Drug(
    @SerializedName("medicinalproduct") val medicinalProduct: String?,
    @SerializedName("drugindication") val drugIndication: String?,
    @SerializedName("openfda") val openFda: OpenFdaDetails?
)

private data class OpenFdaDetails(
    @SerializedName("generic_name") val genericName: List<String>?,
    @SerializedName("brand_name") val brandName: List<String>?,
    @SerializedName("substance_name") val substanceName: List<String>?,
    @SerializedName("route") val route: List<String>?,
    @SerializedName("pharm_class_epc") val pharmClassEpc: List<String>?,
    @SerializedName("pharm_class_moa") val pharmClassMoa: List<String>?
)
