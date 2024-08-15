package com.tegas.instant_messenger_mobile.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils.formatDateTime
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.ParticipantDataItem
import com.tegas.instant_messenger_mobile.databinding.ItemChatsBinding
import com.tegas.instant_messenger_mobile.utils.AndroidDownloader
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(
    private val context: Context,
    private val viewModel: DetailViewModel,
    private val nim: String,
    private val data: MutableList<MessagesItem> = mutableListOf(),
    private val chatType: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    fun setData(data: MutableList<MessagesItem>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }
    fun addMessage(message: MessagesItem) {
        data.add(message)
        notifyItemInserted(data.size - 1)
    }

    fun updateMessageStateToRead() {
        // Loop through the data and update the icon for all messages sent by the user
        for (message in data) {
            if (message.senderId != nim) {
                message.state = 3 //
            }
        }
        notifyDataSetChanged()
    }

    fun updateMessageStateToDelivered() {
        // Loop through the data and update the icon for all messages sent by the user
        for (message in data) {
            if (message.senderId != nim) {
                message.state = 2
            }
        }
        notifyDataSetChanged()
    }

    // Fungsi tambahan untuk memperbarui state pesan di RecyclerView
//    fun setMessageRead(read: Boolean) {
//
//    }


    val downloader = AndroidDownloader(context)

    inner class MessageViewHolder(private val binding: ItemChatsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessagesItem) {

            if (item.senderId != nim) {

                if (item.attachments != "") {
                    binding.layoutReceived.tvAttachments.visibility = View.VISIBLE
                    binding.layoutReceived.tvAttachments.text = item.attachments.toString()
                    binding.layoutReceived.tvAttachments.setOnClickListener {
//                        viewModel.downloadFiles(
//                            binding.root.context,
//                            item.attachments.toString(),
//                            item.attachments.toString()
//                                .substring(item.attachments.toString().lastIndexOf('/') + 1),
//                            "Downloading"
//                        )

                        val path = item.attachments.toString()
                        val baseUrl = "http://192.168.137.1:5000/download"

                        fun encodeUrl(baseUrl: String, path: String): String {
                            val builder = Uri.parse(baseUrl).buildUpon()
                            builder.appendQueryParameter("path", path)
                            return builder.build().toString()
                        }

                        val encodeUrl = encodeUrl(baseUrl, path)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(encodeUrl))
                        context.startActivity(intent)

//                        downloader.downloadFile(encodeUrl)
                    }

                }
                binding.layoutSent.itemSents.visibility = View.GONE
                binding.layoutReceived.chatReceived.text = item.content
                binding.layoutReceived.tvTime.text = formatDateTime(item.sentAt)
                binding.layoutReceived.tvName.text = item.senderName

                if (chatType == "group") {
                    binding.layoutReceived.tvName.visibility = View.VISIBLE
                } else {
                    binding.layoutReceived.tvName.visibility = View.GONE
                }

            } else {

                if (item.attachments != "") {
                    binding.layoutSent.tvAttachments.visibility = View.VISIBLE
                    binding.layoutSent.tvAttachments.text = item.attachments.toString()
                    binding.layoutSent.tvAttachments.setOnClickListener {
                        val path = item.attachments.toString()
                        val baseUrl = "http://192.168.137.1:5000/download"

                        fun encodeUrl(baseUrl: String, path: String): String {
                            val builder = Uri.parse(baseUrl).buildUpon()
                            builder.appendQueryParameter("path", path)
                            return builder.build().toString()
                        }

                        val encodeUrl = encodeUrl(baseUrl, path)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(encodeUrl))
                        context.startActivity(intent)

//                        viewModel.downloadFiles(
//                            binding.root.context,
//                            encodeUrl,
//                            path.substring(item.attachments.toString().lastIndexOf('/') + 1),
//                            "Downloading"
//                        )

//                        downloader.downloadFile(encodeUrl)

                    }
                }

                binding.layoutReceived.itemReceived.visibility = View.GONE
                binding.layoutSent.chatSent.text = item.content
                binding.layoutSent.tvTime.text = formatDateTime(item.sentAt)
                binding.layoutSent.tvName.text = item.senderName

                binding.layoutSent.tvName.visibility = View.GONE

                // Update the drawable based on the read state
                binding.layoutSent.messageState.setImageResource(
                    if (item.state == 1) R.drawable.single_check else if(item.state == 2) R.drawable.black_double else R.drawable.double_check
                )
            }
        }

        // New method to update message state drawable if message is read
//        fun changeMessageIcon() {
//            binding.layoutSent.messageState.setImageResource(R.drawable.double_check)
//        }

        private fun formatDateTime(timestamp: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            return outputFormat.format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            (ItemChatsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
                    )
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }
}