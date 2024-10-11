package io.github.vinceglb.sample.core

import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.coroutineScope
import io.github.vinceglb.filekit.dialog.FileKit
import io.github.vinceglb.filekit.dialog.FileKitDialogSettings
import io.github.vinceglb.filekit.dialog.PickerMode
import io.github.vinceglb.filekit.dialog.PickerType
import io.github.vinceglb.filekit.dialog.pickFile
import io.github.vinceglb.filekit.dialog.saveFile
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.baseName
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val platformSettings: FileKitDialogSettings?
) : ViewModel() {
    private val _uiState = MutableStateFlow(viewModelScope, MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun pickImage() = executeWithLoading {
        // Pick a file
        val file = FileKit.pickFile(
            type = PickerType.Image,
            title = "Custom title here",
            initialDirectory = downloadDirectoryPath(),
            platformSettings = platformSettings,
        )

        // Add file to the state
        if (file != null) {
            val newFiles = _uiState.value.files + file
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun pickImages() = executeWithLoading {
        // Pick files
        val files = FileKit.pickFile(
            type = PickerType.Image,
            mode = PickerMode.Multiple(),
            platformSettings = platformSettings,
        )

        // Add files to the state
        if (files != null) {
            // Add files to the state
            val newFiles = _uiState.value.files + files
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun pickFile() = executeWithLoading {
        // Pick a file
        val file = FileKit.pickFile(
            type = PickerType.File(extensions = listOf("png")),
            platformSettings = platformSettings,
        )

        // Add file to the state
        if (file != null) {
            val newFiles = _uiState.value.files + file
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun pickFiles() = executeWithLoading {
        // Pick files
        val files = FileKit.pickFile(
            type = PickerType.File(extensions = listOf("png")),
            mode = PickerMode.Multiple(),
            platformSettings = platformSettings,
        )

        // Add files to the state
        if (files != null) {
            val newFiles = _uiState.value.files + files
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    fun pickDirectory() = executeWithLoading {
        // Pick a directory
        val directory = pickDirectoryIfSupported(platformSettings)

        // Update the state
        if (directory != null) {
            _uiState.update { it.copy(directory = directory) }
        }
    }

    fun saveFile(file: PlatformFile) = executeWithLoading {
        // Save a file
        val newFile = FileKit.saveFile(
            bytes = file.readBytes(),
            baseName = file.baseName,
            extension = file.extension,
            platformSettings = platformSettings
        )

        // Add file to the state
        if (newFile != null) {
            val newFiles = _uiState.value.files + newFile
            _uiState.update { it.copy(files = newFiles) }
        }
    }

    private fun executeWithLoading(block: suspend () -> Unit) {
        viewModelScope.coroutineScope.launch {
            _uiState.update { it.copy(loading = true) }
            block()
            _uiState.update { it.copy(loading = false) }
        }
    }
}

data class MainUiState(
    val files: Set<PlatformFile> = emptySet(),    // Set instead of List to avoid duplicates
    val directory: PlatformFile? = null,
    val loading: Boolean = false
) {
    // Used by SwiftUI code
    constructor() : this(emptySet(), null, false)
}

expect fun downloadDirectoryPath(): String?

expect suspend fun pickDirectoryIfSupported(
    platformSettings: FileKitDialogSettings?
): PlatformFile?
