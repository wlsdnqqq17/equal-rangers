package com.example.project_equal.network

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_equal.R

class RankingAdapter(private val rankings: List<RankData>) :
    RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    class RankingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userIdText: TextView = view.findViewById(R.id.userid_text)
        val scoreText: TextView = view.findViewById(R.id.score_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val rank = rankings[position]
        holder.userIdText.text = rank.user_id
        holder.scoreText.text = rank.score.toString()
    }

    override fun getItemCount(): Int = rankings.size
}
