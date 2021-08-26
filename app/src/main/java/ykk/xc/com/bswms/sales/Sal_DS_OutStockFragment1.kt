package ykk.xc.com.bswms.sales

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import kotlinx.android.synthetic.main.sal_ds_out_fragment1.*
import okhttp3.*
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.basics.Logistics_DialogActivity
import ykk.xc.com.bswms.basics.Stock_GroupDialogActivity
import ykk.xc.com.bswms.bean.*
import ykk.xc.com.bswms.bean.k3Bean.SeOrderEntry
import ykk.xc.com.bswms.bean.prod.ProdOrder
import ykk.xc.com.bswms.comm.BaseFragment
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.sales.adapter.Sal_DS_OutStockFragment1Adapter
import ykk.xc.com.bswms.util.BigdecimalUtil
import ykk.xc.com.bswms.util.JsonUtil
import ykk.xc.com.bswms.util.LogUtil
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：电商出库---添加明细
 * 作者：ykk
 */
class Sal_DS_OutStockFragment1 : BaseFragment() {

    companion object {
        private val SEL_POSITION = 61
        private val SEL_LOGISTICS = 62
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SUCC2 = 201
        private val UNSUCC2 = 501
        private val SUCC3 = 202
        private val UNSUCC3 = 502
        private val SUCC4 = 203
        private val UNSUCC4 = 503
        private val SAVE = 204
        private val UNSAVE = 504
        private val FIND_DEPT = 205
        private val UNFIND_DEPT = 505
        private val FIND_LAST = 206
        private val UNFIND_LAST = 506
        private val SAVE_LAST = 207
        private val UNSAVE_LAST = 507
        private val FIND_STOCK_STATUS = 208
        private val UNFIND_STOCK_STATUS = 508

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val WRITE_CODE = 3
        private val RESULT_NUM = 4
        private val RESULT_SALORDER_ORDER = 5
        private val RESULT_YUYUE = 6

        private val STOCK_FLAG = "DS_SalOutStock_stock"
        private val STOCKAREA_FLAG = "DS_SalOutStock_stockArea"
        private val STORAGERACK_FLAG = "DS_SalOutStock_storageRack"
        private val STOCKPOS_FLAG = "DS_SalOutStock_stockPos"
    }
    private val context = this
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var stock:Stock? = null
    private var stockArea:StockArea? = null
    private var storageRack:StorageRack? = null
    private var stockPos:StockPosition? = null
    private var mContext: Activity? = null
    private val df = DecimalFormat("#.######")
    private var parent: Sal_DS_OutStockMainActivity? = null
    private var mAdapter: Sal_DS_OutStockFragment1Adapter? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var timesTamp:String? = null // 时间戳
    private var smqFlag = '1' // 扫描类型1：位置扫描，2：物料扫描
    private var curPos:Int = -1 // 当前行
    private val checkDatas = ArrayList<ICStockBillEntry>()
    private val salOutStock_ExpressNos = ArrayList<SalOutStock_ExpressNo>()
    private var logistics :Logistics? = null
    private var salEntryId = 0   // 记录当前扫描的物料类别是脚垫对应的分录行id
    private var footPadExpressNo :String? = null  // 记录当前扫描的物料类别是脚垫对应的快递单
    private var dept :Department? = null // 记录发货部门
    private var defaultDept :Department? = null // 默认锁库发货部门
    private var listPrintDate = ArrayList<ExpressNoData>()
    private var btTmp :BarCodeTable? = null // 记录最后一个工序
    private var printExpressNo :String? = null  // 要打印的快递单

    // 消息处理
    private val mHandler = MyHandler(this)
    private class MyHandler(activity: Sal_DS_OutStockFragment1) : Handler() {
        private val mActivity: WeakReference<Sal_DS_OutStockFragment1>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()

                var errMsg: String? = null
                var msgObj: String? = null
                if (msg.obj is String) {
                    msgObj = msg.obj as String
                }
                when (msg.what) {
                    SUCC1 -> { // 扫码成功 进入
                        when(m.smqFlag) {
                            '1'-> { // 仓库位置
                                m.stock = null
                                m.stockArea = null
                                m.storageRack = null
                                m.stockPos = null
                                m.getStockGroup(msgObj)
                            }
                            '2'-> { // 物料
                                val bt = JsonUtil.strToObject(msgObj, BarCodeTable::class.java)
                                if(m.dept == null) {
                                    if(bt.dept != null) {
                                        m.dept = bt.dept
                                    } else {
                                        m.dept = m.defaultDept
                                    }
                                    m.tv_deptName.text = m.dept!!.departmentName
                                }
                                m.setICStockBill_Row(bt)
                            }
                        }
                        m.isTextChange = false
                    }
                    UNSUCC1 -> { // 扫码失败
                        m.isTextChange = false
                        when(m.smqFlag) {
                            '1' -> { // 仓库位置扫描
                                m.tv_positionName.text = ""
                            }
                        }
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC3 -> { // 得到打印数据 进入
                        val list = JsonUtil.strToList(msgObj, ExpressNoData::class.java)

                        if(m.checkDatas[m.curPos].expressNoData != null) {
                            m.checkDatas[m.curPos].expressNoData = list[0]
                            // 添加到（条码、快递单、订单号）记录表
                            m.setBarcode_ExpressNo(m.curPos, list[0].t01)
                            if(m.salEntryId > 0 && m.footPadExpressNo == null) {
                                m.salEntryId = 0
                                m.footPadExpressNo = list[0].t01
                            }
                           /* val listPrintDate = ArrayList<ExpressNoData>()
                            listPrintDate.add(list[0])
                            m.parent!!.setFragment1DataByPrint(listPrintDate) // 打印*/
                            // 记录打印数据，改成保存后打印
                            m.listPrintDate.clear()
                            m.listPrintDate.add(list[0])

                        } else {
                            val mapExist = HashMap<String, Boolean>()
                            m.checkDatas.forEachIndexed { index, it ->
                                if(it.expressNoData != null) {
                                    mapExist.put(it.expressNoData.t01, true)
                                }
                            }
                            var list2 = ArrayList<ExpressNoData>()
                            list.forEach{
                                if(!mapExist.containsKey(it.t01)) {
                                    list2.add(it)
                                }
                            }
                            if(list2.size > 0) {
                                m.checkDatas[m.curPos].expressNoData = list2[0]
                                // 添加到（条码、快递单、订单号）记录表
                                m.setBarcode_ExpressNo(m.curPos, list2[0].t01)
                                if(m.salEntryId > 0 && m.footPadExpressNo == null) {
                                    m.salEntryId = 0
                                    m.footPadExpressNo = list2[0].t01
                                }

                                /*val listPrintDate = ArrayList<ExpressNoData>()
                                listPrintDate.add(list2[0])
                                m.parent!!.setFragment1DataByPrint(listPrintDate) // 打印*/
                                // 记录打印数据，改成保存后打印
                                m.listPrintDate.clear()
                                m.listPrintDate.add(list2[0])
                            }
                        }
                        m.mAdapter!!.notifyDataSetChanged()

                        // 得到打印数据，就保存
//                        m.run_save()
                        m.run_findLastProcedure()
                    }
                    UNSUCC3 -> { // 得到打印数据  失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC4 -> { // 预约数据 进入
                        m.toasts("预约成功，开始打印...")
                        val list = JsonUtil.strToList(msgObj, ExpressNoData::class.java)
                        m.parent!!.setPrintData(list) // 打印
                        list.forEach {
                            m.setBarcode_ExpressNo(-1, it.t01)
                        }
                    }
                    UNSUCC4 -> { // 预约数据  失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "app预约失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SAVE -> { // 保存成功 进入
//                        val list = JsonUtil.strToList(msgObj, ICStockBillEntry::class.java)
//                        m.checkDatas.clear()
//                        m.checkDatas.addAll(list)
//                        m.mAdapter!!.notifyDataSetChanged()
//                        m.reset()
                        m.printExpressNo = null
                        m.toasts("保存成功✔，开始打印")
                        m.btn_scan.isEnabled = false
                        m.et_code.isEnabled = false
                        m.isTextChange = true
                        // 延时执行，因为输入框失去焦点会改变样式
                        m.mHandler.postDelayed(Runnable {
                            m.lin_focusMtl.setBackgroundResource(R.drawable.back_style_gray3)
                        },300)

                        // 保存后，打印快递单
//                        m.parent!!.setPrintData(m.listPrintDate) // 打印
                        if(m.isNULLS(m.listPrintDate[0].caiNiaoPrintData).length == 0) {
                            val list = JsonUtil.strToList(msgObj, ExpressNoData::class.java)
                            m.parent!!.cainiaoPrintData = null
                            m.parent!!.setPrintData(list) // 打印

                        } else {
                            m.parent!!.cainiaoPrintData = m.listPrintDate[0].caiNiaoPrintData
                            m.parent!!.setPrintData(null)
                        }

                        // 汇报最后一个工序
                        if(m.btTmp != null) {
                            m.run_saveProdReprot()
                        }
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    FIND_DEPT -> { // 查询默认锁库发货部门 进入
                        m.defaultDept = JsonUtil.strToObject(msgObj, Department::class.java)
                        m.tv_deptName.text = m.defaultDept!!.departmentName
                        // 查询金蝶仓库状态
                        if(m.stock != null) {
                            m.run_findStockStatus(m.stock!!.fitemId.toString())
                        }
                    }
                    UNFIND_DEPT -> { // 查询默认锁库发货部门  失败
                        if (m.isNULLS(errMsg).length == 0) errMsg = "请在PC端维护部门的（默认锁库部门）标识！3秒后自动关闭..."
                        m.toasts(errMsg)
                        m.mHandler.postDelayed(Runnable {
                            m.mContext!!.finish()
                        },3000)
                    }
                    FIND_LAST -> { // 查询当前汇报的工序是否为最后一道工序
                        m.btTmp = JsonUtil.strToObject(msgObj, BarCodeTable::class.java)
                        m.run_save()
                    }
                    UNFIND_LAST -> { // 查询当前汇报的工序是否为最后一道工序  失败
                        m.btTmp = null
                        m.run_save()
                    }
                    FIND_STOCK_STATUS -> { // 查询仓库状态
                        // 正常，不做处理
                    }
                    UNFIND_STOCK_STATUS -> { // 查询仓库状态  失败，就清空它
                        m.cb_remember.isChecked = false
                        m.stock = null
                        m.stockArea = null
                        m.storageRack = null
                        m.stockPos = null
                        m.tv_positionName.text = ""
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        when(m.smqFlag) {
                            '1'-> m.setFocusable(m.et_positionCode)
                            '2'-> {
                                if(m.et_code.isEnabled) {
                                    m.setFocusable(m.et_code)
                                }
                            }
                        }
                    }
                    SAOMA -> { // 扫码之后
                        when(m.smqFlag) {
                            '2' -> {
                                if(m.stock == null ) { // 如果扫描的是箱码，未选择位置，就提示
                                    Comm.showWarnDialog(m.mContext,"请先扫描或选择位置！")
                                    m.isTextChange = false
                                    return
                                }
                            }
                        }
                        // 执行查询方法
                        m.run_smDatas()
                    }
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.sal_ds_out_fragment1, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Sal_DS_OutStockMainActivity

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = Sal_DS_OutStockFragment1Adapter(mContext!!, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : Sal_DS_OutStockFragment1Adapter.MyCallBack {
            //            override fun onModify(entity: ICStockBillEntry, position: Int) {
//                EventBus.getDefault().post(EventBusEntity(31, entity))
//                // 滑动第二个页面
//                parent!!.viewPager!!.setCurrentItem(1, false)
//            }
            override fun onDelete(entity: ICStockBillEntry, position: Int) {
//                curPos = position
                val listPrintDate = ArrayList<ExpressNoData>()
                listPrintDate.add(entity.expressNoData)
                parent!!.setPrintData(listPrintDate) // 打印
//                run_removeEntry(entity.id)
            }
        })

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            val entry = checkDatas.get(pos)
            // 是否赠品，2000005代表是，2000006代表否
            if(entry.isComplimentary == 2000005 || entry.icItem.snManager.equals("Y")) return@OnItemClickListener

            curPos = pos
            showInputDialog("数量", entry.fqty.toString(), "0.0", RESULT_NUM)
        }
    }

    override fun initData() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(120, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(120, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }

        getUserInfo()
        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
        hideSoftInputMode(mContext, et_code)
        hideSoftInputMode(mContext, et_positionCode)

        showLocalStockGroup()

        run_findLockDeliDept()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
    }

    @OnClick(R.id.tv_positionName, R.id.btn_positionScan, R.id.btn_positionSel, R.id.btn_scan, R.id.btn_save, R.id.btn_upload, R.id.btn_clone)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.tv_positionName -> { // 点击位置名称
                smqFlag = '1'
                mHandler.sendEmptyMessageDelayed(SETFOCUS,200)
            }
            R.id.btn_positionSel -> { // 选择位置
                smqFlag = '1'
                val bundle = Bundle()
                bundle.putSerializable("stock", stock)
                bundle.putSerializable("stockArea", stockArea)
                bundle.putSerializable("storageRack", storageRack)
                bundle.putSerializable("stockPos", stockPos)
                showForResult(context, Stock_GroupDialogActivity::class.java, SEL_POSITION, bundle)
            }
            R.id.btn_positionScan -> { // 调用摄像头扫描（位置）
                smqFlag = '1'
                ScanUtil.startScan(mContext, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                smqFlag = '2'
                if(stock == null ) {
                    Comm.showWarnDialog(mContext,"请先扫描或选择位置！")
                    return
                }
                ScanUtil.startScan(mContext, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.btn_save -> { // 保存
                if(!checkSave()) return
                run_save()
            }
            R.id.btn_upload -> { // 保存
                if(!checkSave()) return
                run_save()
            }
            R.id.btn_clone -> { // 重置
                /*if (checkSaveHint()) {
                    val build = AlertDialog.Builder(mContext)
                    build.setIcon(R.drawable.caution)
                    build.setTitle("系统提示")
                    build.setMessage("您有未保存的数据，继续重置吗？")
                    build.setPositiveButton("是") { dialog, which -> reset() }
                    build.setNegativeButton("否", null)
                    build.setCancelable(false)
                    build.show()

                } else {
                    reset()
                }*/
                reset()
            }
        }
    }

    /**
     * 检查数据
     */
    fun checkSave() : Boolean {
        if (stock == null) {
            Comm.showWarnDialog(mContext, "请选择位置！")
            return false;
        }
        if(checkDatas.size == 0) {
            Comm.showWarnDialog(mContext, "请扫码物料！")
            return false
        }
//        if(!isFinish()) {
//            Comm.showWarnDialog(mContext, "订单数量未扫完！")
//            return false
//        }
        return true;
    }

    /**
     * 选择了物料没有点击保存，点击了重置，需要提示
     */
    fun checkSaveHint() : Boolean {
        if(checkDatas.size > 0) {
            return true
        }
        return false
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_positionCode -> setFocusable(et_positionCode)
                R.id.et_code -> setFocusable(et_code)
//                R.id.et_containerCode -> setFocusable(et_containerCode)
            }
        }
        et_positionCode!!.setOnClickListener(click)
        et_code!!.setOnClickListener(click)
//        et_containerCode!!.setOnClickListener(click)

        // 仓库---数据变化
        et_positionCode!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                if (!isTextChange) {
                    isTextChange = true
                    smqFlag = '1'
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 仓库---长按输入条码
        et_positionCode!!.setOnLongClickListener {
            smqFlag = '1'
            showInputDialog("输入条码", "", "none", WRITE_CODE)
            true
        }
        // 仓库---焦点改变
        et_positionCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_focusPosition.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusPosition != null) {
                    lin_focusPosition!!.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }

        // 物料---数据变化
        et_code!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                if (!isTextChange) {
                    isTextChange = true
                    smqFlag = '2'
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 物料---长按输入条码
        et_code!!.setOnLongClickListener {
            smqFlag = '2'
            showInputDialog("输入条码号", getValues(et_code), "none", WRITE_CODE)
            true
        }
        // 物料---焦点改变
        et_code.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_focusMtl.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusMtl != null) {
                    lin_focusMtl.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }

        // 位置长按显示仓库详细
        tv_positionName.setOnLongClickListener{
            val stockName = if(stock != null) stock!!.stockName else ""
            val stockAreaName = if(stockArea != null) "/"+stockArea!!.fname else ""
            val storageRackName = if(storageRack != null) "/"+storageRack!!.fnumber else ""
            val stockPosName = if(stockPos != null) "/"+stockPos!!.fname else ""
            Comm.showWarnDialog(mContext,stockName+stockAreaName+storageRackName+stockPosName)
            true
        }

        cb_remember.setOnCheckedChangeListener { buttonView, isChecked ->
            saveLocalStockGroup(isChecked)
        }
    }

    /**
     * 保存仓库信息到本地
     */
    private fun saveLocalStockGroup(isBool :Boolean) {
        val saveOther = getResStr(R.string.saveOther)
        if(isBool) { // 记住仓库信息
            // 对象保存到xml
            if(stock != null) saveObjectToXml(stock, STOCK_FLAG, saveOther)
            else spfRemove(STOCK_FLAG, saveOther)

            if(stockArea != null) saveObjectToXml(stockArea, STOCKAREA_FLAG, saveOther)
            else spfRemove(STOCKAREA_FLAG, saveOther)

            if(storageRack != null) saveObjectToXml(storageRack, STORAGERACK_FLAG, saveOther)
            else spfRemove(STORAGERACK_FLAG, saveOther)

            if(stockPos != null) saveObjectToXml(stockPos, STOCKPOS_FLAG, saveOther)
            else spfRemove(STOCKPOS_FLAG, saveOther)

        } else { // 清空仓库信息
            spfRemove(STOCK_FLAG, saveOther)
            spfRemove(STOCKAREA_FLAG, saveOther)
            spfRemove(STORAGERACK_FLAG, saveOther)
            spfRemove(STOCKPOS_FLAG, saveOther)
        }
    }

    /**
     *  显示本地默认仓库
     */
    private fun showLocalStockGroup() {
        // 显示记录的本地仓库
        val saveOther = getResStr(R.string.saveOther)
        val spfStock = spf(saveOther)
        if(spfStock.contains(STOCK_FLAG)) {
            stock = showObjectByXml(Stock::class.java, STOCK_FLAG, saveOther)
            tv_positionName.text = stock!!.stockName
            cb_remember.isChecked = true
            // 跳转到物料焦点
            smqFlag = '2'
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
        if(spfStock.contains(STOCKAREA_FLAG)) {
            stockArea = showObjectByXml(StockArea::class.java, STOCKAREA_FLAG, saveOther)
            tv_positionName.text = stockArea!!.fname
        }
        if(spfStock.contains(STORAGERACK_FLAG)) {
            storageRack = showObjectByXml(StorageRack::class.java, STORAGERACK_FLAG, saveOther)
            tv_positionName.text = storageRack!!.fnumber
        }
        if(spfStock.contains(STOCKPOS_FLAG)) {
            stockPos = showObjectByXml(StockPosition::class.java, STOCKPOS_FLAG, saveOther)
            tv_positionName.text = stockPos!!.stockPositionName
        }
    }

    /**
     * 重置数据
     */
    private fun reset() {
//        curPos = -1
        btn_scan.isEnabled = true
        et_code.isEnabled = true
        isTextChange = false
        salOutStock_ExpressNos.clear()
        checkDatas.clear()
        mAdapter!!.notifyDataSetChanged()
        dept = null
        tv_deptName.text = "部门："
        tv_needNum.text = "0"
        tv_finishNum.text = "0"

        salEntryId = 0
        footPadExpressNo = null
        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
        parent!!.isChange = false
        smqFlag = '2'
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    /**
     * 扫码销售订单的条码
     */
    private fun setICStockEntry_SalOrder(bt :BarCodeTable, list :List<SeOrderEntry>) {
        val icstockBill = ICStockBill()
        icstockBill.billType = "DS_XSCK" // 电商销售出库
        icstockBill.ftranType = 21
        icstockBill.frob = 1
        icstockBill.fselTranType = 81
        icstockBill.fcustId = list[0].seOrder.fcustid
//        icstockBill.fdeptId = list[0].seOrder.fdeptId
        icstockBill.fdeptId = dept!!.fitemID
        icstockBill.fempId = user!!.empId
        icstockBill.yewuMan = user!!.empName
        icstockBill.fsmanagerId = user!!.empId
        icstockBill.baoguanMan = user!!.empName
        icstockBill.fmanagerId = user!!.empId
        icstockBill.fuzheMan = user!!.empName
        icstockBill.ffmanagerId = user!!.empId
        icstockBill.yanshouMan = user!!.empName
        icstockBill.fbillerId = user!!.erpUserId
        icstockBill.createUserId = user!!.id
        icstockBill.createUserName = user!!.username

        printExpressNo = list[0].seOrder.printExpressNo
//        var isManProduct = false // 是否主产品
        list!!.forEachIndexed { index, it ->
            val entry = ICStockBillEntry()
            entry.icstockBill = icstockBill
            entry.icstockBillId = 0
            entry.fitemId = it.icItem.fitemid
//            entry.fentryId = it.fentryid
            if(it.lockStock != null) {      // 锁库仓库
                entry.fdcStockId = it.lockStock.fitemId
                entry.stockId_wms = it.lockStock.id
                entry.stock = it.lockStock
            } else {
                entry.fdcStockId = stock!!.fitemId
                entry.stockId_wms = stock!!.id
                entry.stock = stock
            }
            if(it.lockStockPos != null) {   // 锁库库位
                entry.fdcSPId = it.lockStockPos.fitemId
                entry.stockPosId_wms = it.lockStockPos.id
                entry.stockPos = it.lockStockPos

            } else {
                if(stockPos != null) {
                    entry.fdcSPId = stockPos!!.fitemId
                    entry.stockPosId_wms = stockPos!!.id
                    entry.stockPos = stockPos
                }
            }

            if(it.isComplimentary == 2000005) { // 是否赠品，2000005代表是，2000006代表否
                entry.fqty = it.fqty
            }
//            entry.fqty = it.useableQty
            entry.fprice = it.fprice
            entry.funitId = it.funitid
            entry.fsourceInterId = it.finterid
            entry.fsourceEntryId = it.fentryid
            entry.fsourceBillNo = it.seOrder.fbillno
            entry.fsourceQty = it.fqty
            entry.fsourceTranType = 81
            entry.forderInterId = it.finterid
            entry.forderEntryId = it.fentryid
            entry.forderBillNo = it.seOrder.fbillno
            entry.fdetailId = it.fdetailid

            entry.icItem = it.icItem
            entry.unit = it.icItem.unit

            entry.remark = ""
            entry.isComplimentary = it.isComplimentary
            entry.icItemType = it.icItemType
            if(it.isFocus > 0) { // 扫码对焦的行
                if(isNULLS(it.icItemClassesName).equals("脚垫")) {
                    salEntryId = it.fentryid
                }
                entry.fqty = 1.0
                // 记录条码
                if(it.icItem.batchManager.equals("Y")) { // 启用批次号
                    setBatchCode(entry, 1.0, bt.batchCode)

                } else if(it.icItem.snManager.equals("Y")) { // 启用序列号
                    setSnCode(entry, bt.snCode)

                } else { // 未启用
                    unStartBatchOrSnCode(entry, 1.0)
                }

                curPos = index
//                isManProduct = if(it.icItemType == 2000007) true else false

            } else {
                if(it.icstockBillEntry_Barcodes != null && it.icstockBillEntry_Barcodes.size > 0) {
                    it.icstockBillEntry_Barcodes.forEach {
                        it.createUserName == user!!.username
                    }
                    entry.icstockBillEntry_Barcodes.addAll(it.icstockBillEntry_Barcodes)
                    entry.strBarcode = setStrBarcode(entry.icstockBillEntry_Barcodes)
                }
            }

            checkDatas.add(entry)
        }
        // 是主副产品，且数量为0的放到最后面，不显示出来
        checkDatasSort()

        // 加载打印的数据
//        if(isManProduct) {
            run_findPrintData(checkDatas[0].fsourceInterId, checkDatas[0].fsourceBillNo, checkDatas[0].icItem.fnumber)
//        }

        // 每扫描一次就保存2020-08-24修改
        /*mHandler.postDelayed(Runnable {
            run_save()
        },300)*/
    }

    /**
     *  列表排序，数量等于0，就排到最后不显示
     */
    private fun checkDatasSort() {
        // 是主产品且数量为0的放到最后面
        val listDatasTmp = ArrayList<ICStockBillEntry>()
        val listDatasTmp2 = ArrayList<ICStockBillEntry>()
        checkDatas.forEach {
            if((it.icItemType == 2000007 || it.icItemType == 2000008) && it.fqty == 0.0) {
                listDatasTmp2.add(it)
            } else {
                listDatasTmp.add(it)
            }
        }
        checkDatas.clear()
        checkDatas.addAll(listDatasTmp)
        checkDatas.addAll(listDatasTmp2)
    }

    /**
     * 扫描每一行的处理
     */
    private fun setICStockBill_Row(bt :BarCodeTable) {
        // 判断条码是否存在（启用批次，序列号）
        val listOrder = JsonUtil.stringToList(bt.relationObj, SeOrderEntry::class.java) as List<SeOrderEntry>
//        if(checkDatas.size == 0) {
            setICStockEntry_SalOrder(bt, listOrder)

        /*} else {
            var isExist = false // 是否匹配

            checkDatas.forEachIndexed { index, it ->
                if (it.icstockBillEntry_Barcodes.size > 0 && (it.icItem.batchManager.equals("Y") || it.icItem.snManager.equals("Y"))) {
                    it.icstockBillEntry_Barcodes.forEach {
                        if (getValues(et_code).length > 0 && getValues(et_code) == it.barcode) {
                            Comm.showWarnDialog(mContext,"条码已使用！")
                            return
                        }
                    }
                }

                if(it.fsourceInterId == listOrder[0].finterid && it.fsourceEntryId == listOrder[0].fentryid) {
                    isExist = true
                    if(it.fqty >= it.fsourceQty) {
                        Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，数量已扫完！")
                        return
                    }
                    val addVal = BigdecimalUtil.add(it.fqty, 1.0)
                    it.fqty = addVal

                    // 记录条码
                    if(it.icItem.batchManager.equals("Y")) { // 启用批次号
                        setBatchCode(it, 1.0, bt.batchCode)

                    } else if(it.icItem.snManager.equals("Y")) { // 启用序列号
                        setSnCode(it, bt.snCode)

                    } else { // 未启用
                        unStartBatchOrSnCode(it, 1.0)
                    }

                    // 重新排序，按照分录大小排序ASC
                    checkDatas.sortBy { it.fsourceEntryId }
                    // 是主副产品，且数量为0的放到最后面
                    checkDatasSort()

                    if(listOrder[0].icItemType == 2000007 ) { // 产品类型 2000007：主产品，2000008：副产品，2000009：原材料，2000010：其它
                        if(isNULLS(listOrder[0].icItemClassesName).equals("脚垫")) {
                            salEntryId = listOrder[0].fentryid
                        }
                        curPos = index
                        run_findPrintData(checkDatas[0].fsourceInterId, checkDatas[0].fsourceBillNo)
                    }
                }
            }
            if(!isExist) {
                Comm.showWarnDialog(mContext, "扫码的条码与订单不匹配！")
                return
            }
        }*/
        countNum()
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     *  扫码之后    物料启用批次
     */
    fun setBatchCode(entry :ICStockBillEntry, fqty :Double, batchCode :String) {
        val entryBarcode = ICStockBillEntry_Barcode()
        entryBarcode.parentId = entry.id
        entryBarcode.barcode = getValues(et_code)
        entryBarcode.batchCode = batchCode
        entryBarcode.snCode = ""
        entryBarcode.fqty = fqty
        entryBarcode.isUniqueness = 'Y'
        entryBarcode.againUse = 0
        entryBarcode.createUserName = user!!.username
        entryBarcode.billType = "DS_XSCK"

        entry.icstockBillEntry_Barcodes.add(entryBarcode)
        entry.strBarcode = setStrBarcode(entry.icstockBillEntry_Barcodes)
    }

    /**
     *  扫码之后    物料启用序列号
     */
    fun setSnCode(entry :ICStockBillEntry, snCode :String) {
        val entryBarcode = ICStockBillEntry_Barcode()
        entryBarcode.parentId = entry.id
        entryBarcode.barcode = getValues(et_code)
        entryBarcode.batchCode = ""
        entryBarcode.snCode = snCode
        entryBarcode.fqty = 1.0
        entryBarcode.isUniqueness = 'Y'
        entryBarcode.againUse = 0
        entryBarcode.createUserName = user!!.username
        entryBarcode.billType = "DS_XSCK"

        entry.icstockBillEntry_Barcodes.add(entryBarcode)
        entry.strBarcode = setStrBarcode(entry.icstockBillEntry_Barcodes)
    }

    /**
     *  扫码之后    物料未启用
     */
    fun unStartBatchOrSnCode(entry :ICStockBillEntry, fqty :Double) {
        val entryBarcode = ICStockBillEntry_Barcode()
        entryBarcode.parentId = entry.id
        entryBarcode.barcode = getValues(et_code)
        entryBarcode.batchCode = ""
        entryBarcode.snCode = ""
        entryBarcode.fqty = fqty
        entryBarcode.isUniqueness = 'N'
        entryBarcode.againUse = 0
        entryBarcode.createUserName = user!!.username
        entryBarcode.billType = "DS_XSCK"

        entry.icstockBillEntry_Barcodes.add(entryBarcode)
        entry.strBarcode = setStrBarcode(entry.icstockBillEntry_Barcodes)
    }

    /**
     * 设置拼接strBarcode的值
     */
    private fun setStrBarcode(list :List<ICStockBillEntry_Barcode>) :String {
        val sb = StringBuffer()
        list.forEach {
            if(sb.indexOf(it.barcode) == -1 && sb.length == 0) sb.append(it.barcode)
            else if(sb.indexOf(it.barcode) == -1 && sb.length > 0) sb.append("，"+it.barcode)
        }
        return sb.toString()
    }

    /**
     * 添加到（条码、快递单、订单号）记录表
     */
    private fun setBarcode_ExpressNo (pos :Int, expressNo :String) {
        val m = SalOutStock_ExpressNo()
        m.salOrderNo = if(pos == -1) checkDatas[0].fsourceBillNo else checkDatas[pos].fsourceBillNo
        m.salOrderEntry = if(pos == -1) 0 else checkDatas[pos].fsourceEntryId
        m.barcode = if(pos == -1) "" else getValues(et_code)
        m.expressNo = expressNo
        m.createUserId = user!!.id
        salOutStock_ExpressNos.add(m)
    }

    /**
     * 统计数量
     */
    private fun countNum() {
        var needNum = 0.0
        var finishNum = 0.0
        checkDatas.forEach {
            needNum = BigdecimalUtil.add(needNum, it.fsourceQty)
            finishNum = BigdecimalUtil.add(finishNum, it.fqty)
        }
        tv_needNum.setText(df.format(needNum))
        tv_finishNum.setText(df.format(finishNum))
    }

    /**
     * 判断是否扫完数
     */
    private fun isFinish(): Boolean {
        checkDatas.forEach {
            if(it.fsourceQty > it.fqty) {
                return false
            }
        }
        return true
    }

    fun getICStockBillEntry(icEntry: ICStockBillEntry) {
//        icStockBillEntry.id = icEntry.id
//        icStockBillEntry.icstockBillId = icEntry.icstockBillId
//        icStockBillEntry.finterId = icEntry.finterId
//        icStockBillEntry.fitemId = icEntry.fitemId
//        icStockBillEntry.fentryId = icEntry.fentryId
//        icStockBillEntry.fdcStockId = icEntry.fdcStockId
//        icStockBillEntry.fdcSPId = icEntry.fdcSPId
//        icStockBillEntry.fqty = icEntry.fqty
//        icStockBillEntry.fprice = icEntry.fprice
//        icStockBillEntry.funitId = icEntry.funitId
//        icStockBillEntry.remark = icEntry.remark

    }

    // 宿主调用的
    fun refreshOnActivityResult() {
        mHandler.sendEmptyMessage(SETFOCUS)
    }

    // 预约调用的
    fun appointment() {
        if(checkDatas.size == 0) {
            Comm.showWarnDialog(mContext,"请先扫描条码，然后预约！")
            return
        }
        showForResult(context, Logistics_DialogActivity::class.java, SEL_LOGISTICS, null)
//        showInputDialog("预约个数", "", "1", RESULT_YUYUE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SEL_POSITION -> {// 仓库	返回
                    stock = null
                    stockArea = null
                    storageRack = null
                    stockPos = null
                    stock = data!!.getSerializableExtra("stock") as Stock
                    if (data!!.getSerializableExtra("stockArea") != null) {
                        stockArea = data!!.getSerializableExtra("stockArea") as StockArea
                    }
                    if (data!!.getSerializableExtra("storageRack") != null) {
                        storageRack = data!!.getSerializableExtra("storageRack") as StorageRack
                    }
                    if (data!!.getSerializableExtra("stockPos") != null) {
                        stockPos = data!!.getSerializableExtra("stockPos") as StockPosition
                    }
                    getStockGroup(null)
                }
                SEL_LOGISTICS -> {// 物流公司	返回
                    logistics = data!!.getSerializableExtra("obj") as Logistics
                    showInputDialog("预约个数", "1", "0", RESULT_YUYUE)
                }
                RESULT_NUM -> { // 数量	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        checkDatas.get(curPos).fqty = num

                        countNum()
                        mAdapter!!.notifyDataSetChanged()
                    }
                }
                RESULT_SALORDER_ORDER -> { // 选择单据   返回
//                    if(icStockBillEntry.fsourceTranType == 81) {
//                        val list = data!!.getSerializableExtra("obj") as List<SeOrderEntry>
//                        setICStockEntry_SeOrder(list)
//                    } else if(icStockBillEntry.fsourceTranType == 83){
//                        val list = data!!.getSerializableExtra("obj") as List<SeoutStockEntry>
//                        setICStockEntry_SeOutStock(list)
//                    }
                }
                WRITE_CODE -> {// 输入条码  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        et_code!!.setText(value.toUpperCase())
                    }
                }
                RESULT_YUYUE -> { // 预约数量返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        var num = parseInt(value)
                        if (num == 0) {
                            num = 1
                        }
                        run_saoOutStock_appointment(checkDatas[0].fsourceBillNo, num)
                    }
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS,300)
    }

    /**
     * 调用华为扫码接口，返回的值
     */
    fun getScanData(barcode :String) {
        when (smqFlag) {
            '1' -> setTexts(et_positionCode, barcode)
            '2' -> setTexts(et_code, barcode)
        }
    }

    /**
     * 得到仓库组
     */
    fun getStockGroup(msgObj : String?) {
        if(msgObj != null) {
            stock = null
            stockArea = null
            storageRack = null
            stockPos = null

            var caseId:Int = 0
            if(msgObj.indexOf("Stock_CaseId=1") > -1) {
                caseId = 1
            } else if(msgObj.indexOf("StockArea_CaseId=2") > -1) {
                caseId = 2
            } else if(msgObj.indexOf("StorageRack_CaseId=3") > -1) {
                caseId = 3
            } else if(msgObj.indexOf("StockPosition_CaseId=4") > -1) {
                caseId = 4
            }

            when(caseId) {
                1 -> {
                    stock = JsonUtil.strToObject(msgObj, Stock::class.java)
                    tv_positionName.text = stock!!.stockName
                }
                2 -> {
                    stockArea = JsonUtil.strToObject(msgObj, StockArea::class.java)
                    tv_positionName.text = stockArea!!.fname
                    if(stockArea!!.stock != null) stock = stockArea!!.stock
                }
                3 -> {
                    storageRack = JsonUtil.strToObject(msgObj, StorageRack::class.java)
                    tv_positionName.text = storageRack!!.fnumber
                    if(storageRack!!.stock != null) stock = storageRack!!.stock
                    if(storageRack!!.stockArea != null)  stockArea = storageRack!!.stockArea
                }
                4 -> {
                    stockPos = JsonUtil.strToObject(msgObj, StockPosition::class.java)
                    tv_positionName.text = stockPos!!.stockPositionName
                    if(stockPos!!.stock != null) stock = stockPos!!.stock
                    if(stockPos!!.stockArea != null)  stockArea = stockPos!!.stockArea
                    if(stockPos!!.storageRack != null)  storageRack = stockPos!!.storageRack
                }
            }
        }

        if(stock != null ) {
            tv_positionName.text = stock!!.stockName
        }
        if(stockArea != null ) {
            tv_positionName.text = stockArea!!.fname
        }
        if(storageRack != null ) {
            tv_positionName.text = storageRack!!.fnumber
        }
        if(stockPos != null ) {
            tv_positionName.text = stockPos!!.stockPositionName
        }
        saveLocalStockGroup(cb_remember.isChecked)

        if(stock != null) {
            // 自动跳到物料焦点
            smqFlag = '2'
            mHandler.sendEmptyMessage(SETFOCUS)
        }
    }

    /**
     * 扫码查询对应的方法
     */
    private fun run_smDatas() {
        showLoadDialog("加载中...", false)
        var mUrl:String? = null
        var barcode:String? = null
        var icstockBillId = ""
        var billType = "" // 单据类型
        var isWhole = "" // 是否查询整张销售订单
        when(smqFlag) {
            '1' -> {
                mUrl = getURL("stockPosition/findBarcodeGroup")
                barcode = getValues(et_positionCode)
            }
            '2' -> {
                mUrl = getURL("seOrder/findBarcode")
                barcode = getValues(et_code)
                isWhole = if(checkDatas.size == 0) "1" else ""
//                icstockBillId = parent!!.fragment1.icStockBill.id.toString()
//                billType = parent!!.fragment1.icStockBill.billType

            }
        }
        val formBody = FormBody.Builder()
                .add("barcode", barcode)
                .add("icstockBillId", icstockBillId)
                .add("billType", billType)
                .add("isWhole", isWhole)
                .add("defaultDeptId", defaultDept!!.fitemID.toString())
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_smDatas --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC1, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC1, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 保存
     */
    private fun run_save() {
        val listResult = java.util.ArrayList<ICStockBillEntry>()
        checkDatas.forEach {
            if(it.fqty > 0) listResult.add(it)
        }
        if(listResult.size == 0) {
            Comm.showWarnDialog(mContext,"请至少一行输入数量！")
            return
        }

        showLoadDialog("保存中...", false)
        var mUrl = getURL("stockBill_WMS/DS_SalOutStock")
        var mJson = JsonUtil.objectToString(listResult)
        var mJson2 = JsonUtil.objectToString(salOutStock_ExpressNos)
        val formBody = FormBody.Builder()
                .add("strJson", mJson)
                .add("strJson2", mJson2)
                .add("footPadExpressNo", isNULLS(footPadExpressNo))
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSAVE)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSAVE, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SAVE, result)
                LogUtil.e("run_save --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 查询库存
     */
    private fun run_findInventoryQty() {
        showLoadDialog("加载中...")
        val mUrl = getURL("icInventory/findInventoryQty")
        val formBody = FormBody.Builder()
//                .add("fStockID", icStockBillEntry.fdcStockId.toString())
//                .add("fStockPlaceID",  icStockBillEntry.fdcSPId.toString())
//                .add("mtlId", icStockBillEntry.fitemId.toString())
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC2)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_findListByParamWms --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC2, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC2, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 查询打印数据
     */
    private fun run_findPrintData(salOrderId :Int,salOrderNo :String, mtlNumber :String) {
        showLoadDialog("准备打印...", false)
        val mUrl = getURL("appPrint/printExpressNo")
        val formBody = FormBody.Builder()
                .add("salOrderId", salOrderId.toString())
                .add("so_id", salOrderNo)
                .add("printExpressNo", isNULLS(printExpressNo))
                .add("expressNotIn", "1") // 已经绑定的快递单，不显示
                .add("curSmType", if(salEntryId > 0) "JD" else "WD") // 当前扫描的条码类型：脚垫 = JD，尾垫 = WD
                .add("mtlNumber", mtlNumber) // 通过物料代码查询对应的商品类别名称
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC3)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_findPrintData --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC3, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC3, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 查询预约的数据
     */
    fun run_saoOutStock_appointment(salOrderNo :String, num :Int) {
        showLoadDialog("准备打印...", false)
        val mUrl = getURL("appPrint/saoOutStock_appointment")
        var strLogistics = JsonUtil.objectToString(logistics!!)
        val formBody = FormBody.Builder()
                .add("so_id", salOrderNo)
                .add("num", num.toString())
                .add("strLogistics", strLogistics)
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC4)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_saoOutStock_appointment --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC4, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC4, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 查询默认锁库发货默认部门
     */
    fun run_findLockDeliDept() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("department/findLockDeliDept")
        val formBody = FormBody.Builder()
                .add("isLockDeliDept", "1")
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNFIND_DEPT)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_findLockDeliDept --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNFIND_DEPT, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(FIND_DEPT, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     *  销售出库的时候，如果报工的是最后一个工序，在销售出库自动汇报
     */
    fun run_findLastProcedure() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("prodOrder/findLastProcedure")
        val formBody = FormBody.Builder()
                .add("barcode", getValues(et_code))
                .add("userId", user!!.id.toString())
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNFIND_LAST)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_findLastProcedure --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNFIND_LAST, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(FIND_LAST, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 汇报最后一个工序
     */
    private fun run_saveProdReprot() {
        val procedure = btTmp!!.listProcedure[0]
        val prodOrder = JsonUtil.stringToObject(btTmp!!.relationObj, ProdOrder::class.java)
        val pr = ProdReport()
        pr.barcodeTableId = btTmp!!.id
        pr.userId = user!!.id
        pr.procedureId = procedure.id
        pr.fqty = 1.0
        pr.deptId = prodOrder.workShopId
        pr.icItemId = prodOrder.icItemId
        pr.icmoFinterId = prodOrder.prodId
        pr.icmoFbillNo = prodOrder.prodNo
        pr.processflowId = procedure.processflowId
        pr.reportType = 'A'

        showLoadDialog("保存中...", false)
        var mUrl = getURL("prodReport/add")
        var mJson = JsonUtil.objectToString(pr)
        val formBody = FormBody.Builder()
                .add("strJson", mJson)
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSAVE_LAST)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSAVE_LAST, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SAVE_LAST, result)
                LogUtil.e("run_saveProdReprot --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 查询金蝶仓库的状态
     */
    private fun run_findStockStatus(strStockId :String) {
        var mUrl = getURL("stock/findStockStatus")
        val formBody = FormBody.Builder()
                .add("strStockId", strStockId)
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNFIND_STOCK_STATUS)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNFIND_STOCK_STATUS, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(FIND_STOCK_STATUS, result)
                LogUtil.e("run_findStockStatus --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 得到用户对象
     */
    private fun getUserInfo() {
        if (user == null) user = showUserByXml()
    }

    override fun onDestroyView() {
        closeHandler(mHandler)
        mBinder!!.unbind()
        super.onDestroyView()
    }
}