package com.example.vitalarmapp.notifications

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.databinding.ItemNotificationCardBinding

internal data class ChangelogEntry(
    val headline: String,
    val subhead: String,
)

internal class NotificationsAdapter :
    RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    private val items = mutableListOf<ChangelogEntry>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(entries: List<ChangelogEntry>) {
        items.clear()
        items.addAll(entries)
        notifyDataSetChanged()
    }

    class NotificationViewHolder(private val binding: ItemNotificationCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: ChangelogEntry) {
            binding.notificationHeadline.text = entry.headline
            binding.notificationSubhead.text = entry.subhead
        }
    }
}
