package edu.nd.pmcburne.hello

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import java.util.Dictionary

class MapViewModel : ViewModel() {

    private val _tags = MutableStateFlow<List<String>>(emptyList<String>())
    val tags: StateFlow<List<String>> = _tags

    private val _landmarks = MutableStateFlow<List<Landmark>>(emptyList())
    val landmarks: StateFlow<List<Landmark>> = _landmarks

    private val _selectedTag = MutableStateFlow<String>("core")
    val selectedTag: StateFlow<String> = _selectedTag

    fun loadData() {
        viewModelScope.launch {
            val res = fetchLandmarks() // suspend function

            // update landmarks list
            _landmarks.value = res

            // flatten all tags from all landmarks into a single map: tag -> true
            val tagsMap = res
                .flatMap { it.tag_list }   // get all tags
                .distinct()                // remove duplicates

            _tags.value = tagsMap
        }
    }

    fun setTag(tag: String) {
        _selectedTag.value = tag
    }
}