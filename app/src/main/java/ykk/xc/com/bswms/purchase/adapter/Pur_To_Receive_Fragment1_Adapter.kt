package ykk.xc.com.bswms.purchase.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.pur.POOrderEntry
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Pur_To_Receive_Fragment1_Adapter(private val context: Activity, datas: List<POOrderEntry>) : BaseArrayRecyclerAdapter<POOrderEntry>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.pur_to_receive_fragment1_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: POOrderEntry, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_mtlNumber = holder.obtainView<TextView>(R.id.tv_mtlNumber)
        val tv_mtlName = holder.obtainView<TextView>(R.id.tv_mtlName)
        val tv_fmodel = holder.obtainView<TextView>(R.id.tv_fmodel)
        val tv_num = holder.obtainView<TextView>(R.id.tv_num)
        val tv_sourceQty = holder.obtainView<TextView>(R.id.tv_sourceQty)
        val tv_sourceNo = holder.obtainView<TextView>(R.id.tv_sourceNo)
        val view_del = holder.obtainView<View>(R.id.view_del)

        // 赋值
        tv_row.text = entity.rowNo.toString()
        tv_mtlName.text = entity.icItem.fname
        tv_mtlNumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+entity.icItem.fnumber+"</font>")
        tv_fmodel.text = Html.fromHtml("规格型号:&nbsp;<font color='#6a5acd'>"+ Comm.isNULLS(entity.icItem.fmodel)+"</font>")
        tv_num.text = Html.fromHtml("送货数:&nbsp;<font color='#000000'>"+ df.format(entity.realQty) +"</font>")
        tv_sourceQty.text = Html.fromHtml("源单数:&nbsp;<font color='#6a5acd'>"+ df.format(entity.useableQty) +"</font>&nbsp;<font color='#000000'>"+ entity.unitName +"</font>")
        tv_sourceNo.text = Html.fromHtml("源单号:&nbsp;<font color='#000000'>"+ entity.fbillno +"</font>")

        val click = View.OnClickListener { v ->
            when (v.id) {
                R.id.view_del -> {// 删除行
                    if (callBack != null) {
                        callBack!!.onDelete(entity, pos)
                    }
                }
            }
        }
        view_del!!.setOnClickListener(click)
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onDelete(entity: POOrderEntry, position: Int)
    }

}
