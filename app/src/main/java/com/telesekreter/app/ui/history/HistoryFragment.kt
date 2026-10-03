package com.telesekreter.app.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telesekreter.app.data.local.AppDatabase
import com.telesekreter.app.data.local.entity.ScheduledMessageEntity
import com.telesekreter.app.databinding.FragmentHistoryBinding
import com.telesekreter.app.databinding.ItemScheduledMessageBinding
import com.telesekreter.app.util.DateTimeUtils
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private val historyList = mutableListOf<ScheduledMessageEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        observeHistory()
    }

    private fun setupRecyclerView() {
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = HistoryAdapter(historyList)
    }

    private fun observeHistory() {
        lifecycleScope.launch {
            db.scheduledMessageDao().getAllMessages().collect { list ->
                val filtered = list.filter { it.status in listOf("BASARILI", "BASARISIZ", "IPTAL_EDILDI") }
                historyList.clear()
                historyList.addAll(filtered)
                binding.rvHistory.adapter?.notifyDataSetChanged()

                // İstatistikler
                val successCount = filtered.count { it.status == "BASARILI" }
                val failedCount = filtered.count { it.status == "BASARISIZ" }
                val canceledCount = filtered.count { it.status == "IPTAL_EDILDI" }

                binding.tvStats.text = "Başarılı: $successCount  |  Başarısız: $failedCount  |  İptal: $canceledCount"
                binding.tvEmptyHistory.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class HistoryAdapter(
        private val items: List<ScheduledMessageEntity>
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemScheduledMessageBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemScheduledMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvRecipient.text = "Alıcı: ${item.recipientSummary}"
            holder.binding.tvMessageContent.text = item.messageText
            holder.binding.tvTime.text = DateTimeUtils.formatDateTime(item.scheduledTimeMillis)
            holder.binding.tvStatus.text = item.status
            holder.binding.btnCancel.visibility = View.GONE
            holder.binding.btnSendNow.visibility = View.GONE
        }

        override fun getItemCount(): Int = items.size
    }
}
