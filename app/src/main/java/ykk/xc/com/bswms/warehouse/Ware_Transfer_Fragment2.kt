package ykk.xc.com.bswms.warehouse

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import kotlinx.android.synthetic.main.ware_transfer_fragment2.*
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.basics.MoreBatchInputDialog
import ykk.xc.com.bswms.basics.Mtl_DialogActivity
import ykk.xc.com.bswms.basics.Stock_GroupDialogActivity
import ykk.xc.com.bswms.bean.*
import ykk.xc.com.bswms.bean.k3Bean.Inventory_K3
import ykk.xc.com.bswms.bean.k3Bean.ICItem
import ykk.xc.com.bswms.bean.k3Bean.MeasureUnit
import ykk.xc.com.bswms.comm.BaseFragment
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.BigdecimalUtil
import ykk.xc.com.bswms.util.JsonUtil
import ykk.xc.com.bswms.util.LogUtil
import java.io.IOException
import java.io.Serializable
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 * 日期：2020-07-22 09:50
 * 描述：自由调拨---添加明细
 * 作者：ykk
 */
class Ware_Transfer_Fragment2 : BaseFragment() {

    companion object {
        private val SEL_POSITION = 61
        private val SEL_POSITION2 = 62
        private val SEL_MTL = 63
        private val SEL_UNIT = 64
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SUCC2 = 201
        private val UNSUCC2 = 501
        private val SAVE = 202
        private val UNSAVE = 502
        private val FIND_MINQTY = 203
        private val UNFIND_MINQTY = 503
        private val FIND_STOCK_STATUS = 208
        private val UNFIND_STOCK_STATUS = 508

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val RESULT_PRICE = 3
        private val RESULT_NUM = 4
        private val RESULT_NUM2 = 5
        private val RESULT_BATCH = 6
        private val RESULT_REMAREK = 7
        private val WRITE_CODE = 8
        private val RESULT_PUR_ORDER = 9
        private val SM_RESULT_NUM = 10
        // 调入仓库组
        private val STOCK_FLAG = "ZYDB_stock"
        private val STOCKAREA_FLAG = "ZYDB_stockArea"
        private val STORAGERACK_FLAG = "ZYDB_storageRack"
        private val STOCKPOS_FLAG = "ZYDB_stockPos"
        // 调出仓库组
        private val STOCK2_FLAG = "ZYDB_stock2"
        private val STOCKAREA2_FLAG = "ZYDB_stockArea2"
        private val STORAGERACK2_FLAG = "ZYDB_storageRack2"
        private val STOCKPOS2_FLAG = "ZYDB_stockPos2"
    }
    private val context = this
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var stock:Stock? = null
    private var stockArea:StockArea? = null
    private var storageRack:StorageRack? = null
    private var stockPos:StockPosition? = null
    private var stock2:Stock? = null
    private var stockArea2:StockArea? = null
    private var storageRack2:StorageRack? = null
    private var stockPos2:StockPosition? = null
    private var mContext: Activity? = null
    private val df = DecimalFormat("#.######")
    private var parent: Ware_Transfer_MainActivity? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var timesTamp:String? = null // 时间戳
    var icStockBillEntry = ICStockBillEntry()
    private var smICStockBillEntry:ICStockBillEntry? = null // 扫码返回的对象
    private var autoICStockBillEntry:ICStockBillEntry? = null // 用于自动保存记录的对象
    private var smICStockBillEntry_Barcodes = ArrayList<ICStockBillEntry_Barcode>() // 扫码返回的对象
    private var smqFlag = '1' // 扫描类型1：调出位置扫描，2：调入位置扫描，3：物料扫描
    private var minQty = 0.0 // 投料调拨单的最小领用数
    private var isNumTextChanged = true // 主计量单位数量改变事件标识
    private var isNum2TextChanged = true // 辅计量单位数量改变事件标识
    private var unitConvertRatio = 0.0 // 单位转换率


    // 消息处理
    private val mHandler = MyHandler(this)
    private class MyHandler(activity: Ware_Transfer_Fragment2) : Handler() {
        private val mActivity: WeakReference<Ware_Transfer_Fragment2>

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
                            '1'-> { // 调出仓库位置
                                m.resetStockGroup2()
                                m.getStockGroup2(msgObj, true)
                            }
                            '2'-> { // 调入位置
                                m.resetStockGroup()
                                m.getStockGroup(msgObj)
                            }
                            '3','4'-> { // 物料
                                m.getStockGroup2(null, false)
                                m.getStockGroup(null)

                                val icEntry = JsonUtil.strToObject(msgObj, ICStockBillEntry::class.java)
                                if(m.getValues(m.tv_mtlName).length > 0 && m.smICStockBillEntry != null && m.smICStockBillEntry!!.fitemId != icEntry.fitemId) {
                                    if(!m.checkSave()) return
                                    m.icStockBillEntry.icstockBillId = m.parent!!.fragment1.icStockBill.id

                                    m.autoICStockBillEntry = icEntry // 加到自动保存对象
                                    m.run_save(null);
//                                    Comm.showWarnDialog(m.mContext,"请先保存当前数据！")
                                    return
                                }
                                m.tv_num2.text = ""
                                m.getMaterial(icEntry)
                            }
                        }
                        m.isTextChange = false
                    }
                    UNSUCC1 -> { // 扫码失败
                        m.isTextChange = false
                        when(m.smqFlag) {
                            '1' -> m.tv_outPositionName.text = ""
                            '2' -> m.tv_inPositionName.text = ""
                            '3' -> m.tv_icItemName.text = ""
                        }
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC2 -> { // 查询库存 进入
                        val list = JsonUtil.strToList(msgObj, Inventory_K3::class.java)
                        m.tv_stockQty.text = Html.fromHtml("即时库存：<font color='#6a5acd'>"+m.df.format(list[0].fqty)+"</font>")
                    }
                    UNSUCC2 -> { // 查询库存  失败
                        m.tv_stockQty.text = "即时库存：0"
                    }
                    SAVE -> { // 保存成功 进入
                        // 保存了分录，供应商就不能修改
//                        m.setEnables(m.parent!!.fragment1.tv_suppSel, R.drawable.back_style_gray2a,false)
                        m.parent!!.fragment1.ppBomTransferEntryList = null
                        EventBus.getDefault().post(EventBusEntity(21)) // 发送指令到fragment3，告其刷新
                        m.reset()
//                        m.toasts("保存成功✔")
                        // 如果有自动保存的对象，保存后就显示下一个
                        if(m.autoICStockBillEntry != null) {
                            m.toasts("自动保存成功✔")
                            m.getMaterial(m.autoICStockBillEntry!!)
                            m.autoICStockBillEntry = null

                        } else {
                            m.toasts("保存成功✔")
                        }
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    FIND_MINQTY -> { // 查询最小领用数 进入
                        val strMinQty = JsonUtil.strToString(msgObj)
                        m.minQty = m.parseDouble(strMinQty)
                    }
                    UNFIND_MINQTY -> { // 查询最小领用数 失败
                        m.minQty = 0.0
                    }
                    FIND_STOCK_STATUS -> { // 查询仓库状态
                        val list = JsonUtil.strToList(msgObj, Int::class.java)
                        if(list.size == 1 ) {
                            // 如果调入仓库存在，调出仓库就清空，否则反之
                            if(m.stock != null && list[0] == m.stock!!.fitemId) {
                                m.cb_outRemember.isChecked = false
                                m.stock2 = null
                                m.stockArea2 = null
                                m.storageRack2 = null
                                m.stockPos2 = null
                                m.tv_outPositionName.text = ""
                            } else {
                                m.cb_inRemember.isChecked = false
                                m.stock = null
                                m.stockArea = null
                                m.storageRack = null
                                m.stockPos = null
                                m.tv_inPositionName.text = ""
                            }
                        }
                    }
                    UNFIND_STOCK_STATUS -> { // 查询仓库状态  失败，就清空它
                        m.cb_inRemember.isChecked = false
                        m.cb_outRemember.isChecked = false
                        m.stock = null
                        m.stockArea = null
                        m.storageRack = null
                        m.stockPos = null
                        m.stock2 = null
                        m.stockArea2 = null
                        m.storageRack2 = null
                        m.stockPos2 = null
                        m.tv_inPositionName.text = ""
                        m.tv_outPositionName.text = ""
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        when(m.smqFlag) {
                            '1'-> m.setFocusable(m.et_outPositionCode)
                            '2'-> m.setFocusable(m.et_inPositionCode)
                            '3'-> m.setFocusable(m.et_code)
                        }
                    }
                    SAOMA -> { // 扫码之后
                        // 执行查询方法
                        m.run_smDatas(0)
                    }
                }
            }
        }
    }

    @Subscribe
    fun onEventBus(entity: EventBusEntity) {
        when (entity.caseId) {
            11 -> { // 接收第一个页面发来的指令
                reset()
            }
            31 -> { // 接收第三个页面发来的指令
                var icEntry = entity.obj as ICStockBillEntry
                btn_save.text = "保存"
                smICStockBillEntry_Barcodes.clear()
                smICStockBillEntry_Barcodes.addAll(icEntry.icstockBillEntry_Barcodes)
                getICStockBillEntry(icEntry)
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.ware_transfer_fragment2, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Ware_Transfer_MainActivity
        EventBus.getDefault().register(this) // 注册EventBus
        // 确人员进入
        if(parent!!.isUpload) {
            btn_outPositionScan.visibility = View.GONE
            et_outPositionCode.isEnabled = false
            tv_outPositionName.isEnabled = false
            btn_outPositionSel.visibility = View.GONE
            cb_outRemember.isEnabled = false
            btn_inPositionScan.visibility = View.GONE
            et_inPositionCode.isEnabled = false
            tv_inPositionName.isEnabled = false
            btn_inPositionSel.visibility = View.GONE
            cb_inRemember.isEnabled = false
            btn_scan.visibility = View.GONE
            et_code.isEnabled = false
            tv_icItemName.isEnabled = false
            btn_mtlSel.visibility = View.GONE
            btn_scan.isEnabled = false
            lin_save.visibility = View.GONE
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
        hideSoftInputMode(mContext, et_outPositionCode)
        hideSoftInputMode(mContext, et_inPositionCode)
        hideSoftInputMode(mContext, et_code)

        parent!!.fragment1.icStockBill.fselTranType = 0
        icStockBillEntry.fsourceTranType = 0

        // 显示记录的本地仓库
        val saveOther = getResStr(R.string.saveOther)
        val spfStock = spf(saveOther)
        // 显示调出仓库---------------------
        if(spfStock.contains(STOCK2_FLAG)) {
            stock2 = showObjectByXml(Stock::class.java, STOCK2_FLAG, saveOther)
            tv_outPositionName.text = stock2!!.stockName
            cb_outRemember.isChecked = true
            // 跳转到物料焦点
            smqFlag = '2'
            mHandler.sendEmptyMessage(SETFOCUS)
        }
        if(spfStock.contains(STOCKAREA2_FLAG)) {
            stockArea2 = showObjectByXml(StockArea::class.java, STOCKAREA2_FLAG, saveOther)
            tv_outPositionName.text = stockArea2!!.fname
        }
        if(spfStock.contains(STORAGERACK2_FLAG)) {
            storageRack2 = showObjectByXml(StorageRack::class.java, STORAGERACK2_FLAG, saveOther)
            tv_outPositionName.text = storageRack2!!.fnumber
        }
        if(spfStock.contains(STOCKPOS2_FLAG)) {
            stockPos2 = showObjectByXml(StockPosition::class.java, STOCKPOS2_FLAG, saveOther)
            tv_outPositionName.text = stockPos2!!.stockPositionName
        }
        // 显示调入仓库---------------------
        if(spfStock.contains(STOCK_FLAG)) {
            stock = showObjectByXml(Stock::class.java, STOCK_FLAG, saveOther)
            tv_inPositionName.text = stock!!.stockName
            cb_inRemember.isChecked = true
            // 跳转到物料焦点
            smqFlag = '3'
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
        if(spfStock.contains(STOCKAREA_FLAG)) {
            stockArea = showObjectByXml(StockArea::class.java, STOCKAREA_FLAG, saveOther)
            tv_inPositionName.text = stockArea!!.fname
        }
        if(spfStock.contains(STORAGERACK_FLAG)) {
            storageRack = showObjectByXml(StorageRack::class.java, STORAGERACK_FLAG, saveOther)
            tv_inPositionName.text = storageRack!!.fnumber
        }
        if(spfStock.contains(STOCKPOS_FLAG)) {
            stockPos = showObjectByXml(StockPosition::class.java, STOCKPOS_FLAG, saveOther)
            tv_inPositionName.text = stockPos!!.stockPositionName
        }
        // 查询金蝶仓库状态
        val strStockId = StringBuffer()
        if(stock != null) {
            strStockId.append(stock!!.fitemId.toString())
        }
        if(stock2 != null) {
            strStockId.append((if(strStockId.length > 0) "," else "" ) + stock2!!.fitemId.toString())
        }
        if(strStockId.length > 0) {
            run_findStockStatus(strStockId.toString())
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if(parent!!.fragment1.ppBomTransferEntryList != null) {
                // 执行保存功能
                setICStockEntry_PPBomTransfer(parent!!.fragment1.ppBomTransferEntryList)
            }
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
    }

    @OnClick(R.id.btn_scan, R.id.btn_mtlSel, R.id.btn_outPositionScan, R.id.btn_outPositionSel, R.id.btn_inPositionScan, R.id.btn_inPositionSel, R.id.tv_num, R.id.tv_num2, R.id.tv_batchNo,
             R.id.tv_remark, R.id.btn_save, R.id.btn_clone, R.id.tv_outPositionName, R.id.tv_icItemName)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_outPositionSel -> { // 选择调出仓库
                smqFlag = '1'
                val bundle = Bundle()
                bundle.putSerializable("stock", stock2)
                bundle.putSerializable("stockArea", stockArea2)
                bundle.putSerializable("storageRack", storageRack2)
                bundle.putSerializable("stockPos", stockPos2)
                showForResult(context, Stock_GroupDialogActivity::class.java, SEL_POSITION2, bundle)
            }
            R.id.btn_inPositionSel -> { // 选择调入仓库
                smqFlag = '2'
                val bundle = Bundle()
                bundle.putSerializable("stock", stock)
                bundle.putSerializable("stockArea", stockArea)
                bundle.putSerializable("storageRack", storageRack)
                bundle.putSerializable("stockPos", stockPos)
                showForResult(context, Stock_GroupDialogActivity::class.java, SEL_POSITION, bundle)
            }
            R.id.btn_mtlSel -> { // 选择物料
                smqFlag = '3'
                val bundle = Bundle()
                showForResult(Mtl_DialogActivity::class.java, SEL_MTL, bundle)
            }
            R.id.btn_outPositionScan -> { // 调用摄像头扫描（调出位置）
                smqFlag = '1'
                ScanUtil.startScan(mContext, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.btn_inPositionScan -> { // 调用摄像头扫描（调入位置）
                smqFlag = '2'
                ScanUtil.startScan(mContext, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                smqFlag = '3'
                ScanUtil.startScan(mContext, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.tv_outPositionName -> { // 调出位置点击
                smqFlag = '1'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.tv_inPositionName -> { // 调入位置点击
                smqFlag = '2'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.tv_icItemName -> { // 物料点击
                smqFlag = '3'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.tv_price -> { // 单价
//                showInputDialog("单价", icStockBillEntry.fprice.toString(), "0.0", RESULT_PRICE)
            }
            R.id.tv_num -> { // 数量
                showInputDialog("主计量单位数", icStockBillEntry.fqty.toString(), "0.0", RESULT_NUM)
            }
            R.id.tv_num2 -> { // 数量
                showInputDialog("辅计量单位数", icStockBillEntry.assistQty.toString(), "0.0", RESULT_NUM2)
            }
            R.id.tv_batchNo -> { // 批次号
                val bundle = Bundle()
                bundle.putInt("icstockBillEntryId", icStockBillEntry.id)
                bundle.putSerializable("icstockBillEntry_Barcodes", icStockBillEntry.icstockBillEntry_Barcodes as Serializable)
                bundle.putString("userName", user!!.username)
                bundle.putString("barcode", getValues(et_code))
                bundle.putInt("againUse", 1)
                showForResult(MoreBatchInputDialog::class.java, RESULT_BATCH, bundle)
            }
            /*R.id.tv_unitSel -> { // 单位
                val bundle = Bundle()
                bundle.putString("accountType", "SC")
                showForResult(Unit_DialogActivity::class.java, SEL_UNIT, bundle)
            }*/
            R.id.tv_remark -> { // 备注
                showInputDialog("备注", icStockBillEntry.remark, "none", RESULT_REMAREK)
            }
            R.id.btn_save -> { // 保存
                if(!checkSave()) return
                icStockBillEntry.icstockBillId = parent!!.fragment1.icStockBill.id
                run_save(null);
            }
            R.id.btn_clone -> { // 重置
                if (checkSaveHint()) {
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
                }
            }
        }
    }

    /**
     * 检查数据
     */
    fun checkSave() : Boolean {
//        if(icStockBillEntry.id == 0) {
        if(icStockBillEntry.fitemId == 0) {
            Comm.showWarnDialog(mContext, "请扫码物料条码，或点击表体列表！")
            return false
        }
        if (icStockBillEntry.fscStockId == 0 || stock2 == null) {
            Comm.showWarnDialog(mContext, "请选择或扫描调出位置！")
            return false
        }
        if (icStockBillEntry.fdcStockId == 0 || stock == null) {
            Comm.showWarnDialog(mContext, "请选择或扫描调入位置！")
            return false
        }
        if(icStockBillEntry.fscStockId == icStockBillEntry.fdcStockId) {
            Comm.showWarnDialog(mContext, "调出仓库和调入仓库不能一样！")
            return false
        }
//        if (icStockBillEntry.fprice == 0.0) {
//            Comm.showWarnDialog(mContext, "请输入单价！")
//            return false;
//        }
        if(icStockBillEntry.icItem.batchManager.equals("Y") && icStockBillEntry.icstockBillEntry_Barcodes.size == 0) {
            Comm.showWarnDialog(mContext, "请输入批次！")
            return false
        }
        if (icStockBillEntry.fqty == 0.0) {
            Comm.showWarnDialog(mContext, "请输入调拨数！")
            return false
        }
        return true
    }

    /**
     * 选择了物料没有点击保存，点击了重置，需要提示
     */
    fun checkSaveHint() : Boolean {
        if(icStockBillEntry.fitemId > 0) {
            return true
        }
        return false
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_outPositionCode -> setFocusable(et_outPositionCode)
                R.id.et_inPositionCode -> setFocusable(et_inPositionCode)
                R.id.et_code -> setFocusable(et_code)
            }
        }
        et_outPositionCode!!.setOnClickListener(click)
        et_inPositionCode!!.setOnClickListener(click)
        et_code!!.setOnClickListener(click)

        // 调出仓库---数据变化
        et_outPositionCode!!.addTextChangedListener(object : TextWatcher {
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
        // 调出仓库---长按输入条码
        et_outPositionCode!!.setOnLongClickListener {
            smqFlag = '1'
            showInputDialog("输入条码", "", "none", WRITE_CODE)
            true
        }
        // 调出仓库---焦点改变
        et_outPositionCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_outFocusPosition.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_outFocusPosition != null) {
                    lin_outFocusPosition!!.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }
        // 位置长按显示仓库详细
        tv_outPositionName.setOnLongClickListener{
            val stockName = if(stock2 != null) stock2!!.stockName else ""
            val stockAreaName = if(stockArea2 != null) "/"+stockArea2!!.fname else ""
            val storageRackName = if(storageRack2 != null) "/"+storageRack2!!.fnumber else ""
            val stockPosName = if(stockPos2 != null) "/"+stockPos2!!.fname else ""
            Comm.showWarnDialog(mContext,stockName+stockAreaName+storageRackName+stockPosName)
            true
        }

        // 调入仓库---数据变化
        et_inPositionCode!!.addTextChangedListener(object : TextWatcher {
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
        // 调入仓库---长按输入条码
        et_inPositionCode!!.setOnLongClickListener {
            smqFlag = '2'
            showInputDialog("输入条码", "", "none", WRITE_CODE)
            true
        }
        // 调入仓库---焦点改变
        et_inPositionCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_inFocusPosition.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_inFocusPosition != null) {
                    lin_inFocusPosition!!.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }
        // 位置长按显示仓库详细
        tv_inPositionName.setOnLongClickListener{
            val stockName = if(stock != null) stock!!.stockName else ""
            val stockAreaName = if(stockArea != null) "/"+stockArea!!.fname else ""
            val storageRackName = if(storageRack != null) "/"+storageRack!!.fnumber else ""
            val stockPosName = if(stockPos != null) "/"+stockPos!!.fname else ""
            Comm.showWarnDialog(mContext,stockName+stockAreaName+storageRackName+stockPosName)
            true
        }

        // 物料---数据变化
        et_code!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                if (!isTextChange) {
                    isTextChange = true
                    smqFlag = '3'
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 物料---长按输入条码
        et_code!!.setOnLongClickListener {
            smqFlag = '3'
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

        // 主数量---数据变化
        tv_num!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if(isNumTextChanged && icStockBillEntry.unit != null && icStockBillEntry.assistUnit != null) {
                    isNumTextChanged = false
                    isNum2TextChanged = false
                    var num = parseDouble(s)
                    var divVal = BigdecimalUtil.div(num, unitConvertRatio)
                    tv_num2.text = df.format(divVal)
                    icStockBillEntry.fqty = num
                    icStockBillEntry.assistQty = divVal
                    isNumTextChanged = true
                    isNum2TextChanged = true
                }
            }
        })
        // 辅数量---数据变化
        tv_num2!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if(isNum2TextChanged && icStockBillEntry.unit != null && icStockBillEntry.assistUnit != null) {
                    isNumTextChanged = false
                    isNum2TextChanged = false
                    var num = parseDouble(s)
                    var mulVal = BigdecimalUtil.mul(num, unitConvertRatio)
                    tv_num.text = df.format(mulVal)
                    icStockBillEntry.fqty = mulVal
                    icStockBillEntry.assistQty = num
                    isNumTextChanged = true
                    isNum2TextChanged = true
                }
            }
        })

        cb_outRemember.setOnCheckedChangeListener { buttonView, isChecked ->
            saveLocalStockGroup(isChecked,1)
        }
        cb_inRemember.setOnCheckedChangeListener { buttonView, isChecked ->
            saveLocalStockGroup(isChecked,0)
        }
    }

    /**
     * 保存仓库信息到本地
     */
    private fun saveLocalStockGroup(isBool :Boolean, flag :Int) {
        val saveOther = getResStr(R.string.saveOther)
        if(flag == 1) { // 调出仓库
            if(isBool) { // 记住仓库信息
                // 对象保存到xml
                if(stock2 != null) saveObjectToXml(stock2, STOCK2_FLAG, saveOther)
                else spfRemove(STOCK2_FLAG, saveOther)

                if(stockArea2 != null) saveObjectToXml(stockArea2, STOCKAREA2_FLAG, saveOther)
                else spfRemove(STOCKAREA2_FLAG, saveOther)

                if(storageRack2 != null) saveObjectToXml(storageRack2, STORAGERACK2_FLAG, saveOther)
                else spfRemove(STORAGERACK2_FLAG, saveOther)

                if(stockPos2 != null) saveObjectToXml(stockPos2, STOCKPOS2_FLAG, saveOther)
                else spfRemove(STOCKPOS2_FLAG, saveOther)

            } else { // 清空仓库信息
                spfRemove(STOCK2_FLAG, saveOther)
                spfRemove(STOCKAREA2_FLAG, saveOther)
                spfRemove(STORAGERACK2_FLAG, saveOther)
                spfRemove(STOCKPOS2_FLAG, saveOther)
            }
        } else {
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

    }

    /**
     * 0：表示点击重置，1：表示保存后重置
     */
    private fun reset() {
        setEnables(tv_batchNo, R.drawable.back_style_blue, true)
        setEnables(tv_num, R.drawable.back_style_blue, true)
        btn_save.text = "添加"
        tv_mtlName.text = ""
        tv_mtlNumber.text = "物料代码："
        tv_fmodel.text = "规格型号："
        tv_unitName.text = "主计量单位："
        tv_unitName2.visibility = View.GONE
        tv_unitConvertRatio.visibility = View.GONE
        tv_stockQty.text = "即时库存：0"
        tv_batchNo.text = ""
        tv_num.text = ""
        tv_remark.text = ""

        icStockBillEntry.id = 0
        icStockBillEntry.icstockBillId = parent!!.fragment1.icStockBill.id
        icStockBillEntry.fitemId = 0
//        icStockBillEntry.fdcStockId = 0
//        icStockBillEntry.fdcSPId = 0
        icStockBillEntry.fqty = 0.0
        icStockBillEntry.fprice = 0.0
        icStockBillEntry.funitId = 0
        icStockBillEntry.remark = ""

        icStockBillEntry.icItem = null
        icStockBillEntry.icstockBillEntry_Barcodes.clear()
        smICStockBillEntry = null
        smICStockBillEntry_Barcodes.clear()
//        stock = null
//        stockPos = null
        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
        parent!!.isChange = false
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    /**
     *  扫码之后    物料启用批次
     */
    fun setBatchCode(fqty : Double) {
        val entryBarcode = ICStockBillEntry_Barcode()
        entryBarcode.parentId = smICStockBillEntry!!.id
        entryBarcode.barcode = getValues(et_code)
        entryBarcode.batchCode = smICStockBillEntry!!.smBatchCode
        entryBarcode.snCode = ""
        entryBarcode.fqty = fqty
        entryBarcode.isUniqueness = 'Y'
        entryBarcode.againUse = 0
        entryBarcode.createUserName = user!!.username
        entryBarcode.billType = parent!!.fragment1.icStockBill.billType

        smICStockBillEntry_Barcodes.add(entryBarcode)
        getICStockBillEntry(smICStockBillEntry!!)
    }

    /**
     *  扫码之后    物料启用序列号
     */
    fun setSnCode() {
        val entryBarcode = ICStockBillEntry_Barcode()
        entryBarcode.parentId = smICStockBillEntry!!.id
        entryBarcode.barcode = getValues(et_code)
        entryBarcode.batchCode = ""
        entryBarcode.snCode = smICStockBillEntry!!.smSnCode
        entryBarcode.fqty = 1.0
        entryBarcode.isUniqueness = 'Y'
        entryBarcode.againUse = 0
        entryBarcode.createUserName = user!!.username
        entryBarcode.billType = parent!!.fragment1.icStockBill.billType

        smICStockBillEntry_Barcodes.add(entryBarcode)
        getICStockBillEntry(smICStockBillEntry!!)
    }

    /**
     *  扫码之后    物料未启用
     */
    fun unSetBatchOrSnCode(fqty : Double) {
        val entryBarcode = ICStockBillEntry_Barcode()
        entryBarcode.parentId = smICStockBillEntry!!.id
        entryBarcode.barcode = getValues(et_code)
        entryBarcode.batchCode = ""
        entryBarcode.snCode = ""
        entryBarcode.fqty = fqty
        entryBarcode.isUniqueness = 'N'
        entryBarcode.againUse = 0
        entryBarcode.createUserName = user!!.username
        entryBarcode.billType = parent!!.fragment1.icStockBill.billType

        smICStockBillEntry_Barcodes.add(entryBarcode)
        getICStockBillEntry(smICStockBillEntry!!)
    }

    fun getMaterial(icEntry: ICStockBillEntry) {
        smICStockBillEntry = icEntry

        btn_save.text = "保存"
        // 判断条码是否存在（启用批次，序列号）
        if (icStockBillEntry.icstockBillEntry_Barcodes.size > 0 && (icEntry.icItem.batchManager.equals("Y") || icEntry.icItem.snManager.equals("Y"))) {
            icStockBillEntry.icstockBillEntry_Barcodes.forEach {
                if (getValues(et_code).length > 0 && getValues(et_code) == it.barcode) {
                    Comm.showWarnDialog(mContext,"条码已使用！")
                    return
                }
            }
        }
        if(icEntry.icItem.batchManager.equals("Y")) { // 启用批次号
            val showInfo:String = "<font color='#666666'>批次号：</font>" + icEntry.smBatchCode+"<font color='#666666'>可用库存：</font>"+"<font color='#FF2200'>"+df.format(icEntry.inventoryNowQty)+"</font>"
            showInputDialog("数量", showInfo, icEntry.smQty.toString(), "0.0", SM_RESULT_NUM)

        } else if(icEntry.icItem.snManager.equals("Y")) { // 启用序列号
            setSnCode()

        } else { // 未启用
            unSetBatchOrSnCode(icEntry.smQty)
        }
        if(icEntry.icstockBillEntry_Barcodes.size > 0) {
            if (smICStockBillEntry_Barcodes.size > 0) {
                var isBool = true
                icEntry.icstockBillEntry_Barcodes.forEach {
                    isBool = false
                    for (it2 in smICStockBillEntry_Barcodes) {
                        if(it.barcode == it2.barcode) {
                            isBool = false
                            break
                        }
                    }
                    if(isBool) {
                        smICStockBillEntry_Barcodes.add(it)
                    }
                }
            } else {
                smICStockBillEntry_Barcodes.addAll(icEntry.icstockBillEntry_Barcodes)
            }
        } else {
            smICStockBillEntry_Barcodes.addAll(icEntry.icstockBillEntry_Barcodes)
        }
    }

    fun getICStockBillEntry(icEntry: ICStockBillEntry) {
        icStockBillEntry.id = icEntry.id
        icStockBillEntry.icstockBillId = icEntry.icstockBillId
        icStockBillEntry.finterId = icEntry.finterId
        icStockBillEntry.fitemId = icEntry.fitemId
        icStockBillEntry.fentryId = icEntry.fentryId
        icStockBillEntry.fdcStockId = icEntry.fdcStockId
        icStockBillEntry.fdcSPId = icEntry.fdcSPId
        icStockBillEntry.stockId_wms = icEntry.stockId_wms
        icStockBillEntry.stockAreaId_wms = icEntry.stockAreaId_wms
        icStockBillEntry.storageRackId_wms = icEntry.storageRackId_wms
        icStockBillEntry.stockPosId_wms = icEntry.stockPosId_wms
        icStockBillEntry.fscStockId = icEntry.fscStockId
        icStockBillEntry.fscSPId = icEntry.fscSPId
        icStockBillEntry.stockId2_wms = icEntry.stockId2_wms
        icStockBillEntry.stockAreaId2_wms = icEntry.stockAreaId2_wms
        icStockBillEntry.storageRackId2_wms = icEntry.storageRackId2_wms
        icStockBillEntry.stockPosId2_wms = icEntry.stockPosId2_wms
//        icStockBillEntry.fbatchNo = icEntry.fbatchNo
//        icStockBillEntry.fqty = icEntry.fqty
        icStockBillEntry.fsourceQty = icEntry.fsourceQty
        icStockBillEntry.qcPassQty = icEntry.qcPassQty
        icStockBillEntry.fprice = icEntry.fprice
        icStockBillEntry.funitId = icEntry.funitId
        icStockBillEntry.fkfDate = icEntry.fkfDate
        icStockBillEntry.fkfPeriod = icEntry.fkfPeriod
        icStockBillEntry.remark = icEntry.remark
        icStockBillEntry.inventoryNowQty = icEntry.inventoryNowQty
        icStockBillEntry.assistUnitId = icEntry.assistUnitId
        icStockBillEntry.assistQty = icEntry.assistQty

        icStockBillEntry.icItem = icEntry.icItem
        icStockBillEntry.unit = icEntry.unit
        icStockBillEntry.assistUnit = icEntry.assistUnit

        tv_mtlName.text = icEntry.icItem.fname
        tv_mtlNumber.text = Html.fromHtml("物料代码：<font color='#6a5acd'>"+icEntry.icItem.fnumber+"</font>")
        tv_fmodel.text = Html.fromHtml("规格型号：<font color='#6a5acd'>"+icEntry.icItem.fmodel+"</font>")
//        tv_price.text = df.format(icEntry.fprice)
//        tv_batchNo.text = icEntry.fbatchNo
//        if(icEntry.icItem.batchManager.equals("Y")) {
//            setEnables(tv_batchNo, R.drawable.back_style_blue, true)
//        } else {
        setEnables(tv_batchNo, R.drawable.back_style_gray3, false)
//        }
        if(icEntry.icItem.batchManager.equals("Y") || icEntry.icItem.snManager.equals("Y")) {
            setEnables(tv_num, R.drawable.back_style_gray3, false)
        } else {
            setEnables(tv_num, R.drawable.back_style_blue, true)
        }
//        tv_num.text = if(icEntry.fqty > 0) df.format(icEntry.fqty) else ""
        if(icEntry.unit != null) {
            tv_unitName.visibility = View.VISIBLE
            tv_unitName.text = Html.fromHtml("主计量单位：<font color='#6a5acd'>"+icEntry.unit.unitName+"</font>")
        } else {
            tv_unitName.visibility = View.GONE
        }
        if(icEntry.assistUnit != null) {
            tv_unitName2.visibility = View.VISIBLE
            tv_unitConvertRatio.visibility = View.VISIBLE
            tv_unitName2.text = Html.fromHtml("辅计量单位：<font color='#6a5acd'>"+icEntry.assistUnit.unitName+"</font>")
            tv_num2.isEnabled = true
            tv_num2.setBackgroundResource(R.drawable.back_style_blue)
            tv_unitConvertRatio.visibility = View.VISIBLE
        } else {
            tv_unitName2.visibility = View.GONE
            tv_unitConvertRatio.visibility = View.GONE
            tv_num2.isEnabled = false
            tv_num2.setBackgroundResource(R.drawable.back_style_gray3)
        }
        tv_unitConvertRatio.text = Html.fromHtml("转换率：<font color='#000000'>"+df.format(icEntry.icItem.unitConvertRatio)+"</font>")
        unitConvertRatio = icEntry.icItem.unitConvertRatio
        tv_remark.text = icEntry.remark

//        val mul = BigdecimalUtil.mul(icEntry.fprice, icEntry.fqty)
//        tv_sumMoney.text = df.format(mul)
        // 显示调出仓库
        if(icEntry.stockId2_wms > 0) {
            stock2 = icEntry.stock2
            stockArea2 = icEntry.stockArea2
            storageRack2 = icEntry.storageRack2
            stockPos2 = icEntry.stockPos2
        }
        getStockGroup2(null, true)

        // 显示调入仓库
        if(icEntry.stockId_wms > 0) {
            stock = icEntry.stock
            stockArea = icEntry.stockArea
            storageRack = icEntry.storageRack
            stockPos = icEntry.stockPos
        }
        getStockGroup(null)

        // 物料未启用
        if(icEntry.icstockBillEntry_Barcodes.size > 0 && icEntry.icItem.batchManager.equals("N") && icEntry.icItem.snManager.equals("N")) {
            showBatch_Qty(null, icEntry.fqty)
        } else {
            // 显示多批次
//        showBatch_Qty(icEntry.icstockBillEntry_Barcodes, icEntry.fqty)
            showBatch_Qty(smICStockBillEntry_Barcodes, icEntry.fqty)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SEL_POSITION -> {// 调入仓库	返回
                    resetStockGroup()
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
                SEL_POSITION2 -> {// 调出仓库	返回
                    resetStockGroup2()
                    stock2 = data!!.getSerializableExtra("stock") as Stock
                    if (data!!.getSerializableExtra("stockArea") != null) {
                        stockArea2 = data!!.getSerializableExtra("stockArea") as StockArea
                    }
                    if (data!!.getSerializableExtra("storageRack") != null) {
                        storageRack2 = data!!.getSerializableExtra("storageRack") as StorageRack
                    }
                    if (data!!.getSerializableExtra("stockPos") != null) {
                        stockPos2 = data!!.getSerializableExtra("stockPos") as StockPosition
                    }
                    getStockGroup2(null, true)
                }
                SEL_MTL -> { //查询物料	返回
                    val icItem = data!!.getSerializableExtra("obj") as ICItem
                    smqFlag = '4'
                    run_smDatas(icItem.fitemid)
                }
                SEL_UNIT -> { //查询单位	返回
                    val unit = data!!.getSerializableExtra("obj") as MeasureUnit
//                    tv_unitSel.text = unit.getfName()
                    icStockBillEntry.funitId = unit.fitemID
                }
                RESULT_PRICE -> { // 单价	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val price = parseDouble(value)
//                        tv_price.text = df.format(price)
//                        icStockBillEntry.fprice = price
//                        if(icStockBillEntry.fqty > 0) {
//                            val mul = BigdecimalUtil.mul(price, icStockBillEntry.fqty)
//                            tv_sumMoney.text = df.format(mul)
//                        }
                    }
                }
                RESULT_NUM -> { // 数量	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        tv_num.text = df.format(num)
                        icStockBillEntry.fqty = num
                    }
                }
                RESULT_NUM2 -> { // 数量2	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        tv_num2.text = df.format(num)
                        icStockBillEntry.assistQty = num
                    }
                }
                SM_RESULT_NUM -> { // 扫码数量	    返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        setBatchCode(num)
                    }
                }
                RESULT_BATCH -> { // 批次号	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val list = bundle.getSerializable("icstockBillEntry_Barcodes") as List<ICStockBillEntry_Barcode>
                        smICStockBillEntry_Barcodes.clear()
                        smICStockBillEntry_Barcodes.addAll(list)
                        showBatch_Qty(smICStockBillEntry_Barcodes, 0.0)
                    }
                }
                RESULT_REMAREK -> { // 备注	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        tv_remark.text = value
                        icStockBillEntry.remark = value
                    }
                }
                RESULT_PUR_ORDER -> { // 选择单据   返回
//                    if(icStockBillEntry.fsourceTranType == 71) {
//                        val list = data!!.getSerializableExtra("obj") as List<POOrderEntry>
//                        setICStockEntry_POOrder(list)
//                    } else if(icStockBillEntry.fsourceTranType == 72){
//                        val list = data!!.getSerializableExtra("obj") as List<POInStockEntry>
//                        setICStockEntry_PPBomTransfer(list)
//                    }
                }
                WRITE_CODE -> {// 输入条码  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        when (smqFlag) {
                            '1' -> setTexts(et_outPositionCode, value.toUpperCase())
                            '2' -> setTexts(et_inPositionCode, value.toUpperCase())
                            '3' -> setTexts(et_code, value.toUpperCase())
                        }
                    }
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    /**
     * 调用华为扫码接口，返回的值
     */
    fun getScanData(barcode :String) {
        when (smqFlag) {
            '1' -> setTexts(et_outPositionCode, barcode)
            '2' -> setTexts(et_inPositionCode, barcode)
            '3' -> setTexts(et_code, barcode)
        }
    }

    /**
     *  显示批次号和数量
     */
    fun showBatch_Qty(list : List<ICStockBillEntry_Barcode>?, fqty : Double) {
        if(list != null && list.size > 0) {
            val strBatch = StringBuffer()
            var sumQty = 0.0
            val listBatch = ArrayList<String>()

            list.forEach{
                if(Comm.isNULLS(it.batchCode).length > 0 && !listBatch.contains(it.batchCode)) {
                    listBatch.add(it.batchCode)
                }
                sumQty += it.fqty
            }
            listBatch.forEach {
                strBatch.append(it + "，")
            }
            // 删除最后一个，
            if (strBatch.length > 0) {
                strBatch.delete(strBatch.length - 1, strBatch.length)
            }
            tv_batchNo.text = strBatch.toString()
            tv_num.text = df.format(sumQty)

            icStockBillEntry.fqty = sumQty
            icStockBillEntry.icstockBillEntry_Barcodes.clear()
            icStockBillEntry.icstockBillEntry_Barcodes.addAll(list)
        } else {
            icStockBillEntry.fqty = fqty
            tv_batchNo.text = ""
            tv_num.text = if(fqty > 0) df.format(fqty) else ""
        }
    }

    fun resetStockGroup() {
        stock = null
        stockArea = null
        storageRack = null
        stockPos = null
    }

    fun resetStockGroup2() {
        stock2 = null
        stockArea2 = null
        storageRack2 = null
        stockPos2 = null
    }

    /**
     * 得到调入仓库组
     */
    fun getStockGroup(msgObj : String?) {
        // 重置数据
        icStockBillEntry.fdcStockId = 0
        icStockBillEntry.stockId_wms = 0
        icStockBillEntry.stockAreaId_wms = 0
        icStockBillEntry.storageRackId_wms = 0
        icStockBillEntry.fdcSPId = 0
        icStockBillEntry.stockPosId_wms = 0

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
                    tv_inPositionName.text = stock!!.stockName
                }
                2 -> {
                    stockArea = JsonUtil.strToObject(msgObj, StockArea::class.java)
                    tv_inPositionName.text = stockArea!!.fname
                    if(stockArea!!.stock != null) stock = stockArea!!.stock
                }
                3 -> {
                    storageRack = JsonUtil.strToObject(msgObj, StorageRack::class.java)
                    tv_inPositionName.text = storageRack!!.fnumber
                    if(storageRack!!.stock != null) stock = storageRack!!.stock
                    if(storageRack!!.stockArea != null)  stockArea = storageRack!!.stockArea
                }
                4 -> {
                    stockPos = JsonUtil.strToObject(msgObj, StockPosition::class.java)
                    tv_inPositionName.text = stockPos!!.stockPositionName
                    if(stockPos!!.stock != null) stock = stockPos!!.stock
                    if(stockPos!!.stockArea != null)  stockArea = stockPos!!.stockArea
                    if(stockPos!!.storageRack != null)  storageRack = stockPos!!.storageRack
                }
            }
        }

        if(stock != null ) {
            tv_inPositionName.text = stock!!.stockName
            icStockBillEntry.fdcStockId = stock!!.fitemId
            icStockBillEntry.stockId_wms = stock!!.id
        }
        if(stockArea != null ) {
            tv_inPositionName.text = stockArea!!.fname
            icStockBillEntry.stockAreaId_wms = stockArea!!.id
        }
        if(storageRack != null ) {
            tv_inPositionName.text = storageRack!!.fnumber
            icStockBillEntry.storageRackId_wms = storageRack!!.id
        }
        if(stockPos != null ) {
            tv_inPositionName.text = stockPos!!.stockPositionName
            icStockBillEntry.fdcSPId = stockPos!!.fitemId
            icStockBillEntry.stockPosId_wms = stockPos!!.id
        }
        saveLocalStockGroup(cb_inRemember.isChecked, 0)

        if(stock != null) {
            // 自动跳到物料焦点
            smqFlag = '3'
            mHandler.sendEmptyMessage(SETFOCUS)
        }
    }

    /**
     * 得到调出仓库组
     */
    fun getStockGroup2(msgObj : String?, isSearch :Boolean) {
        // 重置数据
        icStockBillEntry.fscStockId = 0
        icStockBillEntry.stockId2_wms = 0
        icStockBillEntry.stockAreaId2_wms = 0
        icStockBillEntry.storageRackId2_wms = 0
        icStockBillEntry.fscSPId = 0
        icStockBillEntry.stockPosId2_wms = 0

        if(msgObj != null) {
            stock2 = null
            stockArea2 = null
            storageRack2 = null
            stockPos2 = null

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
                    stock2 = JsonUtil.strToObject(msgObj, Stock::class.java)
                    tv_outPositionName.text = stock2!!.stockName
                }
                2 -> {
                    stockArea2 = JsonUtil.strToObject(msgObj, StockArea::class.java)
                    tv_outPositionName.text = stockArea2!!.fname
                    if(stockArea2!!.stock != null) stock2 = stockArea2!!.stock
                }
                3 -> {
                    storageRack2 = JsonUtil.strToObject(msgObj, StorageRack::class.java)
                    tv_outPositionName.text = storageRack2!!.fnumber
                    if(storageRack2!!.stock != null) stock2 = storageRack2!!.stock
                    if(storageRack2!!.stockArea != null)  stockArea2 = storageRack2!!.stockArea
                }
                4 -> {
                    stockPos2 = JsonUtil.strToObject(msgObj, StockPosition::class.java)
                    tv_outPositionName.text = stockPos2!!.stockPositionName
                    if(stockPos2!!.stock != null) stock2 = stockPos2!!.stock
                    if(stockPos2!!.stockArea != null)  stockArea2 = stockPos2!!.stockArea
                    if(stockPos2!!.storageRack != null)  storageRack2 = stockPos2!!.storageRack
                }
            }
        }

        if(stock2 != null ) {
            tv_outPositionName.text = stock2!!.stockName
            icStockBillEntry.fscStockId = stock2!!.fitemId
            icStockBillEntry.stockId2_wms = stock2!!.id
        }
        if(stockArea2 != null ) {
            tv_outPositionName.text = stockArea2!!.fname
            icStockBillEntry.stockAreaId2_wms = stockArea2!!.id
        }
        if(storageRack2 != null ) {
            tv_outPositionName.text = storageRack2!!.fnumber
            icStockBillEntry.storageRackId2_wms = storageRack2!!.id
        }
        if(stockPos2 != null ) {
            tv_outPositionName.text = stockPos2!!.stockPositionName
            icStockBillEntry.fscSPId = stockPos2!!.fitemId
            icStockBillEntry.stockPosId2_wms = stockPos2!!.id
        }
        saveLocalStockGroup(cb_outRemember.isChecked, 1)
        // 查询即时库存
        if(icStockBillEntry.fscStockId > 0 && icStockBillEntry.fitemId > 0 && isSearch) {
            run_findInventoryQty()
        }

        if(stock2 != null) {
            // 自动跳到调入仓焦点
            smqFlag = '2'
            mHandler.sendEmptyMessage(SETFOCUS)
        }
    }

    private fun setICStockEntry_PPBomTransfer(list : List<PPBomTransferEntry>?) {
        parent!!.fragment1.icStockBill.fselTranType = 0
        var listEntry = ArrayList<ICStockBillEntry>()
        list!!.forEach {
            val entry = ICStockBillEntry()
            entry.icstockBillId = parent!!.fragment1.icStockBill.id
            entry.fitemId = it.icItemId
//            entry.fentryId = it.fentryid  // 后端会自动计算

            entry.fdcStockId = it.inStockId
            entry.fdcSPId = 0
            entry.stockId_wms = it.inStock.id
            entry.stockAreaId_wms = it.inStockAreaId
            entry.storageRackId_wms = it.inStorageRackId
            entry.stockPosId_wms = it.inStockPositionId

            entry.fscStockId = it.outStockId
            entry.fscSPId = 0
            entry.stockId2_wms = it.outStock.id
            entry.stockAreaId2_wms = it.outStockAreaId
            entry.storageRackId2_wms = it.outStorageRackId
            entry.stockPosId2_wms = it.outStockPositionId

//            entry.fqty = it.useableQty
//            entry.fprice = it.fprice
            entry.funitId = it.unitId
            entry.fsourceInterId = it.ppBomTransferId
            entry.fsourceEntryId = it.id
//            entry.fsourceQty = it.mustQty
            entry.fsourceQty = it.useableQty
            entry.qcPassQty = it.erpQty // 使用qcPassQty来保存投料调拨单的Erp申请数
            entry.fsourceTranType = 0
            entry.fsourceBillNo = it.ppBomTransfer.billNo
            entry.fdetailId = it.id

            entry.fkfDate = null
            entry.assistUnitId = it.icItem.assistUnitid
            entry.assistQty = 0.0
            entry.remark = ""
            listEntry.add(entry)
        }
        run_save(listEntry)
    }

    /**
     * 扫码查询对应的方法
     */
    private fun run_smDatas(mtlId : Int) {
        showLoadDialog("加载中...", false)
        var mUrl:String? = null
        var barcode = ""
        var icstockBillId = ""
        var moreStock = "" // 多仓库查询
        var billType = "" // 单据类型
        var checkInventoryNow = "" // 是否检查库存
        var fitemid = "" // 物料id
        when(smqFlag) {
            '1' -> {
                mUrl = getURL("stockPosition/findBarcodeGroup")
                barcode = getValues(et_outPositionCode)
            }
            '2' -> {
                mUrl = getURL("stockPosition/findBarcodeGroup")
                barcode = getValues(et_inPositionCode)
            }
            '3' -> {
                mUrl = getURL("stockBill_WMS/findBarcode")
                barcode = getValues(et_code)
                icstockBillId = parent!!.fragment1.icStockBill.id.toString()
                moreStock = "1"
                billType = parent!!.fragment1.icStockBill.billType
                checkInventoryNow = "1"
            }
            '4' -> { // 选择物料后，查询的数据
                mUrl = getURL("stockBill_WMS/findICStockBillEntryByMtlId")
                icstockBillId = parent!!.fragment1.icStockBill.id.toString()
                checkInventoryNow = "1"
                fitemid = mtlId.toString()
            }
        }
        val formBody = FormBody.Builder()
                .add("barcode", barcode)
                .add("icstockBillId", icstockBillId)
                .add("moreStock", moreStock)
                .add("billType", billType)
//                .add("checkInventoryNow", checkInventoryNow)
                .add("fitemid", fitemid)
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
    private fun run_save(list: List<ICStockBillEntry>?) {
        showLoadDialog("保存中...", false)
        var mUrl:String? = null
        var mJson:String? = null
        if(list != null) {
            mUrl = getURL("stockBill_WMS/saveEntryList")
            mJson = JsonUtil.objectToString(list)
        } else {
            mUrl = getURL("stockBill_WMS/saveEntry")
            mJson = JsonUtil.objectToString(icStockBillEntry)
        }
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
        showLoadDialog("加载中...", false)
        val mUrl = getURL("icInventory/findInventoryQty")
        val formBody = FormBody.Builder()
                .add("fStockID", icStockBillEntry.fscStockId.toString())
                .add("fStockPlaceID",  icStockBillEntry.fscSPId.toString())
                .add("mtlId", icStockBillEntry.fitemId.toString())
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
     * 查询最小领用数
     */
    private fun run_findMinQtyById(id :Int) {
        val mUrl = getURL("ppBomTransfer/findMinQtyById")
        val formBody = FormBody.Builder()
                .add("id", id.toString())
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNFIND_MINQTY)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_findMinQtyById --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNFIND_MINQTY, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(FIND_MINQTY, result)
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
        EventBus.getDefault().unregister(this);
        super.onDestroyView()
    }
}