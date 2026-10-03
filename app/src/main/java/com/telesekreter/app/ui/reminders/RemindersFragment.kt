package com.telesekreter.app.ui.reminders

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.telesekreter.app.data.local.entity.ReminderEntity
import com.telesekreter.app.databinding.FragmentRemindersBinding
import com.telesekreter.app.databinding.ItemReminderBinding
import com.telesekreter.app.scheduler.TaskAlarmScheduler
import com.telesekreter.app.util.DateTimeUtils
import kotlinx.coroutines.launch
import java.util.*

class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private val reminderList = mutableListOf<ReminderEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupListeners()
        observeReminders()
    }

    private fun setupRecyclerView() {
        binding.rvReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReminders.adapter = ReminderAdapter(
            reminderList,
            onDeleteClick = { reminder ->
                lifecycleScope.launch {
                    TaskAlarmScheduler.cancelTask(requireContext(), reminder.id, TaskAlarmScheduler.TYPE_REMINDER)
                    db.reminderDao().deleteReminder(reminder)
                    Toast.makeText(requireContext(), "Hatırlatıcı silindi.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupListeners() {
        binding.btnAddReminder.setOnClickListener {
            showAddReminderDialog()
        }

        // Hızlı Hatırlatıcılar (15 dk, Bu Akşam, Yarın Sabah)
        binding.btnQuick15Min.setOnClickListener {
            val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, 15) }
            saveQuickReminder("Hızlı Hatırlatma", "15 dakika sonraki hatırlatmanız", cal.timeInMillis)
        }

        binding.btnQuickTonight.setOnClickListener {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            saveQuickReminder("Akşam Hatırlatması", "Bu akşam yapılacaklar", cal.timeInMillis)
        }

        binding.btnQuickTomorrow.setOnClickListener {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            saveQuickReminder("Sabah Hatırlatması", "Yarın sabahın işleri", cal.timeInMillis)
        }
    }

    private fun saveQuickReminder(title: String, desc: String, timeMillis: Long) {
        lifecycleScope.launch {
            val reminder = ReminderEntity(
                title = title,
                description = desc,
                scheduledTimeMillis = timeMillis,
                status = "PLANLANDI"
            )
            val id = db.reminderDao().insertReminder(reminder)
            TaskAlarmScheduler.scheduleTask(requireContext(), id, TaskAlarmScheduler.TYPE_REMINDER, timeMillis)
            Toast.makeText(requireContext(), "${DateTimeUtils.formatTime(timeMillis)} için hatılatıcı kuruldu.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_reminder, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etReminderTitle)
        val etDesc = dialogView.findViewById<EditText>(R.id.etReminderDesc)
        val selectedCal = Calendar.getInstance()

        AlertDialog.Builder(requireContext())
            .setTitle("Yeni Hatırlatıcı Ekle")
            .setView(dialogView)
            .setPositiveButton("Planla") { _, _ ->
                val title = etTitle.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                if (title.isBlank()) {
                    Toast.makeText(requireContext(), "Lütfen başlık giriniz!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val entity = ReminderEntity(
                        title = title,
                        description = desc,
                        scheduledTimeMillis = selectedCal.timeInMillis,
                        status = "PLANLANDI"
                    )
                    val insertedId = db.reminderDao().insertReminder(entity)
                    TaskAlarmScheduler.scheduleTask(
                        requireContext(),
                        insertedId,
                        TaskAlarmScheduler.TYPE_REMINDER,
                        selectedCal.timeInMillis
                    )
                    Toast.makeText(requireContext(), "Hatırlatıcı oluşturuldu.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun observeReminders() {
        lifecycleScope.launch {
            db.reminderDao().getAllReminders().collect { list ->
                reminderList.clear()
                reminderList.addAll(list)
                binding.rvReminders.adapter?.notifyDataSetChanged()
                binding.tvEmptyReminders.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ReminderAdapter(
        private val items: List<ReminderEntity>,
        private val onDeleteClick: (ReminderEntity) -> Unit
    ) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvTitle.text = item.title
            holder.binding.tvDesc.text = item.description
            holder.binding.tvDesc.visibility = if (item.description.isBlank()) View.GONE else View.VISIBLE
            holder.binding.tvTime.text = DateTimeUtils.formatDateTime(item.scheduledTimeMillis)
            holder.binding.tvStatus.text = item.status
            holder.binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}
