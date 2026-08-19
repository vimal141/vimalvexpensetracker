package cashvexa.expensetracker.budget.money.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cashvexa.expensetracker.budget.money.databinding.BottomSheetClearDataBinding

class ClearDataBottomSheet(
    private val onClearConfirmed: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetClearDataBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetClearDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnClearAll.setOnClickListener {
            onClearConfirmed()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { d ->
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Design_BottomSheetDialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
