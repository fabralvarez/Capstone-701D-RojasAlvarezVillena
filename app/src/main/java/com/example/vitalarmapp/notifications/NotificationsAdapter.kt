package com.example.vitalarmapp.notifications

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.databinding.ItemNotificationCardBinding
import java.text.DateFormat

internal class NotificationsAdapter :
    RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    private val items = mutableListOf<NotificationEntry>()

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
    fun submitList(entries: List<NotificationEntry>) {
        val previousSize = items.size
        items.clear()
        items.addAll(entries)
        if (previousSize == items.size) {
            notifyItemRangeChanged(0, items.size)
        } else {
            notifyDataSetChanged()
        }
    }

    class NotificationViewHolder(private val binding: ItemNotificationCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: NotificationEntry) {
            val formattedTimestamp = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT
            ).format(entry.timestamp)

            binding.notificationHeadline.text =
                binding.root.context.getString(
                    com.example.vitalarmapp.R.string.notifications_headline_format,
                    entry.title,
                    formattedTimestamp
                )

            binding.notificationSubhead.text = entry.detail
        }
    }
}
