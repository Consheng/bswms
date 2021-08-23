package ykk.xc.com.bswms.produce.adapter

import android.app.Activity
import android.widget.TextView

import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.Procedure
import ykk.xc.com.bswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter

class Procedure_Sel_DialogAdapter(private val context: Activity, private val datas: List<Procedure>) : BaseArrayRecyclerAdapter<Procedure>(datas) {
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.procedure_sel_dialog_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: Procedure, pos: Int) {
        // 初始化id
        val tv_fname = holder.obtainView<TextView>(R.id.tv_fname)
        // 赋值
        tv_fname!!.setText(entity.procedureName)
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: Procedure, position: Int)
    }

}
