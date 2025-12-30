package com.forestguard.app.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forestguard.app.data.AppwriteClient
import com.forestguard.app.data.AuthRepository
import com.forestguard.app.data.ReportRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val account = AppwriteClient.account
    private val reportRepository = ReportRepository()
    private val authRepository = AuthRepository()

    private val _userName = mutableStateOf("Loading...")
    val userName: State<String> = _userName

    private val _userEmail = mutableStateOf("")
    val userEmail: State<String> = _userEmail

    // State Foto Profil
    private val _avatarUrl = mutableStateOf<String?>(null)
    val avatarUrl: State<String?> = _avatarUrl

    private val _reportCount = mutableStateOf(0)
    val reportCount: State<Int> = _reportCount

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                // 1. Ambil Data User (Nama & Foto)
                val (name, avatar) = authRepository.getUserData()
                val user = account.get()

                _userName.value = name
                _userEmail.value = user.email
                _avatarUrl.value = avatar // Set URL Foto

                // 2. Hitung Laporan
                val reports = reportRepository.getReports()
                val myReports = reports.filter { it.userId == user.id }
                _reportCount.value = myReports.size

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // FITUR GANTI FOTO
    fun updateAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.uploadProfilePicture(context, uri)
            _isLoading.value = false

            result.onSuccess { newUrl ->
                _avatarUrl.value = newUrl
                Toast.makeText(context, "Foto Profil Diperbarui!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateName(newName: String, context: Context) {
        viewModelScope.launch {
            try {
                account.updateName(newName)
                _userName.value = newName
                Toast.makeText(context, "Nama berhasil diubah!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                account.deleteSession("current")
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Gagal")
            }
        }
    }

    fun exportToCSV(context: Context) {
        Toast.makeText(context, "Sedang mengekspor data...", Toast.LENGTH_SHORT).show()
    }
}