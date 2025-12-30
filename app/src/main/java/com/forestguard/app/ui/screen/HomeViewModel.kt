package com.forestguard.app.ui.screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forestguard.app.data.AuthRepository
import com.forestguard.app.data.ReportRepository
import com.forestguard.app.model.Report
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val reportRepository = ReportRepository()
    private val authRepository = AuthRepository()

    // State untuk Data
    private val _userName = mutableStateOf("Ranger")
    val userName: State<String> = _userName

    private val _reportList = mutableStateOf<List<Report>>(emptyList())
    val reportList: State<List<Report>> = _reportList

    private val _totalReports = mutableStateOf(0)
    val totalReports: State<Int> = _totalReports

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Ambil Nama User
            val user = authRepository.getUser()
            _userName.value = user ?: "Tamu"

            // 2. Ambil Daftar Laporan dari Appwrite
            val reports = reportRepository.getReports()
            _reportList.value = reports

            // 3. Hitung Statistik (Contoh sederhana)
            _totalReports.value = reports.size

            _isLoading.value = false
        }
    }
}