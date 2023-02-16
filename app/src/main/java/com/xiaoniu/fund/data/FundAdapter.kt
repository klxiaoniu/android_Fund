package com.xiaoniu.fund.data

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xiaoniu.fund.R
import com.xiaoniu.fund.ToastShort
import com.xiaoniu.fund.ui.FundDetailActivity

class FundAdapter(var list: List<Fund>, val mode: Int) :     //mode 0:首页 1:审核 2:我的
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TYPE_FOOTVIEW: Int = 1 //item类型：footview
    val TYPE_ITEMVIEW: Int = 2 //item类型：itemview
    var typeItem = TYPE_ITEMVIEW

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fund_title: TextView = view.findViewById(R.id.fund_title)
        val fund_desc: TextView = view.findViewById(R.id.fund_desc)
        val fund_cur_total: TextView = view.findViewById(R.id.fund_cur_total)
        val fund_in_check: TextView = view.findViewById(R.id.fund_in_check)
    }

    inner class FootViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_msg = itemView.findViewById<TextView>(R.id.tv_msg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (typeItem == TYPE_ITEMVIEW) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_fund, parent, false)
            ItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_foot, parent, false)
            FootViewHolder(view)

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            holder.fund_title.text = list[position].title
            holder.fund_desc.text = list[position].desc
            holder.fund_cur_total.text = "" + list[position].current + "/" + list[position].total
            holder.fund_in_check.visibility =
                if ((mode == 2) && (list[position].isPass == 0)) View.VISIBLE else View.GONE
            holder.itemView.setOnClickListener {
                val position = holder.adapterPosition
                val fund = list[position]
                val intent = Intent(it.context, FundDetailActivity::class.java)
                intent.putExtra("fund_id", fund.id)
                if (mode < 2) intent.putExtra("isCheck", mode)
                it.context.startActivity(intent)
            }
        } else if (holder is FootViewHolder) {
            holder.tv_msg.text = "加载更多"

            //当点击footview时，将该事件回调出去
            holder.tv_msg.setOnClickListener {
                footViewClickListener.invoke("")
            }
        }


    }

    fun setAdapterList(list2: List<Fund>) {
        list = list2
        notifyDataSetChanged()
    }

    fun plusAdapterList(list2: List<Fund>) {
        list = list.plus(list2)
        notifyDataSetChanged()
    }

    override fun getItemCount() = list.size + 1

    override fun getItemViewType(position: Int): Int {
        //设置在数据最底部显示footview
        typeItem = if (position == list.size) TYPE_FOOTVIEW else TYPE_ITEMVIEW
        return typeItem
    }

    var footviewPosition = 0
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (footviewPosition == holder.adapterPosition) {
            return
        }
        if (holder is FootViewHolder) {
            footviewPosition = holder.adapterPosition
            //回调查询事件
            footViewAttachedToWindowListener.invoke()
        }
    }


    //定义footview附加到Window上时的回调
    private var footViewAttachedToWindowListener: () -> Unit = { }
    fun setOnFootViewAttachedToWindowListener(pListener: () -> Unit) {
        this.footViewAttachedToWindowListener = pListener
    }

    //定义footview点击时的回调
    private var footViewClickListener: (String) -> Unit = { ToastShort("已无更多") }  //如果是默认，说明不是分页查询
    fun setOnFootViewClickListener(pListner: (String) -> Unit) {
        this.footViewClickListener = pListner
    }
}