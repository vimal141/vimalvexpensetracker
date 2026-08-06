package com.tb.example.expensebook.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.tb.example.expensebook.R
import com.tb.example.expensebook.data.model.Category
import com.tb.example.expensebook.databinding.ActivityManageCategoriesBinding
import com.tb.example.expensebook.databinding.DialogAddCategoryBinding
import com.tb.example.expensebook.ui.adapter.ManageCategoryAdapter
import com.tb.example.expensebook.ui.viewmodel.TransactionViewModel
import com.tb.example.expensebook.utility.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageCategoriesActivity : BaseActivity<ActivityManageCategoriesBinding>() {
    private val viewModel: TransactionViewModel by viewModel()
    private lateinit var adapter: ManageCategoryAdapter
    private var currentType = Constants.TYPE_EXPENSE

    override fun inflateBinding(inflater: LayoutInflater): ActivityManageCategoriesBinding {
        return ActivityManageCategoriesBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupListeners()
        observeCategories()
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setupRecyclerView() {
        adapter = ManageCategoryAdapter(
            onEditClick = { showAddEditDialog(it) },
            onDeleteClick = { category ->
                if (category.isDefault) {
                    Toast.makeText(this, "Cannot delete default categories", Toast.LENGTH_SHORT).show()
                } else {
                    showDeleteConfirmation(category)
                }
            }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentType = if (tab?.position == 0) Constants.TYPE_EXPENSE else Constants.TYPE_INCOME
                viewModel.setCategoryType(currentType)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.fabAddCategory.setOnClickListener { showAddEditDialog() }
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            viewModel.categories.collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    private fun showAddEditDialog(category: Category? = null) {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
        
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        if (category != null) {
            dialogBinding.txtDialogTitle.text = "Edit Category"
            dialogBinding.etCategoryName.setText(category.name)
            if (category.type == Constants.TYPE_INCOME) {
                dialogBinding.rbIncome.isChecked = true
            } else {
                dialogBinding.rbExpense.isChecked = true
            }
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        
        dialogBinding.btnSaveCategory.setOnClickListener {
            val name = dialogBinding.etCategoryName.text.toString().trim()
            if (name.isEmpty()) {
                dialogBinding.inputName.error = "Enter name"
                return@setOnClickListener
            }

            val type = if (dialogBinding.rbExpense.isChecked) Constants.TYPE_EXPENSE else Constants.TYPE_INCOME
            
            val newCategory = Category(
                id = category?.id ?: 0,
                name = name,
                iconName = category?.iconName ?: "ic_others",
                type = type,
                isDefault = false
            )

            if (category == null) viewModel.addCategory(newCategory) else viewModel.updateCategory(newCategory)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCategory(category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
