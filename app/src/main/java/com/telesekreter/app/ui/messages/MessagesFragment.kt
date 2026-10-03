package com.telesekreter.app.ui.messages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telesekreter.app.R
import com.telesekreter.app.data.local.AppDatabase
import com.telesekreter.app.data.local.entity.GroupEntity
import com.telesekreter.app.data.local.entity.MessageTemplateEntity
import com.telesekreter.app.data.local.entity.PersonEntity
import com.telesekreter.app.data.local.entity.ScheduledMessageEntity
import com.telesekreter.app.databinding.FragmentMessagesBinding
import com.telesekreter.app.databinding.ItemScheduledMessageBinding
import com.telesekreter.app.domain.usecase.VariableParserUseCase
import com.telesekreter.app.scheduler.TaskAlarmScheduler
import com.telesekreter.app.util.DateTimeUtils
import com.telesekreter.app.util.SmsUtils
import kotlinx.coroutines.launch
import java.util.*

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private val scheduledList = mutableListOf<ScheduledMessageEntity>()
    private val allPersons = mutableListOf<PersonEntity>()
    private val allGroups = mutableListOf<GroupEntity>()
    private val allTemplates = mutableListOf<MessageTemplateEntity>()

    private val selectedRecipients = mutableListOf<PersonEntity>()
    private var selectedCalendar = Calendar.getInstance()

    companion object {
        private const val ARG_QUICK_SEND = "arg_quick_send"
        private const val ARG_SCHEDULE = "arg_schedule"
        private const val ARG_BULK = "arg_bulk"
        private const val ARG_WHATSAPP = "arg_whatsapp"

        fun newInstance(isQuickSend: Boolean = false, isSchedule: Boolean = false, isBulk: Boolean = false, isWhatsApp: Boolean = false): MessagesFragment {
            return MessagesFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_QUICK_SEND, isQuickSend)
                    putBoolean(ARG_SCHEDULE, isSchedule)
                    putBoolean(ARG_BULK, isBulk)
                    putBoolean(ARG_WHATSAPP, isWhatsApp)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        val isWhatsApp = arguments?.getBoolean(ARG_WHATSAPP, false) ?: false
        if (isWhatsApp) {
            binding.rbWhatsApp.isChecked = true
        }

        setupViews()
        loadInitialData()
        observeMessages()
    }

    private fun setupViews() {
        binding.rvScheduledMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScheduledMessages.adapter = ScheduledMessageAdapter(
            scheduledList,
            onCancelClick = { msg ->
                lifecycleScope.launch {
                    TaskAlarmScheduler.cancelTask(requireContext(), msg.id, TaskAlarmScheduler.TYPE_MESSAGE)
                    db.scheduledMessageDao().updateStatus(msg.id, "IPTAL_EDILDI", "Kullanıcı tarafından iptal edildi.")
                    Toast.makeText(requireContext(), "Mesaj iptal edildi.", Toast.LENGTH_SHORT).show()
                }
            },
            onSendNowClick = { msg ->
                lifecycleScope.launch {
                    val pIds = msg.recipientPersonIds.split(",").mapNotNull { it.trim().toLongOrNull() }
                    val recipients = db.personDao().getPersonsByIds(pIds)
                    if (msg.channel == "WHATSAPP") {
                        val firstPerson = recipients.firstOrNull()
                        if (firstPerson != null) {
                            val parsed = VariableParserUseCase.parse(msg.messageText, firstPerson)
                            SmsUtils.openWhatsApp(requireContext(), firstPerson.phone, parsed)
                        }
                        db.scheduledMessageDao().updateStatus(msg.id, "BASARILI", "WhatsApp açıldı.")
                    } else {
                        var succ = 0
                        var fail = 0
                        val errList = mutableListOf<String>()
                        for (person in recipients) {
                            val parsed = VariableParserUseCase.parse(msg.messageText, person)
                            val (sent, err) = SmsUtils.sendSmsDirect(requireContext(), person.phone, parsed)
                            if (sent) succ++ else {
                                fail++
                                errList.add("${person.fullName}: $err")
                            }
                        }
                        if (fail == 0 && succ > 0) {
                            db.scheduledMessageDao().updateStatus(msg.id, "BASARILI", "$succ kişiye SMS başarıyla ulaştı.")
                            Toast.makeText(requireContext(), "Mesajlar gönderildi.", Toast.LENGTH_SHORT).show()
                        } else {
                            val log = errList.joinToString("; ")
                            db.scheduledMessageDao().updateStatus(msg.id, "BASARISIZ", log)
                            Toast.makeText(requireContext(), "Mesaj gönderilemedi: $log", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )

        // Tarih ve Saat Butonları
        updateDateTimeDisplay()
        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSelectTime.setOnClickListener { showTimePicker() }

        // Alıcı Seçimi Butonları
        binding.btnSelectRecipients.setOnClickListener { showRecipientSelectionDialog() }
        binding.btnSelectTemplate.setOnClickListener { showTemplateSelectionDialog() }

        // Planla / Gönder Butonu
        binding.btnPlanOrSend.setOnClickListener { handleScheduleOrSend() }
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            db.personDao().getAllPersons().collect { persons ->
                allPersons.clear()
                allPersons.addAll(persons)
            }
        }
        lifecycleScope.launch {
            db.groupDao().getAllGroups().collect { groups ->
                allGroups.clear()
                allGroups.addAll(groups)
            }
        }
        lifecycleScope.launch {
            db.messageTemplateDao().getAllTemplates().collect { templates ->
                allTemplates.clear()
                allTemplates.addAll(templates)
            }
        }
    }

    private val allMessagesFromDb = mutableListOf<ScheduledMessageEntity>()
    private var currentStatusFilter: String? = null // null means ALL

    private fun setupFilterChips() {
        binding.chipGroupMessageFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipFilterPendingApproval -> currentStatusFilter = "ONAY_BEKLIYOR"
                R.id.chipFilterScheduled -> currentStatusFilter = "PLANLANDI"
                R.id.chipFilterSuccess -> currentStatusFilter = "BASARILI"
                else -> currentStatusFilter = null
            }
            applyMessageFilter()
        }
    }

    private fun applyMessageFilter() {
        val filtered = if (currentStatusFilter == null) {
            allMessagesFromDb
        } else {
            allMessagesFromDb.filter { it.status == currentStatusFilter }
        }
        scheduledList.clear()
        scheduledList.addAll(filtered)
        binding.rvScheduledMessages.adapter?.notifyDataSetChanged()
        binding.tvEmptyMessages.visibility = if (scheduledList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun observeMessages() {
        setupFilterChips()
        lifecycleScope.launch {
            db.scheduledMessageDao().getAllMessages().collect { list ->
                allMessagesFromDb.clear()
                allMessagesFromDb.addAll(list)
                applyMessageFilter()
            }
        }
    }

    private fun updateDateTimeDisplay() {
        binding.btnSelectDate.text = DateTimeUtils.formatDate(selectedCalendar.timeInMillis)
        binding.btnSelectTime.text = DateTimeUtils.formatTime(selectedCalendar.timeInMillis)
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateTimeDisplay()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val now = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedCalendar.set(Calendar.MINUTE, minute)
                selectedCalendar.set(Calendar.SECOND, 0)
                updateDateTimeDisplay()
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun showRecipientSelectionDialog() {
        if (allPersons.isEmpty()) {
            Toast.makeText(requireContext(), "Önce Kişiler sekmesinden kişi eklemelisiniz!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_recipients, null)
        val etSearch = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearchRecipient)
        val btnSelectAll = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSelectAll)
        val btnClearAll = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClearAll)
        val rvSelect = dialogView.findViewById<RecyclerView>(R.id.rvSelectRecipients)

        val tempSelectedSet = selectedRecipients.map { it.id }.toMutableSet()
        val displayList = mutableListOf<PersonEntity>()
        displayList.addAll(allPersons)

        class SelectRecipientAdapter : RecyclerView.Adapter<SelectRecipientAdapter.ViewHolder>() {
            inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val cb = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cbRecipient)
                val tvName = view.findViewById<android.widget.TextView>(R.id.tvRecipientName)
                val tvPhone = view.findViewById<android.widget.TextView>(R.id.tvRecipientPhone)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_select_recipient, parent, false)
                return ViewHolder(v)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val p = displayList[position]
                holder.tvName.text = p.fullName
                holder.tvPhone.text = p.phone
                holder.cb.isChecked = tempSelectedSet.contains(p.id)

                val toggleCheck = {
                    if (tempSelectedSet.contains(p.id)) {
                        tempSelectedSet.remove(p.id)
                        holder.cb.isChecked = false
                    } else {
                        tempSelectedSet.add(p.id)
                        holder.cb.isChecked = true
                    }
                }

                holder.itemView.setOnClickListener { toggleCheck() }
                holder.cb.setOnClickListener { toggleCheck() }
            }

            override fun getItemCount(): Int = displayList.size
        }

        val adapter = SelectRecipientAdapter()
        rvSelect.layoutManager = LinearLayoutManager(requireContext())
        rvSelect.adapter = adapter

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString().orEmpty().lowercase(Locale("tr"))
                displayList.clear()
                if (q.isBlank()) {
                    displayList.addAll(allPersons)
                } else {
                    displayList.addAll(allPersons.filter {
                        it.fullName.lowercase(Locale("tr")).contains(q) || it.phone.contains(q)
                    })
                }
                adapter.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        btnSelectAll.setOnClickListener {
            tempSelectedSet.addAll(displayList.map { it.id })
            adapter.notifyDataSetChanged()
        }

        btnClearAll.setOnClickListener {
            tempSelectedSet.clear()
            adapter.notifyDataSetChanged()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Alıcı Seçimi (${allPersons.size} Kişi)")
            .setView(dialogView)
            .setPositiveButton("Tamam (${tempSelectedSet.size})") { _, _ ->
                selectedRecipients.clear()
                selectedRecipients.addAll(allPersons.filter { tempSelectedSet.contains(it.id) })
                updateRecipientsSummary()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun updateRecipientsSummary() {
        if (selectedRecipients.isEmpty()) {
            binding.tvSelectedRecipientsSummary.text = "Alıcı seçilmedi."
        } else if (selectedRecipients.size == 1) {
            binding.tvSelectedRecipientsSummary.text = "Alıcı: ${selectedRecipients[0].fullName}"
        } else {
            binding.tvSelectedRecipientsSummary.text = "Seçilen: ${selectedRecipients.size} kişi (${selectedRecipients.take(2).joinToString { it.name }}...)"
        }
    }

    private fun showTemplateSelectionDialog() {
        if (allTemplates.isEmpty()) {
            Toast.makeText(requireContext(), "Kayıtlı şablon bulunamadı.", Toast.LENGTH_SHORT).show()
            return
        }

        val titles = allTemplates.map { "[${it.category}] ${it.title}" }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Mesaj Şablonu Seç")
            .setItems(titles) { _, which ->
                val selected = allTemplates[which]
                binding.etMessageBody.setText(selected.content)
            }
            .setNegativeButton("Kapat", null)
            .show()
    }

    private fun handleScheduleOrSend() {
        val messageText = binding.etMessageBody.text.toString().trim()
        if (selectedRecipients.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen en az bir alıcı seçiniz!", Toast.LENGTH_SHORT).show()
            return
        }
        if (messageText.isBlank()) {
            Toast.makeText(requireContext(), "Lütfen mesaj metnini yazınız!", Toast.LENGTH_SHORT).show()
            return
        }

        val triggerTime = selectedCalendar.timeInMillis
        val isNow = triggerTime <= System.currentTimeMillis() + 60000 // 1 dakikadan azsa hemen gönder

        // Onay İletişim Kutusu (Güvenlik Kuralı: Alıcıyı, tarihi ve mesajı net göster)
        val summaryText = if (selectedRecipients.size == 1) selectedRecipients[0].fullName else "${selectedRecipients.size} kişi"
        val timeDesc = if (isNow) "Şimdi" else DateTimeUtils.formatDateTime(triggerTime)

        val isWhatsApp = binding.rbWhatsApp.isChecked
        val channelType = if (isWhatsApp) "WHATSAPP" else "SMS"

        AlertDialog.Builder(requireContext())
            .setTitle("Mesaj Gönderim Onayı")
            .setMessage("Kanal: $channelType\nAlıcı: $summaryText\nZaman: $timeDesc\n\nMesaj:\n$messageText\n\nPlanlansın mı?")
            .setPositiveButton("ONAYLA & PLANLA") { _, _ ->
                lifecycleScope.launch {
                    val entity = ScheduledMessageEntity(
                        recipientPersonIds = selectedRecipients.map { it.id }.joinToString(","),
                        recipientSummary = summaryText,
                        messageText = messageText,
                        scheduledTimeMillis = triggerTime,
                        status = "PLANLANDI",
                        channel = channelType,
                        requireConfirmation = binding.switchRequireConfirmation.isChecked
                    )
                    val insertedId = db.scheduledMessageDao().insertMessage(entity)

                    if (!isNow) {
                        TaskAlarmScheduler.scheduleTask(
                            requireContext(),
                            insertedId,
                            TaskAlarmScheduler.TYPE_MESSAGE,
                            triggerTime
                        )
                        Toast.makeText(requireContext(), "Mesaj $timeDesc için planlandı.", Toast.LENGTH_LONG).show()
                    } else {
                        // Hemen gönder
                        if (isWhatsApp) {
                            for (person in selectedRecipients) {
                                val parsed = VariableParserUseCase.parse(messageText, person)
                                SmsUtils.openWhatsApp(requireContext(), person.phone, parsed)
                            }
                        } else {
                            var succ = 0
                            var fail = 0
                            val errList = mutableListOf<String>()
                            for (person in selectedRecipients) {
                                val parsed = VariableParserUseCase.parse(messageText, person)
                                val (sent, err) = SmsUtils.sendSmsDirect(requireContext(), person.phone, parsed)
                                if (sent) succ++ else {
                                    fail++
                                    errList.add("${person.fullName}: $err")
                                }
                            }
                            if (fail == 0 && succ > 0) {
                                db.scheduledMessageDao().updateStatus(insertedId, "BASARILI", "Hemen gönderildi ($succ kişi).")
                                Toast.makeText(requireContext(), "Mesaj gönderildi.", Toast.LENGTH_SHORT).show()
                            } else {
                                val log = errList.joinToString("; ")
                                db.scheduledMessageDao().updateStatus(insertedId, "BASARISIZ", log)
                                Toast.makeText(requireContext(), "Mesaj gönderilemedi: $log", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    // Alanları sıfırla
                    binding.etMessageBody.setText("")
                    selectedRecipients.clear()
                    updateRecipientsSummary()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ScheduledMessageAdapter(
        private val items: List<ScheduledMessageEntity>,
        private val onCancelClick: (ScheduledMessageEntity) -> Unit,
        private val onSendNowClick: (ScheduledMessageEntity) -> Unit
    ) : RecyclerView.Adapter<ScheduledMessageAdapter.ViewHolder>() {

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

            val statusColor = when (item.status) {
                "BASARILI" -> ContextCompat.getColor(holder.itemView.context, R.color.status_success)
                "BASARISIZ" -> ContextCompat.getColor(holder.itemView.context, R.color.status_failed)
                "IPTAL" -> ContextCompat.getColor(holder.itemView.context, R.color.status_canceled)
                else -> ContextCompat.getColor(holder.itemView.context, R.color.status_pending)
            }
            holder.binding.tvStatus.setTextColor(statusColor)

            if (!item.logMessage.isNullOrBlank()) {
                holder.binding.tvLog.text = item.logMessage
                holder.binding.tvLog.visibility = View.VISIBLE
            } else {
                holder.binding.tvLog.visibility = View.GONE
            }

            val isPending = item.status in listOf("PLANLANDI", "BEKLIYOR", "ONAY_BEKLIYOR")
            holder.binding.btnCancel.visibility = if (isPending) View.VISIBLE else View.GONE
            holder.binding.btnSendNow.visibility = if (isPending) View.VISIBLE else View.GONE

            if (item.status == "ONAY_BEKLIYOR") {
                holder.binding.btnSendNow.text = "✅ Onayla & Gönder"
            } else {
                holder.binding.btnSendNow.text = "Şimdi Gönder"
            }

            holder.binding.btnCancel.setOnClickListener { onCancelClick(item) }
            holder.binding.btnSendNow.setOnClickListener { onSendNowClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}
