package app.chintan.naturist.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.chintan.naturist.databinding.PostItemBinding
import app.chintan.naturist.model.Post
import coil.load

class PostListAdapter(private val list: List<Post>, val adapterOnClick: (Any) -> Unit) :
    RecyclerView.Adapter<PostListAdapter.BlogItemView>() {
    inner class BlogItemView(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.postTitle.text = post.title
            binding.postDesc.text = post.description
            binding.postImage.load(post.imageUrl)

            binding.root.setOnClickListener { adapterOnClick(post) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogItemView = BlogItemView(
        PostItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: BlogItemView, position: Int) = holder.bind(list[position])
}