package com.github.kwasow.codeedit.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.github.kwasow.codeedit.R
import com.github.kwasow.codeedit.ServerDetailsActivity
import com.github.kwasow.codeedit.utils.RemoteInfoManager

class ServersAdapter(private val dataset: MutableList<RemoteInfoManager>, private val connected: String?) : RecyclerView.Adapter<ServersAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TODO: This should use layout binding
        var rootCard: CardView = itemView.findViewById(R.id.serverRootCard)

        var serverName: TextView = itemView.findViewById(R.id.serverName)
        var serverUsernameAddress: TextView = itemView.findViewById(R.id.serverUsernameAddress)
        var serverOs: TextView = itemView.findViewById(R.id.serverOS)

        var serverStatus: TextView = itemView.findViewById(R.id.serverStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val serverView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_server, parent, false)

        return ViewHolder(serverView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.rootCard.setOnClickListener {
            val intent = Intent(it.context, ServerDetailsActivity::class.java)
            intent.putExtra("details", dataset[position])
            it.context.startActivity(intent)
        }

        holder.serverName.text = dataset[position].alias
        holder.serverUsernameAddress.text =
            " ${dataset[position].username}@${dataset[position].hostname}"
        holder.serverOs.text = " " + dataset[position].os

        if (connected != null &&
            connected == "${dataset[position].username}@${dataset[position].hostname}:${dataset[position].port}") {
            holder.serverStatus.visibility = View.VISIBLE
        } else {
            holder.serverStatus.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}