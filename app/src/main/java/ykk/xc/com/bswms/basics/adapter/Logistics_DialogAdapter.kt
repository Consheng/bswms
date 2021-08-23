package ykk.xc.com.bswms.basics.adapter

import android.app.Activity
import android.view.View
import android.widget.TextView
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.Logistics
import ykk.xc.com.bswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter

class Logistics_DialogAdapter(private val context: Activity, private val datas: List<Logistics>) : BaseArrayRecyclerAdapter<Logistics>(datas) {
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.ab_logistics_dialog_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: Logistics, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_fnumber = holder.obtainView<TextView>(R.id.tv_fnumber)
        val tv_fname = holder.obtainView<TextView>(R.id.tv_fname)
        // 赋值
        tv_row!!.setText((pos + 1).toString())
        tv_fnumber!!.setText(entity.logisticsNumber)
        tv_fname!!.setText(entity.logisticsName)

        val view = tv_row!!.getParent() as View
        if (entity.isDefault == 1) {
            view.setBackgroundResource(R.drawable.back_style_check1_true)
        } else {
            view.setBackgroundResource(R.drawable.back_style_check1_false)
        }
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: Logistics, position: Int)
    }

}
