package com.telesekreter.app.ui.templates

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
import com.telesekreter.app.data.local.entity.MessageTemplateEntity
import com.telesekreter.app.databinding.FragmentTemplatesBinding
import com.telesekreter.app.databinding.ItemTemplateBinding
import kotlinx.coroutines.launch

class TemplatesFragment : Fragment() {

    private var _binding: FragmentTemplatesBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private val templateList = mutableListOf<MessageTemplateEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemplatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupListeners()
        observeTemplates()
    }

    private fun setupRecyclerView() {
        binding.rvTemplates.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTemplates.adapter = TemplateAdapter(
            templateList,
            onShareWhatsAppClick = { template ->
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, template.content)
                    setPackage("com.whatsapp")
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    // WhatsApp yüklü değilse genel paylaşım aç
                    val generalIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, template.content)
                    }
                    startActivity(android.content.Intent.createChooser(generalIntent, "Şablonu Paylaş"))
                }
            },
            onDeleteClick = { template ->
                lifecycleScope.launch {
                    db.messageTemplateDao().deleteTemplate(template)
                    Toast.makeText(requireContext(), "${template.title} silindi.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupListeners() {
        binding.btnAddTemplate.setOnClickListener {
            showAddTemplateDialog()
        }
    }

    private fun showAddTemplateDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_template, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTemplateTitle)
        val etCategory = dialogView.findViewById<EditText>(R.id.etTemplateCategory)
        val etContent = dialogView.findViewById<EditText>(R.id.etTemplateContent)

        AlertDialog.Builder(requireContext())
            .setTitle("Yeni Mesaj Şablonu")
            .setView(dialogView)
            .setPositiveButton("Kaydet") { _, _ ->
                val title = etTitle.text.toString().trim()
                val category = etCategory.text.toString().trim().ifBlank { "Genel" }
                val content = etContent.text.toString().trim()

                if (title.isBlank() || content.isBlank()) {
                    Toast.makeText(requireContext(), "Başlık ve Mesaj boş bırakılamaz!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val entity = MessageTemplateEntity(
                        title = title,
                        category = category,
                        content = content
                    )
                    db.messageTemplateDao().insertTemplate(entity)
                    Toast.makeText(requireContext(), "$title şablonu eklendi.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun observeTemplates() {
        lifecycleScope.launch {
            db.messageTemplateDao().getAllTemplates().collect { list ->
                templateList.clear()
                templateList.addAll(list)
                binding.rvTemplates.adapter?.notifyDataSetChanged()
                binding.tvEmptyTemplates.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TemplateAdapter(
        private val items: List<MessageTemplateEntity>,
        private val onShareWhatsAppClick: (MessageTemplateEntity) -> Unit,
        private val onDeleteClick: (MessageTemplateEntity) -> Unit
    ) : RecyclerView.Adapter<TemplateAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemTemplateBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemTemplateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvTitle.text = item.title
            holder.binding.tvCategory.text = item.category
            holder.binding.tvContent.text = item.content
            holder.binding.btnShareWhatsApp.setOnClickListener { onShareWhatsAppClick(item) }
            holder.binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}
