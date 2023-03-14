package com.example.tripledes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.tripledes.data.User

internal class UserAdapter(
    private var users: ArrayList<User>,
    private val context: Context,
    private val onItemClicked: (User) -> Unit) :
    RecyclerView.Adapter<UserAdapter.MyViewHolder>() {

    var checkedPosition = -1

    lateinit var clickListener: OnItemClickListener

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemTextView: TextView = view.findViewById(R.id.itemTextView)
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = users[position]
        holder.itemTextView.text = item.name
        holder.itemTextView.setOnClickListener {
            Toast.makeText(context, "position: $position", Toast.LENGTH_LONG).show()
        }
        checkedPosition = holder.adapterPosition

        holder.itemTextView.setOnClickListener {
            onItemClicked(item)
        }

    }
    override fun getItemCount(): Int {
        return users.size
    }

    fun getUser(): User? {
        if (checkedPosition != -1) {
            return users[checkedPosition]
        }
        return null
    }
}