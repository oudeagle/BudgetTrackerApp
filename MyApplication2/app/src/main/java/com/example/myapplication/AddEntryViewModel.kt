package com.example.myapplication

import android.media.Image
import android.net.Uri
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddEntryViewModel : ViewModel() {
    /* The below viewModel is the viewModel which coincides with the addEntry.kt file.
    * This viewmodel allows the input for a name, money spent (allocation), category and any attachments being made
    * The Viewmodel also has access to the sharedViewModel _budgetEntries list. This is where the new entry that is added is
    * appended to. */
    val name = mutableStateOf("")
    val allocation = mutableDoubleStateOf(0.0)
    private val _budgetEntries = mutableStateListOf<BudgetEntry>()
    val budgetEntries: List<BudgetEntry>
        get() = _budgetEntries.toList()
    val category = mutableStateOf("")
    val attachment = mutableStateOf("")

    fun onNameChange(newName: String) {
        name.value = newName
    }

    fun onAllocationChange(newAllocation: Double) {
        allocation.doubleValue = newAllocation
    }

    fun onCategoryChange(newCategory: String){
       category.value = newCategory
    }
}

