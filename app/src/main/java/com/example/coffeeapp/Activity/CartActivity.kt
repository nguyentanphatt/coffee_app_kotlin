package com.example.coffeeapp.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.coffeeapp.R
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeapp.Adapter.CartAdapter
import com.example.coffeeapp.Domain.ItemsModel
import com.example.coffeeapp.MainActivity
import com.example.coffeeapp.ViewModel.CartViewModel
import com.example.coffeeapp.databinding.ActivityCartBinding
import com.google.firebase.auth.FirebaseAuth

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var viewModel: CartViewModel
    private lateinit var adapter: CartAdapter
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[CartViewModel::class.java]

        checkAuthAndLoadCart()
        setupListeners()
        observeViewModel()
    }

    private fun checkAuthAndLoadCart() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        userId = currentUser.uid
        viewModel.loadCartItems(userId)
    }

    private fun setupListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.checkoutBtn.setOnClickListener {
            if (viewModel.isCartEmpty()) {
                Toast.makeText(
                    this,
                    "Your cart is empty",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                showCheckoutDialog()
            }
        }

        binding.button.setOnClickListener {
            val discountCode = binding.editTextText2.text.toString()
            if (discountCode.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Discount code: $discountCode",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Please enter discount code",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.cartItems.observe(this) { items ->
            if (items.isEmpty()) {
                showEmptyState()
            } else {
                showCartWithItems(ArrayList(items))
            }
        }

        viewModel.totalPrice.observe(this) { total ->
            updateTotals(total)
        }

        viewModel.operationStatus.observe(this) { status ->
            when (status) {
                is CartViewModel.OperationStatus.Success -> {
                    Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
                }
                is CartViewModel.OperationStatus.Error -> {
                    Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.cartView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE

        binding.apply {
            subtotal.text = "$0.00"
            delivery.text = "$0.00"
            tax.text = "$0.00"
            total.text = "$0.00"
        }

        binding.checkoutBtn.isEnabled = false
        binding.checkoutBtn.alpha = 0.5f
    }

    private fun showCartWithItems(items: ArrayList<ItemsModel>) {
        binding.cartView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE

        binding.checkoutBtn.isEnabled = true
        binding.checkoutBtn.alpha = 1.0f

        if (binding.cartView.adapter == null) {
            binding.cartView.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = CartAdapter(items, userId, viewModel)
            binding.cartView.adapter = adapter
        } else {
            adapter.updateData(items)
        }
    }

    private fun updateTotals(subtotal: Double) {
        val deliveryFee = if (subtotal > 0) 5.0 else 0.0
        val tax = subtotal * 0.1
        val total = subtotal + deliveryFee + tax

        binding.apply {
            this.subtotal.text = "$${String.format("%.2f", subtotal)}"
            delivery.text = "$${String.format("%.2f", deliveryFee)}"
            this.tax.text = "$${String.format("%.2f", tax)}"
            this.total.text = "$${String.format("%.2f", total)}"
        }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            viewModel.loadCartItems(userId)
        }
    }

    private fun showCheckoutDialog() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_checkout, null)
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)

        val processingLayout = dialogView.findViewById<LinearLayout>(R.id.processingLayout)
        val successLayout = dialogView.findViewById<LinearLayout>(R.id.successLayout)
        val orderIdText = dialogView.findViewById<TextView>(R.id.orderIdText)
        val backToHomeBtn = dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.backToHomeBtn)

        processingLayout.visibility = View.VISIBLE
        successLayout.visibility = View.GONE

        dialog.show()

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            viewModel.clearCart(userId)

            val orderId = "#${System.currentTimeMillis().toString().takeLast(6)}"
            orderIdText.text = "Order ID: $orderId"

            processingLayout.visibility = View.GONE
            successLayout.visibility = View.VISIBLE

            backToHomeBtn.setOnClickListener {
                dialog.dismiss()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }, 3000)
    }
}