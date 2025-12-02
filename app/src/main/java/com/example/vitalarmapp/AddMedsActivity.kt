@file:Suppress("SpellCheckingInspection")

package com.example.vitalarmapp

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalarmapp.adapters.MedicationSearchAdapter
import com.example.vitalarmapp.adapters.MedicationSearchItem
import com.example.vitalarmapp.databinding.ActivityAddMedsBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.search.SearchView
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.LinkedHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AddMedsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedsBinding
    private val gson: Gson by lazy { Gson() }
    private val defaultCardTopMargin by lazy {
        resources.getDimensionPixelSize(R.dimen.add_meds_card_margin_with_search)
    }
    private val selectionCardTopMargin by lazy {
        resources.getDimensionPixelSize(R.dimen.add_meds_card_margin_with_selection)
    }
    private var searchJob: Job? = null
    private var translationJob: Job? = null
    private val translationClient: Translator by lazy {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()
        Translation.getClient(options)
    }
    private lateinit var searchAdapter: MedicationSearchAdapter
    private var selectedMedication: MedicationSearchItem? = null
    private var displayedMedication: MedicationSearchItem? = null
    private var ignoreQueryChanges: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupSearchBar()
        setupSearch()
        setupSelectionAppBar()
        setupAddAction()
        updateSelectedMedicationCardSpacing(false)
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
                    if (selectedMedication != null) {
                        binding.selectedMedicationCard.isVisible = true
                    }
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
            addItemDecoration(
                DividerItemDecoration(
                    this@AddMedsActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        binding.searchView.editText.addTextChangedListener { editable ->
            if (ignoreQueryChanges) return@addTextChangedListener
            onQueryChanged(editable?.toString().orEmpty())
        }

        updateAddMedicationState()
    }

    private fun setupAddAction() {
        binding.addMedicationButton.setOnClickListener {
            val medication = displayedMedication ?: return@setOnClickListener
            val saved = saveMedicationLocally(medication)
            if (saved) {
                showAddConfirmationDialog()
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.add_meds_save_error,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupSelectionAppBar() {
        binding.selectionTopAppBar.setNavigationOnClickListener {
            exitSelectionMode()
        }

        binding.selectionTopAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_selection -> {
                    removeSelectedMedication()
                    true
                }

                else -> false
            }
        }

        binding.selectedMedicationCard.setOnLongClickListener {
            if (selectedMedication != null) {
                enterSelectionMode()
                true
            } else {
                false
            }
        }
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
        displayedMedication = null
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
        ignoreQueryChanges = true
        selectedMedication = item
        displayedMedication = item
        binding.searchView.editText.setText("")
        binding.searchBar.setText("")
        binding.searchView.hide()
        binding.searchView.editText.clearFocus()
        binding.searchBar.clearFocus()
        hideKeyboard()
        ignoreQueryChanges = false
        showSelectedMedicationCard(item)
        updateAddMedicationState()
    }

    private fun showSelectedMedicationCard(item: MedicationSearchItem) {
        binding.selectedMedicationCard.isVisible = true
        val formattedName = formatCardText(item.name)
        binding.selectedMedicationName.text = formattedName.orEmpty()
        updateSelectedMedicationCardSpacing(false)

        val indicationText = formatCardText(item.indication)
        binding.selectedMedicationIndication.isVisible = indicationText != null
        binding.selectedMedicationIndication.text = indicationText ?: ""

        val pathologyText = formatCardText(item.pharmacology)
        binding.selectedMedicationPathology.isVisible = pathologyText != null
        binding.selectedMedicationPathology.text = pathologyText ?: ""

        val routeText = formatCardText(item.route)
        binding.selectedMedicationRouteContainer.isVisible = routeText != null
        binding.selectedMedicationRoute.text = routeText ?: ""

        val substanceText = formatCardText(item.substance)
        binding.selectedMedicationSubstanceContainer.isVisible = substanceText != null
        binding.selectedMedicationSubstance.text = substanceText ?: ""

        updateDisplayedMedication(
            item,
            formattedName,
            indicationText,
            pathologyText,
            routeText,
            substanceText
        )

        translateSelectedMedicationCard(item)
    }

    private fun translateSelectedMedicationCard(item: MedicationSearchItem) {
        if (!shouldTranslateToSpanishUnitedStates()) return

        translationJob?.cancel()
        translationJob = lifecycleScope.launch {
            val downloadResult = runCatching { ensureTranslatorReady() }

            if (downloadResult.isFailure || selectedMedication != item) return@launch

            val translatedName = translateText(item.name)
            val translatedIndication = translateText(item.indication)
            val translatedPharmacology = translateText(item.pharmacology)
            val translatedRoute = translateText(item.route)
            val translatedSubstance = translateText(item.substance)

            if (selectedMedication != item) return@launch

            val finalName = translatedName ?: binding.selectedMedicationName.text?.toString()
            binding.selectedMedicationName.text = finalName

            val indicationText = translatedIndication ?: formatCardText(item.indication)
            binding.selectedMedicationIndication.isVisible = indicationText != null
            binding.selectedMedicationIndication.text = indicationText ?: ""

            val pathologyText = translatedPharmacology ?: formatCardText(item.pharmacology)
            binding.selectedMedicationPathology.isVisible = pathologyText != null
            binding.selectedMedicationPathology.text = pathologyText ?: ""

            val routeText = translatedRoute ?: formatCardText(item.route)
            binding.selectedMedicationRouteContainer.isVisible = routeText != null
            binding.selectedMedicationRoute.text = routeText ?: ""

            val substanceText = translatedSubstance ?: formatCardText(item.substance)
            binding.selectedMedicationSubstanceContainer.isVisible = substanceText != null
            binding.selectedMedicationSubstance.text = substanceText ?: ""

            updateDisplayedMedication(
                item,
                finalName,
                indicationText,
                pathologyText,
                routeText,
                substanceText
            )
        }
    }

    private fun shouldTranslateToSpanishUnitedStates(): Boolean {
        val locale = resources.configuration.locales.get(0)
        return locale.language.equals("es", ignoreCase = true) &&
            locale.country.equals("US", ignoreCase = true)
    }

    private suspend fun ensureTranslatorReady() {
        withContext(Dispatchers.IO) {
            val conditions = DownloadConditions.Builder().build()
            translationClient.downloadModelIfNeeded(conditions).await()
        }
    }

    private suspend fun translateText(value: String?): String? {
        val normalized = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { translationClient.translate(normalized).await() }
            .getOrNull()
            ?.let { formatCardText(it) }
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
        val hasSelection = displayedMedication != null
        binding.addMedicationButton.isEnabled = binding.progressBar.isVisible.not() && hasSelection
        binding.selectedMedicationCard.isVisible = hasSelection
    }

    private fun updateSelectedMedicationCardSpacing(isSelectionMode: Boolean) {
        val newTopMargin = if (isSelectionMode) selectionCardTopMargin else defaultCardTopMargin
        val layoutParams = binding.selectedMedicationCard.layoutParams as ConstraintLayout.LayoutParams

        if (layoutParams.topMargin != newTopMargin) {
            layoutParams.topMargin = newTopMargin
            binding.selectedMedicationCard.layoutParams = layoutParams
        }
    }

    private fun enterSelectionMode() {
        binding.searchBar.visibility = View.GONE
        binding.selectionTopAppBar.visibility = View.VISIBLE
        binding.selectionTopAppBar.title = getString(R.string.add_meds_selection_count, 1)
        updateSelectedMedicationCardSpacing(true)
    }

    private fun exitSelectionMode() {
        binding.selectionTopAppBar.visibility = View.GONE
        binding.searchBar.visibility = View.VISIBLE
        updateSelectedMedicationCardSpacing(false)
    }

    private fun removeSelectedMedication() {
        val removedMedicationName = selectedMedication?.name
        selectedMedication = null
        displayedMedication = null
        binding.selectedMedicationName.text = null
        binding.selectedMedicationIndication.text = null
        binding.selectedMedicationPathology.text = null
        binding.selectedMedicationRoute.text = null
        binding.selectedMedicationSubstance.text = null
        binding.selectedMedicationCard.isVisible = false
        binding.searchBar.setText("")
        exitSelectionMode()
        updateAddMedicationState()

        removedMedicationName?.let { name ->
            Snackbar.make(
                binding.root,
                getString(R.string.add_meds_selection_removed_message, name),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {
        searchJob?.cancel()
        translationJob?.cancel()
        translationClient.close()
        super.onDestroy()
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, AddMedsActivity::class.java)
    }

    private fun OpenFdaResponse?.toMedicationItems(): List<MedicationSearchItem> {
        val uniqueItems = LinkedHashMap<String, MedicationSearchItem>()

        this?.results.orEmpty().forEach { result ->
            result.patient?.drug.orEmpty().forEach { drug ->
                val rawName =
                    drug.displayName()?.trim()?.takeIf { it.isNotBlank() } ?: return@forEach
                val normalizedName = capitalizeName(rawName)
                val indication = drug.drugIndication?.trim().takeIf { it?.isNotBlank() == true }
                val pharmacology = drug.bestDescription()
                val route = drug.openFda?.route?.firstOrNull()?.trim()
                val substance = drug.openFda?.substanceName?.firstOrNull()?.trim()

                if (!uniqueItems.containsKey(rawName.lowercase())) {
                    uniqueItems[rawName.lowercase()] = MedicationSearchItem(
                        normalizedName,
                        indication,
                        pharmacology,
                        route,
                        substance
                    )
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

    private fun Drug.bestDescription(): String? {
        return openFda?.pharmClassEpc?.firstOrNull()
            ?: openFda?.pharmClassMoa?.firstOrNull()
            ?: drugIndication?.takeIf { it.isNotBlank() }
            ?: openFda?.route?.firstOrNull()
    }

    private fun capitalizeName(text: String): String {
        return text.lowercase().replaceFirstChar { char ->
            if (char.isLowerCase() || char.isUpperCase()) char.titlecase() else char.toString()
        }
    }

    private fun formatCardText(value: String?): String? {
        val normalized = value?.trim()?.takeIf { it.isNotBlank() }?.lowercase()
        return normalized?.replaceFirstChar { char ->
            if (char.isLowerCase() || char.isUpperCase()) char.titlecase() else char.toString()
        }
    }

    private fun updateDisplayedMedication(
        base: MedicationSearchItem,
        name: String?,
        indication: String?,
        pharmacology: String?,
        route: String?,
        substance: String?
    ) {
        displayedMedication = MedicationSearchItem(
            name?.takeIf { it.isNotBlank() } ?: base.name,
            indication,
            pharmacology,
            route,
            substance
        )
    }

    private fun saveMedicationLocally(item: MedicationSearchItem): Boolean {
        return runCatching {
            val prefs = getSharedPreferences("medications_prefs", MODE_PRIVATE)
            val type = object : TypeToken<MutableList<MedicationSearchItem>>() {}.type
            val storedJson = prefs.getString("medications_list", "[]")
            val currentList: MutableList<MedicationSearchItem> = runCatching {
                gson.fromJson<MutableList<MedicationSearchItem>>(storedJson, type)
            }.getOrDefault(mutableListOf())

            currentList.add(item)
            val editor = prefs.edit()
            editor.putString("medications_list", gson.toJson(currentList))
            val committed = editor.commit()
            if (!committed) error("Failed to persist medication locally")
        }.isSuccess
    }

    private fun showAddConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_meds_dialog_title))
            .setMessage(getString(R.string.add_meds_dialog_body))
            .setNegativeButton(getString(R.string.add_meds_dialog_add_another)) { _, _ ->
                val restartIntent =
                    Intent(this, AddMedsActivity::class.java).addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(restartIntent)
                finish()
            }
            .setPositiveButton(getString(R.string.add_meds_dialog_return)) { _, _ ->
                startActivity(AddMainTabActivity.intent(this).addFlags(FLAG_ACTIVITY_CLEAR_TOP))
                finish()
            }
            .show()
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }.addOnCanceledListener {
            continuation.cancel()
        }
    }

}

private data class OpenFdaResponse(
    val results: List<DrugEvent>?,
)

private data class DrugEvent(
    val patient: PatientInfo?,
)

private data class PatientInfo(
    val drug: List<Drug>?,
)

private data class Drug(
    @SerializedName("medicinalproduct") val medicinalProduct: String?,
    @SerializedName("drugindication") val drugIndication: String?,
    @SerializedName("openfda") val openFda: OpenFdaDetails?,
)

private data class OpenFdaDetails(
    @SerializedName("generic_name") val genericName: List<String>?,
    @SerializedName("brand_name") val brandName: List<String>?,
    @SerializedName("substance_name") val substanceName: List<String>?,
    @SerializedName("route") val route: List<String>?,
    @SerializedName("pharm_class_epc") val pharmClassEpc: List<String>?,
    @SerializedName("pharm_class_moa") val pharmClassMoa: List<String>?,
)
