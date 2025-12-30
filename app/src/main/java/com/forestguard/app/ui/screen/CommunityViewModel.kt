package com.forestguard.app.ui.screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forestguard.app.data.ReportRepository
import com.forestguard.app.model.Report
import com.forestguard.app.model.Comment
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val repository = ReportRepository()

    private var allReports = listOf<Report>()
    private val _reports = mutableStateOf<List<Report>>(emptyList())
    val reports: State<List<Report>> = _reports

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _currentUserId = mutableStateOf("")
    val currentUserId: State<String> = _currentUserId

    private val _selectedFilter = mutableStateOf("Terbaru")
    val selectedFilter: State<String> = _selectedFilter

    // Komentar
    private val _activeComments = mutableStateOf<List<Comment>>(emptyList())
    val activeComments: State<List<Comment>> = _activeComments
    private val _activeReportId = mutableStateOf<String?>(null)
    val activeReportId: State<String?> = _activeReportId

    // Stats
    val statUserCount = mutableStateOf("0")
    val statDiscussionCount = mutableStateOf("0")

    init {
        loadData()
    }

    fun loadData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _currentUserId.value = repository.getCurrentUserId()
                allReports = repository.getReports()

                statDiscussionCount.value = allReports.size.toString()
                statUserCount.value = allReports.map { it.userId }.distinct().count().toString()

                applyFilter(_selectedFilter.value)
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        applyFilter(filter)
    }

    private fun applyFilter(filter: String) {
        _reports.value = when (filter) {
            "Terverifikasi" -> allReports.filter { it.status == "Terverifikasi" }
            else -> allReports
        }
    }

    fun toggleLike(reportId: String) {
        val userId = _currentUserId.value
        val updatedList = _reports.value.map {
            if (it.id == reportId) {
                val newLikes = it.likedBy.toMutableList()
                if (newLikes.contains(userId)) newLikes.remove(userId) else newLikes.add(userId)
                it.copy(likedBy = newLikes)
            } else it
        }
        _reports.value = updatedList // Update UI

        viewModelScope.launch {
            val target = updatedList.find { it.id == reportId }
            if (target != null) repository.toggleLike(reportId, target.likedBy)
        }
    }

    fun openComments(reportId: String) {
        _activeReportId.value = reportId
        viewModelScope.launch {
            _activeComments.value = repository.getComments(reportId)
        }
    }

    fun closeComments() {
        _activeReportId.value = null
        _activeComments.value = emptyList()
    }

    fun sendComment(content: String) {
        val currentId = _activeReportId.value ?: return
        viewModelScope.launch {
            repository.addComment(currentId, content)
            // Reload comments
            _activeComments.value = repository.getComments(currentId)
        }
    }

    // UPDATE VERIFIKASI (OPTIMISTIC UI)
    fun verifyReport(reportId: String, isVerified: Boolean) {
        val status = if (isVerified) "Terverifikasi" else "Ditolak"

        // 1. Update UI Lokal Langsung
        val updatedList = _reports.value.map {
            if (it.id == reportId) it.copy(status = status) else it
        }
        _reports.value = updatedList

        // 2. Kirim ke Server
        viewModelScope.launch {
            repository.updateReportStatus(reportId, status)
        }
    }

    fun deleteReport(reportId: String) {
        val updatedList = _reports.value.filter { it.id != reportId }
        _reports.value = updatedList
        viewModelScope.launch {
            repository.deleteReport(reportId)
        }
    }
}