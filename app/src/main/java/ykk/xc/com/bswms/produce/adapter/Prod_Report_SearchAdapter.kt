package ykk.xc.com.bswms.produce.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.ProdReport
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Prod_Report_SearchAdapter(private val context: Activity, datas: List<ProdReport>) : BaseArrayRecyclerAdapter<ProdReport>(datas) {
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.prod_report_search_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ProdReport, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_mtlName = holder.obtainView<TextView>(R.id.tv_mtlName)
        val tv_prodNo = holder.obtainView<TextView>(R.id.tv_prodNo)
        val tv_procedureName = holder.obtainView<TextView>(R.id.tv_procedureName)
        val tv_reportType = holder.obtainView<TextView>(R.id.tv_reportType)
        val tv_userName = holder.obtainView<TextView>(R.id.tv_userName)
        val tv_barcode = holder.obtainView<TextView>(R.id.tv_barcode)
        val tv_reportDate = holder.obtainView<TextView>(R.id.tv_reportDate)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_mtlName.text = entity.icItem.fname
        tv_prodNo.text = Html.fromHtml("生产单号:&nbsp;<font color='#6a5acd'>"+entity.icmoFbillNo+"</font>")
        tv_procedureName.text = Html.fromHtml("工序:&nbsp;<font color='#6a5acd'>" + entity.procedure.procedureName + "</font>")
        tv_userName.text = Html.fromHtml("汇报人:&nbsp;<font color='#000000'>"+ entity.user.username+"</font>")
        tv_barcode.text = Html.fromHtml("条码:&nbsp;<font color='#6a5acd'>"+ entity.barCodeTable.barcode+"</font>")
        tv_reportDate.text = Html.fromHtml("报工时间:&nbsp;<font color='#000000'>"+ entity.reportTime.substring(0,19) +"</font>")
        if(entity.reportType == 'B') {
            tv_reportType.visibility = View.VISIBLE
        } else {
            tv_reportType.visibility = View.INVISIBLE
        }
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onDelete(entity: ProdReport, position: Int)
    }

}
