package ykk.xc.com.bswms.entrance.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.MissionBill
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class MainTabFragment0_Adapter(private val context: Activity, private val datas: List<MissionBill>) : BaseArrayRecyclerAdapter<MissionBill>(datas) {
    private val df = DecimalFormat("#.####")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.aa_main_item0_adapter
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: MissionBill, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_date = holder.obtainView<TextView>(R.id.tv_date)
        val tv_billNo = holder.obtainView<TextView>(R.id.tv_billNo)
        val tv_missionType = holder.obtainView<TextView>(R.id.tv_missionType)
        val tv_missionContent = holder.obtainView<TextView>(R.id.tv_missionContent)
        val tv_sourceBillNo = holder.obtainView<TextView>(R.id.tv_sourceBillNo)
        val tv_outStockName = holder.obtainView<TextView>(R.id.tv_outStockName)
        val tv_deptName = holder.obtainView<TextView>(R.id.tv_deptName)

        // 赋值
        tv_row.text = (pos + 1).toString()
        tv_date.text = entity.createTime
        tv_billNo.text = entity.billNo
        //任务类型 	1：代表外购收料任务，21：代表销售发货任务,31：代表仓库外购收货任务,41：代表投料调拨，42：代表生产入库调拨，51：拣货任务单，52：出库质检任务，53：仓管复核任务
        when (entity.missionType) {
            1 -> tv_missionType.text = "锁库成品调拨任务"
            2 -> tv_missionType.text = "生产材料调拨任务"
        }
        val missionContent = Comm.isNULLS(entity.missionContent)
        if(missionContent.length > 0) {
            tv_missionContent.visibility = View.VISIBLE
        } else {
            tv_missionContent.visibility = View.INVISIBLE
        }
        tv_missionContent.text = Html.fromHtml("任务内容:&nbsp;<font color='#000000'>"+ Comm.isNULLS(entity.missionContent)+"</font>")
        tv_sourceBillNo.text = Html.fromHtml("来源单:&nbsp;<font color='#6a5acd'>"+ entity.sourceBillNo+"</font>")
        if(Comm.isNULLS(entity.outStockName).length > 0) {
            tv_outStockName.visibility = View.VISIBLE
            tv_outStockName.text = Html.fromHtml("调出仓库:&nbsp;<font color='#6a5acd'>" + entity.outStockName + "</font>")
        } else {
            tv_outStockName.visibility = View.INVISIBLE
        }
        if(entity.dept != null) {
            tv_deptName.visibility = View.VISIBLE
            tv_deptName.text = Html.fromHtml("部门:&nbsp;<font color='#000000'>" + entity.dept.departmentName + "</font>")
        } else {
            tv_deptName.visibility = View.INVISIBLE
        }

        val view = tv_row.parent as View
        if(entity.isCheck) {
            view.setBackgroundResource(R.drawable.back_style_check1_true)
        } else {
            view.setBackgroundResource(R.drawable.back_style_check1_false)
        }
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: MissionBill, position: Int)
    }


}
