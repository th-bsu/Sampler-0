package com.th.chapter_11.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.th.chapter_11.R
import com.th.chapter_11.db.PostEntity

class PostAdapter
(
    private val layoutInflater: LayoutInflater
)
    :RecyclerView.Adapter<PostAdapter.PostViewHolder>()
{

    // TH: manages mutable ordered collection of elements.
    private val posts = mutableListOf<PostEntity>()

    // TH: holds on reference to given view(s) and binds data to given view(s).
    inner class PostViewHolder (containView: View)
        :RecyclerView.ViewHolder(containView)
    {

        private val textViewName: TextView      = containView.findViewById(R.id.view_post_row_name)
        private val textViewProductId: TextView = containView.findViewById(R.id.view_post_row_product_id)
        private val textViewAmount: TextView    = containView.findViewById(R.id.view_post_row_amount)
        private val textViewValue: TextView     = containView.findViewById(R.id.view_post_row_value)

        fun bind(postEntity: PostEntity){
            textViewName.text       = postEntity.name
            textViewProductId.text  = postEntity.productId.toString()
            textViewAmount.text     = postEntity.amount.toString()
            textViewValue.text      = postEntity.value.toString()
        }

    }

    // TH: gets invoked when RecyclerView needs new ViewHolder of the given type to represent item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder =
        PostViewHolder(layoutInflater.inflate(R.layout.view_post_row,parent,false))

    // TH: gets invoked by RecyclerView to display the data at the specified position.
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    // TH: notifies any registered observers that the data set has changed.
    // TH: forces any observers to assume that all existing items and structure may no longer be valid.
    fun updatePosts(posts: List<PostEntity>) {
        this.posts.clear()
        this.posts.addAll(posts)
        this.notifyDataSetChanged()
    }

}