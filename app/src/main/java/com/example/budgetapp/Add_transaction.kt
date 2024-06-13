package com.example.budgetapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.example.budgetapp.databinding.ActivityAddTransactionBinding
import com.example.budgetapp.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private lateinit var binding: ActivityMainBinding

class Add_transaction : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        val binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val addTransactionBtn = binding.addTransactionBtn

        binding.labelInput.addTextChangedListener {

            if(it!!.count() > 0)
                binding.labelLayout.error = null

        }

        binding.amountInput.addTextChangedListener {
            if(it!!.count() > 0)
                binding.amountLayout.error = null
        }
        addTransactionBtn.setOnClickListener {
            val label = binding.labelInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val amount = binding.amountInput.text.toString().toDoubleOrNull()

            if(label.isEmpty())
                binding.labelLayout.error = "Please enter a valid label"

            else if(amount == null)
                binding.amountLayout.error = "Please enter a valid amount"
            else{

                val transaction =Transaction(0,label,amount,description)
                insert(transaction)

            }
        }

        binding.closeBtn.setOnClickListener{
             finish()

        }
    }

    private fun insert(transaction: Transaction){
        val db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}