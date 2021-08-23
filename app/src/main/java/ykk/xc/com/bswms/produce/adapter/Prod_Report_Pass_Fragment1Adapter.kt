package ykk.xc.com.bswms.produce.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.ProdReportSel
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Prod_Report_Pass_Fragment1Adapter(private val context: Activity, datas: List<ProdReportSel>) : BaseArrayRecyclerAdapter<ProdReportSel>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.prod_report_pass_fragment1_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ProdReportSel, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_userName = holder.obtainView<TextView>(R.id.tv_userName)
        val tv_procedureName = holder.obtainView<TextView>(R.id.tv_procedureName)
        val tv_fqty = holder.obtainView<TextView>(R.id.tv_fqty)
        val tv_reportTime = holder.obtainView<TextView>(R.id.tv_reportTime)
        val tv_classesName = holder.obtainView<TextView>(R.id.tv_classesName)
        val tv_styleName = holder.obtainView<TextView>(R.id.tv_styleName)
        val tv_structureName = holder.obtainView<TextView>(R.id.tv_structureName)
        val view_del = holder.obtainView<View>(R.id.view_del)


        // 赋值
        tv_row.text = (pos+1).toString()
        tv_procedureName.text = Html.fromHtml("工序:&nbsp;<font color='#FF4400'>" + entity.procedure.procedureName + "</font>")
        tv_userName.text = entity.user.username
        tv_fqty.text = Html.fromHtml("数量:&nbsp;<font color='#FF0000'><big>"+ df.format(entity.fqty) +"</big></font>")
//        tv_reportTime.text = Html.fromHtml("时间:&nbsp;<font color='#000000'>"+ entity.reportTime +"</font>")
        tv_reportTime.text = entity.reportTime
        tv_classesName.text = Html.fromHtml("产品类别:&nbsp;<font color='#6a5acd'>"+ entity.classesName+"</font>")
        tv_styleName.text = Html.fromHtml("款式:&nbsp;<font color='#6a5acd'>"+ entity.styleName+"</font>")
        tv_structureName.text = Html.fromHtml("结构:&nbsp;<font color='#6a5acd'>"+ entity.structureName+"</font>")

        val view = tv_row!!.getParent() as View
        if (entity.isCheckRow) {
            view.setBackgroundResource(R.drawable.back_style_check1_true)
        } else {
            view.setBackgroundResource(R.drawable.back_style_check1_false)
        }

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
        fun onDelete(entity: ProdReportSel, position: Int)
    }

}
