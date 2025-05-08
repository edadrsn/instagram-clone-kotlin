package com.example.kotlininstagram.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlininstagram.databinding.RecyclerRowBinding
import com.example.kotlininstagram.model.Post

class FeedRecyclerAdapter(private val postList : ArrayList<Post>) : RecyclerView.Adapter<FeedRecyclerAdapter.PostHolder>() {

    class PostHolder(val binding:RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size

    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.recyclerEmailText.text = postList.get(position).userEmail
        holder.binding.recyclerComment.text = postList.get(position).comment

    }


}