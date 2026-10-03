package com.telesekreter.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.telesekreter.app.data.local.AppDatabase
import com.telesekreter.app.databinding.FragmentHomeBinding
import com.telesekreter.app.ui.MainActivity
import com.telesekreter.app.ui.birthdays.BirthdaysFragment
import com.telesekreter.app.ui.calltasks.CallTasksFragment
import com.telesekreter.app.ui.contacts.ContactsFragment
import com.telesekreter.app.ui.history.HistoryFragment
import com.telesekreter.app.ui.messages.MessagesFragment
import com.telesekreter.app.ui.reminders.RemindersFragment
import com.telesekreter.app.ui.settings.SettingsFragment
import com.telesekreter.app.ui.templates.TemplatesFragment
import com.telesekreter.app.util.DateTimeUtils
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTodayDate.text = "Bugün: " + DateTimeUtils.formatDate(System.currentTimeMillis())

        setupButtonListeners()
        observeDashboardStats()
    }

    private val voiceLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                handleVoiceCommand(spokenText)
            }
        }
    }

    private fun handleVoiceCommand(text: String) {
        val lower = text.lowercase(java.util.Locale("tr"))
        val mainActivity = activity as? MainActivity

        if (lower.contains("mesaj") || lower.contains("yaz")) {
            mainActivity?.loadFragment(MessagesFragment.newInstance(isSchedule = true))
            android.widget.Toast.makeText(requireContext(), "Algılanan: \"$text\"\nMesaj ekranı açıldı.", android.widget.Toast.LENGTH_LONG).show()
        } else if (lower.contains("ara") || lower.contains("telefon")) {
            mainActivity?.loadFragment(CallTasksFragment())
            android.widget.Toast.makeText(requireContext(), "Algılanan: \"$text\"\nArama planlama açıldı.", android.widget.Toast.LENGTH_LONG).show()
        } else if (lower.contains("hatırlat") || lower.contains("hatırla")) {
            mainActivity?.loadFragment(RemindersFragment())
            android.widget.Toast.makeText(requireContext(), "Algılanan: \"$text\"\nHatırlatıcı açıldı.", android.widget.Toast.LENGTH_LONG).show()
        } else if (lower.contains("doğum günü")) {
            mainActivity?.loadFragment(BirthdaysFragment())
            android.widget.Toast.makeText(requireContext(), "Algılanan: \"$text\"\nDoğum günleri açıldı.", android.widget.Toast.LENGTH_LONG).show()
        } else {
            mainActivity?.loadFragment(MessagesFragment.newInstance(isQuickSend = true))
            android.widget.Toast.makeText(requireContext(), "Algılanan: \"$text\"", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun setupButtonListeners() {
        val mainActivity = activity as? MainActivity

        binding.btnVoiceAssistant.setOnClickListener {
            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Komutunuzu söyleyin (Örn: Yarın Ahmet'e günaydın mesajı gönder)")
            }
            try {
                voiceLauncher.launch(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(requireContext(), "Cihazınızda ses tanıma servisi bulunamadı.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSendMessage.setOnClickListener {
            mainActivity?.loadFragment(MessagesFragment.newInstance(isQuickSend = true))
        }

        binding.btnWhatsAppMessage.setOnClickListener {
            mainActivity?.loadFragment(MessagesFragment.newInstance(isQuickSend = true, isWhatsApp = true))
        }

        binding.btnScheduleMessage.setOnClickListener {
            mainActivity?.loadFragment(MessagesFragment.newInstance(isSchedule = true))
        }

        binding.btnBulkMessage.setOnClickListener {
            mainActivity?.loadFragment(MessagesFragment.newInstance(isBulk = true))
        }

        binding.btnContacts.setOnClickListener {
            mainActivity?.loadFragment(ContactsFragment())
        }

        binding.btnBirthdays.setOnClickListener {
            mainActivity?.loadFragment(BirthdaysFragment())
        }

        binding.btnReminders.setOnClickListener {
            mainActivity?.loadFragment(RemindersFragment())
        }

        binding.btnCallSchedule.setOnClickListener {
            mainActivity?.loadFragment(CallTasksFragment())
        }

        binding.btnTemplates.setOnClickListener {
            mainActivity?.loadFragment(TemplatesFragment())
        }

        binding.btnHistory.setOnClickListener {
            mainActivity?.loadFragment(HistoryFragment())
        }

        binding.btnBackup.setOnClickListener {
            mainActivity?.loadFragment(SettingsFragment())
        }
    }

    private fun observeDashboardStats() {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            combine(
                db.scheduledMessageDao().getPendingMessages(),
                db.callTaskDao().getPendingCallTasks(),
                db.reminderDao().getActiveReminders()
            ) { messages, calls, reminders ->
                Triple(messages.size, calls.size, reminders.size)
            }.collect { (msgCount, callCount, reminderCount) ->
                binding.tvSummaryTasks.text = "• Bekleyen Mesaj: $msgCount | Planlanan Arama: $callCount | Hatırlatıcı: $reminderCount"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
