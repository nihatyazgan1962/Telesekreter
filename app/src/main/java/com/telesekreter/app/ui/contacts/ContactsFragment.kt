package com.telesekreter.app.ui.contacts

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
import com.google.android.material.chip.Chip
import com.telesekreter.app.R
import com.telesekreter.app.data.local.AppDatabase
import com.telesekreter.app.data.local.entity.GroupEntity
import com.telesekreter.app.data.local.entity.PersonEntity
import com.telesekreter.app.data.local.entity.PersonGroupCrossRef
import com.telesekreter.app.databinding.FragmentContactsBinding
import com.telesekreter.app.databinding.ItemPersonBinding
import kotlinx.coroutines.launch

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private val personsList = mutableListOf<PersonEntity>()
    private val groupsList = mutableListOf<GroupEntity>()
    private var selectedGroupId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupListeners()
        setupSearch()
        loadGroups()
        loadPersons()
    }

    private fun setupSearch() {
        binding.etSearchContact.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPersons(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private val allLoadedPersons = mutableListOf<PersonEntity>()

    private fun filterPersons(query: String) {
        val filtered = if (query.isBlank()) {
            allLoadedPersons
        } else {
            val q = query.lowercase(java.util.Locale("tr"))
            allLoadedPersons.filter {
                it.fullName.lowercase(java.util.Locale("tr")).contains(q) ||
                it.phone.contains(q)
            }
        }
        personsList.clear()
        personsList.addAll(filtered)
        binding.rvPersons.adapter?.notifyDataSetChanged()
        binding.tvEmptyState.visibility = if (personsList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        binding.rvPersons.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPersons.adapter = PersonAdapter(
            personsList,
            onWhatsAppClick = { person ->
                com.telesekreter.app.util.SmsUtils.openWhatsApp(requireContext(), person.phone, "Merhaba ${person.name},")
            },
            onCallClick = { person ->
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:${person.phone.replace("[^0-9+]".toRegex(), "")}")
                }
                startActivity(intent)
            },
            onDeleteClick = { person ->
                lifecycleScope.launch {
                    db.personDao().deletePerson(person)
                    Toast.makeText(requireContext(), "${person.fullName} silindi.", Toast.LENGTH_SHORT).show()
                }
            },
            onStarClick = { person ->
                lifecycleScope.launch {
                    val updated = person.copy(isImportant = !person.isImportant)
                    db.personDao().updatePerson(updated)
                }
            }
        )
    }

    private fun setupListeners() {
        binding.btnAddPerson.setOnClickListener {
            showAddPersonDialog()
        }

        binding.btnAddGroup.setOnClickListener {
            showAddGroupDialog()
        }

        binding.btnImportContacts.setOnClickListener {
            importContactsFromPhone()
        }

        binding.btnCleanDuplicates.setOnClickListener {
            cleanDuplicateContacts()
        }
    }

    private fun cleanDuplicateContacts() {
        lifecycleScope.launch {
            val all = db.personDao().getAllPersonsListSync()
            if (all.isEmpty()) {
                Toast.makeText(requireContext(), "Kayıtlı kişi bulunmuyor.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val groupedByPhone = all.groupBy { com.telesekreter.app.util.ContactUtils.normalizePhoneNumber(it.phone) }
            var deletedCount = 0

            for ((_, group) in groupedByPhone) {
                if (group.size > 1) {
                    // İlk kaydı veya yıldızlı olanı koru, diğerlerini sil
                    val keep = group.find { it.isImportant } ?: group.first()
                    for (duplicate in group) {
                        if (duplicate.id != keep.id) {
                            db.personDao().deletePerson(duplicate)
                            deletedCount++
                        }
                    }
                }
            }

            if (deletedCount > 0) {
                Toast.makeText(requireContext(), "$deletedCount adet mükerrer kişi temizlendi ve birleştirildi!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Mükerrer veya yinelenen kişi bulunamadı.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun importContactsFromPhone() {
        lifecycleScope.launch {
            try {
                val importedList = com.telesekreter.app.util.ContactUtils.importPhoneContacts(requireContext())
                if (importedList.isEmpty()) {
                    Toast.makeText(requireContext(), "Rehberde aktarılacak kişi bulunamadı veya rehber izni verilmedi.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Mevcut kayıtları hafızaya alıp numaraya göre eşleştiriyoruz (tekrarı engellemek için)
                val existingPersons = db.personDao().getAllPersonsListSync()
                val existingPhoneMap = existingPersons.associateBy { com.telesekreter.app.util.ContactUtils.normalizePhoneNumber(it.phone) }

                var insertedCount = 0
                var updatedCount = 0

                for (imported in importedList) {
                    val normalized = com.telesekreter.app.util.ContactUtils.normalizePhoneNumber(imported.phone)
                    val existing = existingPhoneMap[normalized]
                    if (existing != null) {
                        // Eğer mevcut kişi varsa sadece adı/soyadı güncelle veya koru, mükerrer 2. kayıt açma
                        val updated = existing.copy(
                            name = imported.name,
                            surname = imported.surname,
                            phone = normalized
                        )
                        db.personDao().updatePerson(updated)
                        updatedCount++
                    } else {
                        db.personDao().insertPerson(imported.copy(phone = normalized))
                        insertedCount++
                    }
                }

                val msg = if (insertedCount > 0 && updatedCount > 0) {
                    "$insertedCount yeni kişi eklendi, $updatedCount kişi güncellendi."
                } else if (insertedCount > 0) {
                    "$insertedCount kişi telefon rehberinizden başarıyla aktarıldı!"
                } else {
                    "Tüm kişileriniz zaten güncel ($updatedCount kişi kontrol edildi)."
                }

                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Rehber aktarma hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadGroups() {
        lifecycleScope.launch {
            db.groupDao().getAllGroups().collect { groups ->
                groupsList.clear()
                groupsList.addAll(groups)
                renderGroupChips()
            }
        }
    }

    private fun renderGroupChips() {
        binding.chipGroup.removeAllViews()

        // "Tümü" çipi
        val allChip = Chip(requireContext()).apply {
            text = "Tümü"
            isCheckable = true
            isChecked = selectedGroupId == null
            setOnClickListener {
                selectedGroupId = null
                loadPersons()
            }
        }
        binding.chipGroup.addView(allChip)

        groupsList.forEach { group ->
            val chip = Chip(requireContext()).apply {
                text = group.name
                isCheckable = true
                isChecked = selectedGroupId == group.id
                setOnClickListener {
                    selectedGroupId = group.id
                    loadPersons()
                }
            }
            binding.chipGroup.addView(chip)
        }
    }

    private fun loadPersons() {
        lifecycleScope.launch {
            val flow = if (selectedGroupId == null) {
                db.personDao().getAllPersons()
            } else {
                db.personDao().getPersonsInGroup(selectedGroupId!!)
            }

            flow.collect { list ->
                allLoadedPersons.clear()
                allLoadedPersons.addAll(list)
                filterPersons(binding.etSearchContact.text?.toString().orEmpty())
            }
        }
    }

    private fun showAddPersonDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_person, null)
        val etName = dialogView.findViewById<EditText>(R.id.etPersonName)
        val etSurname = dialogView.findViewById<EditText>(R.id.etPersonSurname)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPersonPhone)
        val etBirthDate = dialogView.findViewById<EditText>(R.id.etPersonBirthDate)
        val etNote = dialogView.findViewById<EditText>(R.id.etPersonNote)

        AlertDialog.Builder(requireContext())
            .setTitle("Yeni Kişi Ekle")
            .setView(dialogView)
            .setPositiveButton("Kaydet") { _, _ ->
                val name = etName.text.toString().trim()
                val surname = etSurname.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val birthDate = etBirthDate.text.toString().trim()
                val note = etNote.text.toString().trim()

                if (name.isBlank() || phone.isBlank()) {
                    Toast.makeText(requireContext(), "İsim ve Telefon boş bırakılamaz!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val newPerson = PersonEntity(
                        name = name,
                        surname = surname,
                        phone = phone,
                        birthDate = birthDate.ifBlank { null },
                        notes = note
                    )
                    val personId = db.personDao().insertPerson(newPerson)

                    // Eğer grup seçiliyse o gruba da ekle
                    selectedGroupId?.let { gId ->
                        db.personDao().insertPersonGroupCrossRef(PersonGroupCrossRef(personId, gId))
                    }

                    Toast.makeText(requireContext(), "$name eklendi.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showAddGroupDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Grup Adı (Örn: Ailem, Dernek)"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Yeni Grup Oluştur")
            .setView(input)
            .setPositiveButton("Oluştur") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotBlank()) {
                    lifecycleScope.launch {
                        db.groupDao().insertGroup(GroupEntity(name = name))
                        Toast.makeText(requireContext(), "$name grubu oluşturuldu.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class PersonAdapter(
        private val items: List<PersonEntity>,
        private val onWhatsAppClick: (PersonEntity) -> Unit,
        private val onCallClick: (PersonEntity) -> Unit,
        private val onDeleteClick: (PersonEntity) -> Unit,
        private val onStarClick: (PersonEntity) -> Unit
    ) : RecyclerView.Adapter<PersonAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemPersonBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvName.text = item.fullName
            holder.binding.tvPhone.text = item.phone
            holder.binding.btnStar.setImageResource(
                if (item.isImportant) android.R.drawable.star_big_on else android.R.drawable.star_big_off
            )
            holder.binding.btnStar.setOnClickListener { onStarClick(item) }
            holder.binding.btnWhatsApp.setOnClickListener { onWhatsAppClick(item) }
            holder.binding.btnCall.setOnClickListener { onCallClick(item) }
            holder.binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}
