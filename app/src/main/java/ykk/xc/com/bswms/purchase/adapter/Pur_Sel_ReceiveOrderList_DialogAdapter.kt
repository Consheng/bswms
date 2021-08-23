package ykk.xc.com.bswms.purchase.adapter

import android.app.Activity
import android.view.View
import android.widget.TextView
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.pur.POInStock
import ykk.xc.com.bswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter

class Pur_Sel_ReceiveOrderList_DialogAdapter(private val context: Activity, private val datas: List<POInStock>) : BaseArrayRecyclerAdapter<POInStock>(datas) {
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.pur_sel_receive_orderlist_dialog_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: POInStock, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_fnumber = holder.obtainView<TextView>(R.id.tv_fnumber)
        val tv_fdate = holder.obtainView<TextView>(R.id.tv_fdate)
        val tv_waitUnload = holder.obtainView<TextView>(R.id.tv_waitUnload)

        // 赋值
        tv_row!!.setText((pos + 1).toString())
        tv_fnumber!!.setText(entity.fbillno)
        tv_fdate!!.setText(entity.fdate)
        if(entity.isWmsUploadStatus) {
            tv_waitUnload.visibility = View.VISIBLE
        } else {
            tv_waitUnload.visibility = View.INVISIBLE
        }
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: POInStock, position: Int)
    }

}
