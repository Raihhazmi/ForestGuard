package com.forestguard.app.ui.screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forestguard.app.data.ReportRepository
import com.forestguard.app.model.Forest // Import Forest
import com.forestguard.app.model.Report
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {
    private val repository = ReportRepository()

    // Data Laporan User
    private val _reports = mutableStateOf<List<Report>>(emptyList())
    val reports: State<List<Report>> = _reports

    // Data Hutan (Dinamis dari Appwrite)
    private val _forests = mutableStateOf<List<Forest>>(emptyList())
    val forests: State<List<Forest>> = _forests

    private val _activeFilter = mutableStateOf("Hotspot")
    val activeFilter: State<String> = _activeFilter

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            // Ambil Laporan User
            _reports.value = repository.getReports()

            // Ambil Data Hutan
            _forests.value = repository.getForests()
        }
    }

    fun setFilter(filter: String) {
        _activeFilter.value = filter
    }
}