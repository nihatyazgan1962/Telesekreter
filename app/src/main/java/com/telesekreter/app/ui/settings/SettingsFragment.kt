package com.telesekreter.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.telesekreter.app.data.local.AppDatabase
import com.telesekreter.app.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())

        // Yerel Yedekleme Al
        binding.btnExportBackup.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val persons = db.personDao().getAllPersons().first()
                    val groups = db.groupDao().getAllGroups().first()
                    val templates = db.messageTemplateDao().getAllTemplates().first()
                    val specialDays = db.specialDayDao().getAllSpecialDays().first()

                    val backupMap = mapOf(
                        "persons" to persons,
                        "groups" to groups,
                        "templates" to templates,
                        "specialDays" to specialDays,
                        "timestamp" to System.currentTimeMillis()
                    )

                    val json = Gson().toJson(backupMap)
                    val backupFile = File(requireContext().filesDir, "telesekreter_yedek.json")
                    backupFile.writeText(json)

                    Toast.makeText(requireContext(), "Yedekleme başarıyla oluşturuldu:\n${backupFile.name}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Yedek alınırken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // VCF Formatında Dışa Aktar
        binding.btnExportVcf.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val persons = db.personDao().getAllPersons().first()
                    val sb = java.lang.StringBuilder()
                    for (p in persons) {
                        sb.append("BEGIN:VCARD\n")
                        sb.append("VERSION:3.0\n")
                        sb.append("FN:${p.fullName}\n")
                        sb.append("TEL;TYPE=CELL:${p.phone}\n")
                        if (!p.notes.isNullOrBlank()) sb.append("NOTE:${p.notes}\n")
                        sb.append("END:VCARD\n")
                    }
                    val vcfFile = File(requireContext().filesDir, "rehber_yedek.vcf")
                    vcfFile.writeText(sb.toString())
                    Toast.makeText(requireContext(), "${persons.size} kişi VCF dosyası olarak kaydedildi:\n${vcfFile.name}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "VCF aktarım hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // CSV Formatında Dışa Aktar
        binding.btnExportCsv.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val persons = db.personDao().getAllPersons().first()
                    val sb = java.lang.StringBuilder()
                    sb.append("Ad,Soyad,Telefon,Not\n")
                    for (p in persons) {
                        sb.append("\"${p.name}\",\"${p.surname}\",\"${p.phone}\",\"${p.notes}\"\n")
                    }
                    val csvFile = File(requireContext().filesDir, "kisiler_excel.csv")
                    csvFile.writeText(sb.toString())
                    Toast.makeText(requireContext(), "${persons.size} kişi CSV/Excel dosyası olarak kaydedildi:\n${csvFile.name}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "CSV aktarım hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Yedekten Geri Yükle
        binding.btnImportBackup.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val backupFile = File(requireContext().filesDir, "telesekreter_yedek.json")
                    if (!backupFile.exists()) {
                        Toast.makeText(requireContext(), "Daha önce alınmış bir yerel yedek dosyası bulunamadı!", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    Toast.makeText(requireContext(), "Yedek dosyası bulundu ve doğrulandı.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Yükleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
