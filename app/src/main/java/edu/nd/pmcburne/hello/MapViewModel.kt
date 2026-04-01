package edu.nd.pmcburne.hello

import android.content.Context
import android.util.Log
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
import kotlin.collections.flatMap

class MapViewModel : ViewModel() {

    private val _tags = MutableStateFlow<List<String>>(emptyList<String>())
    val tags: StateFlow<List<String>> = _tags

    private val _landmarks = MutableStateFlow<List<Landmark>>(emptyList())
    val landmarks: StateFlow<List<Landmark>> = _landmarks

    private val _selectedTag = MutableStateFlow<String>("core")
    val selectedTag: StateFlow<String> = _selectedTag

    private fun getTagsFromLandmarks(landmarks: List<Landmark>): List<String> {
        return landmarks
            .flatMap { it.tag_list }   // get all tags
            .distinct()                // remove duplicates
    }


    fun startupLoadData(context: Context) {
        val dao = DatabaseProvider.getDatabase(context).landmarkDao()
        viewModelScope.launch {
            // read from DB originally
            val dbResults = dao.getAll()
            val dbTags = getTagsFromLandmarks(dbResults)
            _landmarks.value = dbResults
            _tags.value = dbTags

            // then call the API
            try {
                val apiResults = fetchLandmarks()
                val apiTags = getTagsFromLandmarks(apiResults)
                _landmarks.value = apiResults
                _tags.value = apiTags

                // then update the DB
                dao.deleteAll()
                dao.insertAll(apiResults)
            }
            catch (e: Exception) {
                Log.w("App", "Failed to fetch from API")
            }

        }
    }

    fun setTag(tag: String) {
        _selectedTag.value = tag
    }
}