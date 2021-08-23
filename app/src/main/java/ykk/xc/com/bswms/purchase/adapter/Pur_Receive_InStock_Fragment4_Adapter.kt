package ykk.xc.com.bswms.purchase.adapter

import android.app.Activity
import android.widget.TextView
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.ICStockBillEntry
import ykk.xc.com.bswms.bean.ICStockBillEntry_Barcode
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Pur_Receive_InStock_Fragment4_Adapter(private val context: Activity, datas: List<ICStockBillEntry_Barcode>) : BaseArrayRecyclerAdapter<ICStockBillEntry_Barcode>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.pur_receive_in_stock_fragment4_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ICStockBillEntry_Barcode, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_barcode = holder.obtainView<TextView>(R.id.tv_barcode)
        val tv_batchCode = holder.obtainView<TextView>(R.id.tv_batchCode)
        val tv_snCode = holder.obtainView<TextView>(R.id.tv_snCode)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_barcode.text = entity.barcode
        tv_batchCode.text = Comm.isNULLS(entity.batchCode)
        tv_snCode.text = Comm.isNULLS(entity.snCode)
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onDelete(entity: ICStockBillEntry, position: Int)
    }

}
