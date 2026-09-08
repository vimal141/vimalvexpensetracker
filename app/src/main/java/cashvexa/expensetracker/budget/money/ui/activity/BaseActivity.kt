package cashvexa.expensetracker.budget.money.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding

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
            enableEdgeToEdge()
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
        if (isApplyKeyboardPadding){
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }else{
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

}