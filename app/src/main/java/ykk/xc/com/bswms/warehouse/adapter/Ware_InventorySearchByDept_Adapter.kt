package ykk.xc.com.bswms.warehouse.adapter

import android.app.Activity
import android.text.Html
import android.widget.TextView
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.k3Bean.Inventory_K3
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Ware_InventorySearchByDept_Adapter(private val context: Activity, datas: List<Inventory_K3>) : BaseArrayRecyclerAdapter<Inventory_K3>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.ware_inventory_search_by_dept_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: Inventory_K3, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_mtlNumber = holder.obtainView<TextView>(R.id.tv_mtlNumber)
        val tv_mtlName = holder.obtainView<TextView>(R.id.tv_mtlName)
        val tv_stockQty = holder.obtainView<TextView>(R.id.tv_stockQty)
        val tv_assistQty = holder.obtainView<TextView>(R.id.tv_assistQty)

//        val tv_stockName = holder.obtainView<TextView>(R.id.tv_stockName)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_mtlName.text = entity.mtlName
        tv_mtlNumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+entity.mtlNumber+"</font>")
        tv_stockQty.text = Html.fromHtml("库存:&nbsp;<font color='#FF0000'><big>"+ df.format(entity.fqty) +"</big></font>")
        tv_assistQty.text = Html.fromHtml("辅助数量:&nbsp;<font color='#6a5acd'><big>"+ df.format(entity.fsecQty) +"</big></font>")
//        tv_stockName.text = Html.fromHtml("仓库:&nbsp;<font color='#000000'>"+entity.stockName+"</font>")

    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
    }

}
