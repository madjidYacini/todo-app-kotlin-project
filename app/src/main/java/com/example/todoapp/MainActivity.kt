package com.example.todoapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.network.Task
import com.example.todoapp.network.TaskListAdapter
import com.example.todoapp.network.TodoApiCall
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton

import java.util.*

class MainActivity : AppCompatActivity() {
    private var reverse = false
    private val listTask = LinkedList<Task>()

    private fun showAddItemDialog(onFinish: (String?) -> Unit) {
        val editText = EditText(this)
        alert("What do you want to do next?", "Add a new task") {
            customView = editText
            okButton { onFinish(editText.text.toString()) }
            cancelButton { onFinish(null) }
            onCancelled { onFinish(null) }
        }.show()
    }

    private fun deleteTask(position: Int) {


        val builder = AlertDialog.Builder(this@MainActivity)


        builder.setTitle("Delete Confirmation")


        builder.setMessage("Do you want to delete the task ?")

        builder.setPositiveButton("YES") { dialog, which ->

            lifecycleScope.launch {

                val task = listTask[position]
                val res = TodoApiCall.retrofitService.deleteTasks(task.id).await()
                if (res.isSuccessful) {
                    listTask.remove(task)
                    recyclerview.adapter?.notifyItemRemoved(position)

                }
            }

            Toast.makeText(applicationContext, "Ok, task deleted ", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("No") { dialog, which ->
            Toast.makeText(applicationContext, "You are not agree.", Toast.LENGTH_SHORT).show()
        }

        builder.setNeutralButton("Cancel") { _, _ ->
            Toast.makeText(applicationContext, "Activity canceled .", Toast.LENGTH_SHORT).show()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()


    }

    private fun CloseTask(position: Int) {
        lifecycleScope.launch {
            val task = listTask[position]

            val res = TodoApiCall.retrofitService.closeTasks(task.id).await()
            Toast.makeText(this@MainActivity, "La tache a été bien fermée ", Toast.LENGTH_SHORT).show()

        }

    }

    private fun ReopenTask(position: Int) {
        lifecycleScope.launch {
            val task = listTask[position]
            val respose = TodoApiCall.retrofitService.reopenTask(task.id).await()
            Toast.makeText(this@MainActivity, "La tache a été bien ouverte ", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        recyclerview.adapter = TaskListAdapter(listTask, this::deleteTask, this::CloseTask, this::ReopenTask)
        fab.setOnClickListener {
            showAddItemDialog { text ->
                if (text != null) {
                    lifecycleScope.launch {
                        val newTask = TodoApiCall.retrofitService.createTasks(Task("", text)).await()
                        if (newTask != null) {
                            listTask.add(0, newTask)
                            recyclerview.adapter?.notifyItemInserted(0)
                            recyclerview.smoothScrollToPosition(0)
                        }
                        Toast.makeText(this@MainActivity, "La tache a été bien ajoutée", Toast.LENGTH_SHORT).show()

                    }
                }

            }

        }



        recyclerview.layoutManager = LinearLayoutManager(this)

        refreshTasks()

    }

    private fun refreshTasks() {
        lifecycleScope.launch {
            val newTasks = TodoApiCall.retrofitService.getTasks().await()
            listTask.clear()
            listTask.addAll(newTasks)
            recyclerview.adapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_delete -> {
                deleteOption()

                true
            }
            R.id.action_refresh -> {
                refreshTasks()
                Toast.makeText(this@MainActivity, "Refresh 🙂", Toast.LENGTH_SHORT).show()

                true
            }
            R.id.action_sort -> {
                reverse = !reverse
                if (reverse) {
                    listTask.sortByDescending { it.id }
                    Toast.makeText(this@MainActivity, "Sort Z ----> A 🙂", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this@MainActivity, "Sort A ----> Z 🙂", Toast.LENGTH_SHORT).show()

                    listTask.sortBy { it.id }
                }
                recyclerview.adapter?.notifyDataSetChanged()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteOption() {
        if (listTask.isEmpty()) {
            Toast.makeText(this@MainActivity, "there is no task to delete  ", Toast.LENGTH_SHORT).show()

        } else {
            lifecycleScope.launch {
                listTask.forEach {
                    TodoApiCall.retrofitService.deleteTasks(it.id).await()
                }
                listTask.clear()
                recyclerview.adapter?.notifyDataSetChanged()
                Toast.makeText(this@MainActivity, "All tasks are deleted 🗑", Toast.LENGTH_SHORT).show()

            }

        }
    }


}
