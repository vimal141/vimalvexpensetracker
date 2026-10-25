package cashvexa.expensetracker.budget.money.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import cashvexa.expensetracker.budget.money.utility.helper.EdgeToEdgeUtil

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var binding: VB
    abstract fun inflateBinding(inflater: LayoutInflater): VB
    open val isEdgeToEdgeEnabled: Boolean = true
    open val isAutoApplyInsetsPadding: Boolean = true
    open val isApplyKeyboardPadding: Boolean = false
    abstract fun onCreateActivity(savedInstanceState: Bundle?)
    abstract fun onBackPressedAction()
    open fun getInsetView(): View = binding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)

        if (isEdgeToEdgeEnabled) {
            EdgeToEdgeUtil.setupEdgeToEdge(this)
            if (isAutoApplyInsetsPadding) {
                setupEdgeToEdgePadding(getInsetView())
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressedAction()
            }
        })

        onCreateActivity(savedInstanceState)
    }

    protected open fun setupEdgeToEdgePadding(view: View) {
        EdgeToEdgeUtil.applySystemBarInsets(
            view = view,
            applyTop = true,
            applyBottom = true,
            applyLeft = true,
            applyRight = true,
            includeIme = isApplyKeyboardPadding
        )
    }
}