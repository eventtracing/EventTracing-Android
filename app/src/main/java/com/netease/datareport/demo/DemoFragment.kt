package com.netease.datareport.demo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netease.cloudmusic.datareport.eventtracing.NodeBuilder

class DemoFragment : Fragment() {

    companion object{
        private const val TAG = "DemoFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_demo, container, false)

        val characterList = mutableListOf<Char>()
        var c = 'a'
        while (c <= 'z') {
            characterList.add(c)
            c++
        }

        val mLetterAdapter = LetterAdapter(characterList)

        val letterReView: RecyclerView = root.findViewById(R.id.re_view)
        val tempContent = root.findViewById<ViewGroup>(R.id.temp_content)
        NodeBuilder.setPageId(tempContent, "list_container")
        NodeBuilder.getNodeBuilder(letterReView).setPageId("listview")
            .setScrollEventEnable(true)

        letterReView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.i(TAG, "onScrolled x: ${dx}, y: $dy")
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.i(TAG, "onScrollStateChanged newState: $newState")
            }
        })
        letterReView.adapter = mLetterAdapter
        letterReView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        return root
    }
}


private class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tv1: TextView = itemView.findViewById(R.id.tv1)
    var tv2: TextView = itemView.findViewById(R.id.tv2)
    var item: View = itemView
}

private class LetterAdapter(private val dataList: List<Char>) : RecyclerView.Adapter<VH>() {
    var num = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        num++
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = dataList[position]
        holder.tv1.text = c.toString()
        holder.tv2.text = c.toInt().toString()
        NodeBuilder.getNodeBuilder(holder.item).setElementId("item $c")
            .setReuseIdentifier(position.toString()) //设置item复用的唯一标识
            .params()
            .putBIPosition(position)
            .putBICustomParam("content", c.toString())
            .putDynamicParams{ mutableMapOf(Pair<String, Any>("key", "$c"), Pair<String, Any>("pos", position)) }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}