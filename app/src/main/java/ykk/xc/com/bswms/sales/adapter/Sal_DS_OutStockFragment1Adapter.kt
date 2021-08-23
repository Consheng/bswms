package ykk.xc.com.bswms.sales.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.sal_ds_out_fragment1_item.view.*
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.ICStockBillEntry
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Sal_DS_OutStockFragment1Adapter(private val context: Activity, datas: List<ICStockBillEntry>) : BaseArrayRecyclerAdapter<ICStockBillEntry>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.sal_ds_out_fragment1_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ICStockBillEntry, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_mtlNumber = holder.obtainView<TextView>(R.id.tv_mtlNumber)
        val tv_mtlName = holder.obtainView<TextView>(R.id.tv_mtlName)
        val tv_batchNo = holder.obtainView<TextView>(R.id.tv_batchNo)
        val tv_fmodel = holder.obtainView<TextView>(R.id.tv_fmodel)
        val tv_num = holder.obtainView<TextView>(R.id.tv_num)
        val tv_sourceQty = holder.obtainView<TextView>(R.id.tv_sourceQty)
        val tv_sourceNo = holder.obtainView<TextView>(R.id.tv_sourceNo)
        val tv_strBarcode = holder.obtainView<TextView>(R.id.tv_strBarcode)
        val view_del = holder.obtainView<View>(R.id.view_del)
        val tv_stockName = holder.obtainView<TextView>(R.id.tv_stockName)
        val tv_stockAreaName = holder.obtainView<TextView>(R.id.tv_stockAreaName)
        val tv_storageRackName = holder.obtainView<TextView>(R.id.tv_storageRackName)
        val tv_stockPosName = holder.obtainView<TextView>(R.id.tv_stockPosName)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_mtlName.text = entity.icItem.fname
        tv_mtlNumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+entity.icItem.fnumber+"</font>")
        /*if(Comm.isNULLS(entity.strBatchCode).length > 0) {
            tv_batchNo.visibility = View.VISIBLE
            tv_batchNo.text = Html.fromHtml("批次:&nbsp;<font color='#6a5acd'>" + entity.strBatchCode + "</font>")
        } else {
            tv_batchNo.visibility = View.INVISIBLE
        }*/
        tv_fmodel.text = Html.fromHtml("规格:&nbsp;<font color='#6a5acd'>"+ Comm.isNULLS(entity.icItem.fmodel)+"</font>")

        tv_num.text = Html.fromHtml("出库数:&nbsp;<font color='#FF0000'>"+ df.format(entity.fqty) +"</font>")
        tv_sourceQty.text = Html.fromHtml("订单数:&nbsp;<font color='#6a5acd'>"+ df.format(entity.fsourceQty) +"</font>&nbsp;<font color='#666666'>"+ entity.unit.unitName +"</font>")
        tv_sourceNo.text = Html.fromHtml("订单:&nbsp;<font color='#6a5acd'>"+ entity.fsourceBillNo +"</font>")
        if(Comm.isNULLS(entity.strBarcode).length > 0) {
            tv_strBarcode.text = Html.fromHtml("条码:&nbsp;<font color='#6a5acd'>"+ entity.strBarcode +"</font>")
            tv_strBarcode.visibility = View.VISIBLE
        } else {
            tv_strBarcode.visibility = View.INVISIBLE
        }

        // 显示快递单
        /*if(entity.expressNoData != null) {
            view_del.visibility = View.VISIBLE
        } else {
            view_del.visibility = View.INVISIBLE
        }*/

        val parent = tv_row.parent as View
        if(entity.isComplimentary == 2000005 || entity.icItem.snManager.equals("Y")) { // 是否赠品，2000005代表是，2000006代表否
            parent.setBackgroundResource(R.drawable.back_style_gray3b)
        } else {
            parent.setBackgroundResource(R.drawable.back_style_check1_false)
        }

        // 主产品没有扫码的不显示
        if((entity.icItemType == 2000007 || entity.icItemType == 2000008) && entity.fqty == 0.0) { // 产品类型 2000007：主产品，2000008：副产品，2000009：原材料，2000010：其它
            parent.visibility = View.GONE
        } else {
            parent.visibility = View.VISIBLE
        }

        // 显示仓库组信息
        if(entity.stock != null ) {
            tv_stockName.visibility = View.VISIBLE
            tv_stockName.text = Html.fromHtml("仓库:&nbsp;<font color='#000000'>"+entity.stock!!.stockName+"</font>")
        } else {
            tv_stockName.visibility = View.INVISIBLE
        }
        if(entity.stockArea != null ) {
            tv_stockAreaName.visibility = View.VISIBLE
            tv_stockAreaName.text = Html.fromHtml("库区:&nbsp;<font color='#000000'>"+entity.stockArea!!.fname+"</font>")
        } else {
            tv_stockAreaName.visibility = View.INVISIBLE
        }
        if(entity.storageRack != null ) {
            tv_storageRackName.visibility = View.VISIBLE
            tv_storageRackName.text = Html.fromHtml("货架:&nbsp;<font color='#000000'>"+entity.storageRack!!.fnumber+"</font>")
        } else {
            tv_storageRackName.visibility = View.INVISIBLE
        }
        if(entity.stockPos != null ) {
            tv_stockPosName.visibility = View.VISIBLE
            tv_stockPosName.text = Html.fromHtml("库位:&nbsp;<font color='#000000'>"+entity.stockPos!!.stockPositionName+"</font>")
        } else {
            tv_stockPosName.visibility = View.INVISIBLE
        }
//        if(entity.container != null ) {
//            tv_containerName.visibility = View.VISIBLE
//            tv_containerName.text = Html.fromHtml("容器:&nbsp;<font color='#000000'>"+entity.container!!.fnumber+"</font>")
//        } else {
//            tv_containerName.visibility = View.INVISIBLE
//        }

        val click = View.OnClickListener { v ->
            when (v.id) {
                R.id.view_del // 删除行
                -> if (callBack != null) {
                    callBack!!.onDelete(entity, pos)
                }
            }
        }
        view_del!!.setOnClickListener(click)
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onDelete(entity: ICStockBillEntry, position: Int)
    }

}
