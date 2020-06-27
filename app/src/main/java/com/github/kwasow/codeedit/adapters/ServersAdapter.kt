package com.github.kwasow.codeedit.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kwasow.codeedit.ServerDetailsActivity
import com.github.kwasow.codeedit.databinding.ViewServerBinding
import com.github.kwasow.codeedit.utils.RemoteInfoManager

class ServersAdapter(private val dataset: MutableList<RemoteInfoManager>, private val connected: String?)
    : RecyclerView.Adapter<ServersAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, layoutBinding: ViewServerBinding)
        : RecyclerView.ViewHolder(itemView) {
        var rootCard = layoutBinding.serverRootCard

        var serverName = layoutBinding.serverName
        var serverUsernameAddress = layoutBinding.serverUsernameAddress
        var serverOs = layoutBinding.serverOS
        var serverStatus = layoutBinding.serverStatus
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutBinding = ViewServerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)

        return ViewHolder(layoutBinding.root, layoutBinding)
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