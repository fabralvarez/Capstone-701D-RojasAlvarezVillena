package com.example.vitalarmapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vitalarmapp.databinding.ItemPastAlarmBinding
import com.example.vitalarmapp.utils.local.AlarmRecord

class PastAlarmsAdapter(
    private val onClick: (String) -> Unit,
) : ListAdapter<AlarmRecord, PastAlarmsAdapter.PastAlarmViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<AlarmRecord>() {
        override fun areItemsTheSame(oldItem: AlarmRecord, newItem: AlarmRecord) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AlarmRecord, newItem: AlarmRecord) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PastAlarmViewHolder {
        val binding = ItemPastAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PastAlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PastAlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PastAlarmViewHolder(
        private val binding: ItemPastAlarmBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AlarmRecord) {
            binding.pastAlarmTitle.text = item.patientName
            binding.pastAlarmSubtitle.text = binding.root.context.getString(
                R.string.past_alarm_subtitle,
                item.medicationName,
                item.medicationDetail,
                item.time
            )
            binding.root.setOnClickListener { onClick(item.id) }
        }
    }
}
