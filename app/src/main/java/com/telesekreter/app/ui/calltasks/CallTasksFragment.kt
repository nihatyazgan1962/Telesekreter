package com.telesekreter.app.ui.calltasks

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telesekreter.app.R
import com.telesekreter.app.data.local.AppDatabase
import com.telesekreter.app.data.local.entity.CallTaskEntity
import com.telesekreter.app.data.local.entity.PersonEntity
import com.telesekreter.app.databinding.FragmentCallTasksBinding
import com.telesekreter.app.databinding.ItemCallTaskBinding
import com.telesekreter.app.scheduler.TaskAlarmScheduler
import com.telesekreter.app.util.DateTimeUtils
import kotlinx.coroutines.launch
import java.util.*

class CallTasksFragment : Fragment() {

    private var _binding: FragmentCallTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private val callTasksList = mutableListOf<CallTaskEntity>()
    private val allPersons = mutableListOf<PersonEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupListeners()
        loadPersons()
        observeCallTasks()
    }

    private fun setupRecyclerView() {
        binding.rvCallTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCallTasks.adapter = CallTaskAdapter(
            callTasksList,
            onCallClick = { task ->
                // Güvenli Arama Intent'i (Android ACTION_DIAL)
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${task.phone.replace("[^0-9+]".toRegex(), "")}")
                }
                startActivity(intent)
            },
            onDeleteClick = { task ->
                lifecycleScope.launch {
                    TaskAlarmScheduler.cancelTask(requireContext(), task.id, TaskAlarmScheduler.TYPE_CALL)
                    db.callTaskDao().deleteCallTask(task)
                    Toast.makeText(requireContext(), "Arama görevi silindi.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupListeners() {
        binding.btnAddCallTask.setOnClickListener {
            showAddCallTaskDialog()
        }
    }

    private fun loadPersons() {
        lifecycleScope.launch {
            db.personDao().getAllPersons().collect { persons ->
                allPersons.clear()
                allPersons.addAll(persons)
            }
        }
    }

    private fun showAddCallTaskDialog() {
        if (allPersons.isEmpty()) {
            Toast.makeText(requireContext(), "Önce Kişiler sekmesinden kişi ekleyiniz!", Toast.LENGTH_SHORT).show()
            return
        }

        val names = allPersons.map { it.fullName }.toTypedArray()
        var selectedPersonIndex = 0

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_call_task, null)
        val etNote = dialogView.findViewById<EditText>(R.id.etCallNote)
        val btnSelectDate = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCallDate)
        val btnSelectTime = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCallTime)

        val selectedCal = Calendar.getInstance().apply { add(Calendar.MINUTE, 30) }
        btnSelectDate.text = DateTimeUtils.formatDate(selectedCal.timeInMillis)
        btnSelectTime.text = DateTimeUtils.formatTime(selectedCal.timeInMillis)

        btnSelectDate.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedCal.set(Calendar.YEAR, y)
                selectedCal.set(Calendar.MONTH, m)
                selectedCal.set(Calendar.DAY_OF_MONTH, d)
                btnSelectDate.text = DateTimeUtils.formatDate(selectedCal.timeInMillis)
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnSelectTime.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, min ->
                selectedCal.set(Calendar.HOUR_OF_DAY, h)
                selectedCal.set(Calendar.MINUTE, min)
                btnSelectTime.text = DateTimeUtils.formatTime(selectedCal.timeInMillis)
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Aranacak Kişiyi Seçin")
            .setSingleChoiceItems(names, 0) { _, which ->
                selectedPersonIndex = which
            }
            .setView(dialogView)
            .setPositiveButton("Arama Planla") { _, _ ->
                val person = allPersons[selectedPersonIndex]
                val note = etNote.text.toString().trim()

                lifecycleScope.launch {
                    val entity = CallTaskEntity(
                        personId = person.id,
                        personName = person.fullName,
                        phone = person.phone,
                        note = note,
                        scheduledTimeMillis = selectedCal.timeInMillis,
                        status = "PLANLANDI"
                    )
                    val insertedId = db.callTaskDao().insertCallTask(entity)
                    TaskAlarmScheduler.scheduleTask(
                        requireContext(),
                        insertedId,
                        TaskAlarmScheduler.TYPE_CALL,
                        selectedCal.timeInMillis
                    )
                    Toast.makeText(requireContext(), "${person.fullName} için arama planlandı.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun observeCallTasks() {
        lifecycleScope.launch {
            db.callTaskDao().getAllCallTasks().collect { list ->
                callTasksList.clear()
                callTasksList.addAll(list)
                binding.rvCallTasks.adapter?.notifyDataSetChanged()
                binding.tvEmptyCallTasks.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class CallTaskAdapter(
        private val items: List<CallTaskEntity>,
        private val onCallClick: (CallTaskEntity) -> Unit,
        private val onDeleteClick: (CallTaskEntity) -> Unit
    ) : RecyclerView.Adapter<CallTaskAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemCallTaskBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCallTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvPersonName.text = item.personName
            holder.binding.tvPhone.text = item.phone
            holder.binding.tvTime.text = DateTimeUtils.formatDateTime(item.scheduledTimeMillis)
            holder.binding.tvNote.text = item.note
            holder.binding.tvNote.visibility = if (item.note.isBlank()) View.GONE else View.VISIBLE

            holder.binding.btnCallNow.setOnClickListener { onCallClick(item) }
            holder.binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}
