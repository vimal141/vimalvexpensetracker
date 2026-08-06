package com.tb.example.expensebook.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tb.example.expensebook.R
import com.tb.example.expensebook.ui.adapter.TransactionAdapter
import com.tb.example.expensebook.ui.viewmodel.TransactionViewModel
import com.tb.example.expensebook.databinding.FragmentHistoryBinding
import com.tb.example.expensebook.ui.activity.AddTransactionActivity
import com.tb.example.expensebook.utility.Constants

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by activityViewModel()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFilters()
        observeTransactions()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupRecyclerView() {
        adapter =
            TransactionAdapter { transaction ->
                val intent =
                    Intent(requireContext(), AddTransactionActivity::class.java)
                        .apply {
                            putExtra(Constants.EXTRA_TRANSACTION, transaction)
                        }
                startActivity(intent)
            }
        
        adapter.onSelectionChanged = { isSelectionMode, count ->
            if (isSelectionMode) {
                binding.layoutCab.visibility = View.VISIBLE
                binding.fragmentTittle.visibility = View.INVISIBLE
                binding.txtCabCount.text = getString(R.string.selected_count, count)
                
                if (count == adapter.itemCount && adapter.itemCount > 0) {
                    binding.btnCabSelectAll.setImageResource(R.drawable.ic_selected)
                } else {
                    binding.btnCabSelectAll.setImageResource(R.drawable.ic_unselected)
                }
            } else {
                binding.layoutCab.visibility = View.GONE
                binding.fragmentTittle.visibility = View.VISIBLE
                binding.btnCabSelectAll.setImageResource(R.drawable.ic_unselected)
            }
        }

        binding.btnCabClose.setOnClickListener {
            adapter.clearSelection()
        }

        binding.btnCabSelectAll.setOnClickListener {
            if (adapter.selectedItems.size == adapter.itemCount && adapter.itemCount > 0) {
                adapter.clearSelection()
            } else {
                adapter.selectAll()
            }
        }

        binding.btnCabDelete.setOnClickListener {
            val selected = adapter.selectedItems.toList()
            if (selected.isNotEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_transactions))
                    .setMessage(getString(R.string.delete_transactions_desc, selected.size))
                    .setPositiveButton(getString(R.string.delete)) { _, _ ->
                        selected.forEach { viewModel.delete(it) }
                        adapter.clearSelection()
                        Snackbar.make(
                            binding.root, getString(R.string.toast_transactions_deleted, selected.size), Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
        }

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun setupSearch() {
        binding.btnClearSearch.visibility = if (binding.etSearch.text.isNullOrEmpty()) View.GONE else View.VISIBLE

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                viewModel.setSearchQuery(query)
                binding.btnClearSearch.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
            viewModel.clearFilters()
        }
    }

    private fun setupFilters() {
        val typeChips = mapOf(
            binding.chipAll to Constants.FILTER_ALL,
            binding.chipIncome to Constants.TYPE_INCOME,
            binding.chipExpense to Constants.TYPE_EXPENSE
        )
        typeChips.forEach { (chip, type) ->
            chip.setOnClickListener {
                typeChips.keys.forEach { it.isChecked = false }
                chip.isChecked = true
                viewModel.setFilterType(type)
            }
        }
        binding.chipAll.isChecked = true
    }

    private fun observeTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.filteredTransactions.collectLatest { list ->
                        val safeList = list ?: emptyList()
                        if (safeList.isEmpty()) {
                            binding.rvHistory.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.VISIBLE
                        } else {
                            binding.layoutEmpty.visibility = View.GONE
                            binding.rvHistory.visibility = View.VISIBLE
                            adapter.updateData(safeList, viewModel.searchQuery.value)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
