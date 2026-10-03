package com.telesekreter.app.ui.birthdays

import android.app.AlertDialog
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
import com.telesekreter.app.data.local.entity.SpecialDayEntity
import com.telesekreter.app.databinding.FragmentBirthdaysBinding
import com.telesekreter.app.databinding.ItemSpecialDayBinding
import com.telesekreter.app.ui.MainActivity
import com.telesekreter.app.ui.messages.MessagesFragment
import kotlinx.coroutines.launch

class BirthdaysFragment : Fragment() {

    private var _binding: FragmentBirthdaysBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private val specialDaysList = mutableListOf<SpecialDayEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBirthdaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupListeners()
        observeSpecialDays()
    }

    private fun setupRecyclerView() {
        binding.rvSpecialDays.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSpecialDays.adapter = SpecialDayAdapter(
            specialDaysList,
            onSendCelebrationClick = { item ->
                // Doğum günü veya tebrik mesajı gönderme ekranına yönlendir
                val mainActivity = activity as? MainActivity
                mainActivity?.loadFragment(MessagesFragment.newInstance(isQuickSend = true))
            },
            onDeleteClick = { item ->
                lifecycleScope.launch {
                    db.specialDayDao().deleteSpecialDay(item)
                    Toast.makeText(requireContext(), "${item.title} silindi.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupListeners() {
        binding.btnAddSpecialDay.setOnClickListener {
            showAddSpecialDayDialog()
        }
    }

    private fun showAddSpecialDayDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_special_day, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etSpecialDayTitle)
        val etDay = dialogView.findViewById<EditText>(R.id.etSpecialDayDay)
        val etMonth = dialogView.findViewById<EditText>(R.id.etSpecialDayMonth)

        AlertDialog.Builder(requireContext())
            .setTitle("Yeni Özel Gün / Doğum Günü")
            .setView(dialogView)
            .setPositiveButton("Kaydet") { _, _ ->
                val title = etTitle.text.toString().trim()
                val day = etDay.text.toString().toIntOrNull() ?: 1
                val month = etMonth.text.toString().toIntOrNull() ?: 1

                if (title.isBlank()) {
                    Toast.makeText(requireContext(), "Lütfen başlık giriniz!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val entity = SpecialDayEntity(
                        title = title,
                        dayOfMonth = day,
                        month = month,
                        type = "OZEL"
                    )
                    db.specialDayDao().insertSpecialDay(entity)
                    Toast.makeText(requireContext(), "$title kaydedildi.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun observeSpecialDays() {
        lifecycleScope.launch {
            db.specialDayDao().getAllSpecialDays().collect { list ->
                specialDaysList.clear()
                specialDaysList.addAll(list)
                binding.rvSpecialDays.adapter?.notifyDataSetChanged()
                binding.tvEmptySpecialDays.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SpecialDayAdapter(
        private val items: List<SpecialDayEntity>,
        private val onSendCelebrationClick: (SpecialDayEntity) -> Unit,
        private val onDeleteClick: (SpecialDayEntity) -> Unit
    ) : RecyclerView.Adapter<SpecialDayAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemSpecialDayBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSpecialDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvTitle.text = item.title
            holder.binding.tvDate.text = "${item.dayOfMonth} / ${item.month}"
            holder.binding.btnSendCelebration.setOnClickListener { onSendCelebrationClick(item) }
            holder.binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}
