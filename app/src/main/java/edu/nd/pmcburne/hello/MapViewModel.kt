package edu.nd.pmcburne.hello

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import java.util.Dictionary

class MapViewModel : ViewModel() {

    private val _tags = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val tags: StateFlow<Map<String, Boolean>> = _tags

    private val _landmarks = MutableStateFlow<List<Landmark>>(emptyList())
    val landmarks: StateFlow<List<Landmark>> = _landmarks

    fun loadData() {
        viewModelScope.launch {
            val res = fetchLandmarks() // suspend function

            // update landmarks list
            _landmarks.value = res

            // flatten all tags from all landmarks into a single map: tag -> true
            val tagsMap = res
                .flatMap { it.tag_list }   // get all tags
                .distinct()                // remove duplicates
                .associateWith { true }    // map each tag to true

            _tags.value = tagsMap
        }
    }

    fun setTag(tag: String, value: Boolean) {
        _tags.value = _tags.value.toMutableMap().apply {
            this[tag] = value
        }
    }
}