package ykk.xc.com.bswms.basics.adapter

import android.app.Activity
import android.text.Html
import android.widget.TextView
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.k3Bean.ICItem
import ykk.xc.com.bswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter

class Mtl_DialogAdapter(private val context: Activity, private val datas: List<ICItem>) : BaseArrayRecyclerAdapter<ICItem>(datas) {
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.ab_mtl_list_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ICItem, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_fnumber = holder.obtainView<TextView>(R.id.tv_fnumber)
        val tv_fname = holder.obtainView<TextView>(R.id.tv_fname)
        val tv_fmodel = holder.obtainView<TextView>(R.id.tv_fmodel)
        // 赋值icItem/findListByPage
        tv_row!!.setText((pos + 1).toString())
        tv_fname!!.setText(entity.fname)
        tv_fnumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+entity.fnumber+"</font>")
        tv_fmodel.text = Html.fromHtml("规格:&nbsp;<font color='#6a5acd'>"+entity.fmodel+"</font>")
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: ICItem, position: Int)
    }

}
