package com.example.budgetapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.Global
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.budgetapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Formatter.BigDecimalLayoutForm


class MainActivity : AppCompatActivity() {

    private lateinit var deletedTranscation: Transaction
    private lateinit var transactions: List<Transaction>
    private lateinit var oldtransaction: List<Transaction>
    private lateinit var transcationAdapter: TransactionAdapter
    private lateinit var linearlayoutManager: LinearLayoutManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        transactions = arrayListOf()
        transcationAdapter = TransactionAdapter(transactions)
        linearlayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transactions"
        ).build()

        binding.recyclerview.apply {
            adapter = transcationAdapter
            layoutManager = linearlayoutManager
        }




        fetchAll()


        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                deleteTranscation(transactions[viewHolder.adapterPosition])
            }


        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(binding.recyclerview)


        binding.addBtn.setOnClickListener {
            val intent = Intent(this, Add_transaction::class.java)
            startActivity(intent)
        }

    }


    private fun fetchAll() {

        GlobalScope.launch {


            transactions = db.transactionDao().getAll()

            runOnUiThread {
                updateDashboard()
                transcationAdapter.setData(transactions)

            }

        }


    }


    private fun updateDashboard() {

        val totalAmount = transactions.map { it.amount }.sum();

        val budgetAmount = transactions.filter { it.amount > 0 }.map { it.amount }.sum()
        val expenseAmount = totalAmount - budgetAmount


        binding.total.text = "$ %.2f".format(totalAmount)
        binding.budget.text = "$ %.2f".format(budgetAmount)
        binding.expense.text = "$ %.2f".format(expenseAmount)

    }

    private fun undoDelete() {
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTranscation)

            transactions = oldtransaction

            runOnUiThread {
                transcationAdapter.setData(transactions)
                updateDashboard()
            }
        }
    }

    private fun showSnackbar() {
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "Transaction deleted!", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo") {
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }


    private fun deleteTranscation(transaction: Transaction) {

        deletedTranscation = transaction

        oldtransaction = transactions


        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            transactions = transactions.filter { it.id != transaction.id }
            runOnUiThread {
                updateDashboard()
                transcationAdapter.setData(transactions)
                showSnackbar()
            }
        }





    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}