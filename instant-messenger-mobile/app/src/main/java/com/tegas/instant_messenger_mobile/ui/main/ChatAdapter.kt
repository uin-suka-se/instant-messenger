package com.tegas.instant_messenger_mobile.ui.main

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem
import com.tegas.instant_messenger_mobile.databinding.ItemContactBinding
import com.tegas.instant_messenger_mobile.ui.detail.WebSocketService
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(
    private val context: Context,
    private var data: MutableList<ChatsItem> = mutableListOf(),
    private val listener: (ChatsItem) -> Unit,
//    private val context: Context
) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

//        private val userImages = context.resources.obtainTypedArray(R.array.user_images)

    fun setData(data: MutableList<ChatsItem>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun setFilteredList(mList: MutableList<ChatsItem>) {
        this.data = mList
        notifyDataSetChanged()
    }
    inner class ChatViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatsItem) {
            binding.tvName.text = item.name
            binding.tvMessage.text = item.lastMessage
            binding.tvTime.text = formatDateTime(item.lastMessageTime)

            Glide.with(itemView)
                .load(R.drawable.daniela_villarreal)
                .into(binding.ivPhoto)
//            val imageIndex = adapterPosition % userImages.length()
//            binding.ivPhoto.setImageResource(userImages.getResourceId(imageIndex, -1))
        }

        private fun formatDateTime(timestamp: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            return outputFormat.format(date)
        }

    }

//    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
//        super.onDetachedFromRecyclerView(recyclerView)
//        userImages.recycle()
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            ItemContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            listener(item)
        }
    }


}