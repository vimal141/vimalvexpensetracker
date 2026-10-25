package cashvexa.expensetracker.budget.money.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cashvexa.expensetracker.budget.money.data.model.Transaction
import cashvexa.expensetracker.budget.money.ui.activity.AddTransactionActivity
import cashvexa.expensetracker.budget.money.ui.activity.SettingsActivity
import cashvexa.expensetracker.budget.money.ui.adapter.TransactionAdapter
import cashvexa.expensetracker.budget.money.ui.viewmodel.TransactionViewModel
import cashvexa.expensetracker.budget.money.utility.Constants
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import cashvexa.expensetracker.budget.money.R
import cashvexa.expensetracker.budget.money.databinding.DialogBottomSheetDeleteBinding
import cashvexa.expensetracker.budget.money.databinding.FragmentHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

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
        setupListeners()
        observeTransactions()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupListeners() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter { transaction ->
            val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply {
                putExtra(Constants.EXTRA_TRANSACTION, transaction)
            }
            startActivity(intent)
        }

        adapter.onSelectionChanged = { isSelectionMode, count ->
            if (isSelectionMode) {
                binding.layoutCab.visibility = View.VISIBLE
                binding.fragmentTittle.visibility = View.INVISIBLE
                binding.btnSettings.visibility = View.INVISIBLE
                binding.txtCabCount.text = getString(R.string.selected_count, count)

                if (count == adapter.itemCount && adapter.itemCount > 0) {
                    binding.btnCabSelectAll.setImageResource(R.drawable.ic_selected)
                } else {
                    binding.btnCabSelectAll.setImageResource(R.drawable.ic_unselected)
                }
            } else {
                binding.layoutCab.visibility = View.GONE
                binding.fragmentTittle.visibility = View.VISIBLE
                binding.btnSettings.visibility = View.VISIBLE
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
                showDeleteBottomSheet(selected)
            }
        }

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun showDeleteBottomSheet(selected: List<Transaction>) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogBottomSheetDeleteBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        cashvexa.expensetracker.budget.money.utility.helper.EdgeToEdgeUtil.applyBottomSheetInsets(dialogBinding.root)

        (dialogBinding.root.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)

        val count = selected.size
        if (count == 1) {
            dialogBinding.txtDeleteTitle.text = getString(R.string.delete_transaction_title)
            dialogBinding.txtDeleteMessage.text = getString(R.string.delete_single_transaction_desc)
        } else {
            dialogBinding.txtDeleteTitle.text = getString(R.string.delete_transactions_title, count)
            dialogBinding.txtDeleteMessage.text = getString(R.string.delete_transactions_desc, count)
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirmDelete.setOnClickListener {
            selected.forEach { viewModel.delete(it) }
            adapter.clearSelection()
            dialog.dismiss()
            Snackbar.make(
                binding.root,
                getString(R.string.toast_transactions_deleted, count),
                Snackbar.LENGTH_SHORT
            ).show()
        }

        dialog.show()
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
                        if (list.isEmpty()) {
                            binding.rvHistory.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.VISIBLE
                        } else {
                            binding.layoutEmpty.visibility = View.GONE
                            binding.rvHistory.visibility = View.VISIBLE
                            adapter.updateData(list, viewModel.searchQuery.value)
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        _binding?.let { b ->
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(b.etSearch.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
