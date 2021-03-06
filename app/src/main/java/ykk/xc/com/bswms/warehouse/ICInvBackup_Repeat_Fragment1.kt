package ykk.xc.com.bswms.warehouse

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import kotlinx.android.synthetic.main.ware_icinvbackup_repeat_fragment1.*
import okhttp3.*
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.basics.Container_DialogActivity
import ykk.xc.com.bswms.basics.Mtl_MoreDialogActivity
import ykk.xc.com.bswms.basics.Stock_GroupDialogActivity
import ykk.xc.com.bswms.bean.*
import ykk.xc.com.bswms.bean.k3Bean.ICInvBackup
import ykk.xc.com.bswms.bean.k3Bean.ICItem
import ykk.xc.com.bswms.bean.k3Bean.ICStockCheckProcess
import ykk.xc.com.bswms.comm.BaseFragment
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.JsonUtil
import ykk.xc.com.bswms.util.LogUtil
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import ykk.xc.com.bswms.warehouse.adapter.ICInvBackup_Repeat_Fragment1_Adapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：WMS 复盘
 * 作者：ykk
 */
class ICInvBackup_Repeat_Fragment1 : BaseFragment() {

    companion object {
        private val SEL_STOCK = 10
        private val SEL_MTL = 11
        private val SEL_CONTAINER = 12

        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SUCC2 = 201
        private val UNSUCC2 = 501
        private val SUBMIT = 202
        private val UNSUBMIT = 502
        private val SAVE = 203
        private val UNSAVE = 503
        private val SETFOCUS = 1
        private val RESULT_NUM = 2
        private val RESULT_BATCH = 3
        private val RESULT_WEIGHT = 4
        private val RESULT_MINPACK = 5
        private val SAOMA = 6
        private val WRITE_CODE = 7
    }

    private val context = this
    private var mAdapter: ICInvBackup_Repeat_Fragment1_Adapter? = null
    private val checkDatas = ArrayList<ICInvBackup>()
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private var parent: ICInvBackup_Repeat_MainActivity? = null
    private var stock:Stock? = null
    private var stockArea: StockArea? = null
    private var storageRack: StorageRack? = null
    private var stockPos: StockPosition? = null
    private var container: Container? = null

    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var icStockCheckProcess: ICStockCheckProcess? = null
    private var curPos:Int = 0 // 当前行
    private var smqFlag = '1' // 使用扫码枪扫码（1：仓库位置扫码，2：容器扫码，3：物料扫码）
    private var mapId = HashMap<Int, Boolean>() // 判断唯一性

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: ICInvBackup_Repeat_Fragment1) : Handler() {
        private val mActivity: WeakReference<ICInvBackup_Repeat_Fragment1>

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
                            '1' -> { // 仓库位置扫描
                                m.resetStockGroup()
                                m.getStockGroup(msgObj)
//                                if(m.checkDatas.size == 0) {
//                                    m.run_okhttpDatas(null)
//                                }
                            }
                            '2' -> { // 容器扫描
                                val container = JsonUtil.strToObject(msgObj, Container::class.java)
                                m.getContainer(container)
                            }
                            '3' -> { // 物料扫描
                                val list = JsonUtil.strToList(msgObj, ICInvBackup::class.java)
                                val icInvBackup = list[0]
                                m.tv_mtlName.text = icInvBackup.mtlNumber
                                // 显示仓库信息
//                                m.stock = null
//                                m.stockArea = null
//                                m.storageRack = null
//                                m.stockPos = null
//
//                                m.stock = icInvBackup.stock
//                                m.stockArea = icInvBackup.stockArea
//                                m.storageRack = icInvBackup.storageRack
//                                m.stockPos = icInvBackup.stockPos
//                                m.getStockGroup(null)

                                list.forEach{
                                    if(it.repeatStatus == 0) { // 如果未盘点
                                        it.repeatQty = it.realQty // 默认盘点数等于复盘数
                                    }
                                    it.repeatStatus = 1
                                    it.repeatUserId = m.user!!.id

                                    if(!m.mapId.containsKey(it.id)) {
                                        m.mapId.put(it.id, true)
                                        m.checkDatas.add(it)
                                    }
                                }
                                m.parent!!.isChange = true
                                m.mAdapter!!.notifyDataSetChanged()
//                                m.getMtlAfter(listIcitem, 0)
                            }
                        }
                        m.isTextChange = false
                    }
                    UNSUCC1 -> { // 扫码失败
                        m.isTextChange = false
                        m.mAdapter!!.notifyDataSetChanged()
                        when(m.smqFlag) {
                            '1' -> { // 仓库位置扫描
//                                m.tv_positionName.text = ""
                            }
                            '2' -> { // 容器扫描
//                                m.container = null
//                                m.tv_containerName.text = ""
                            }
                            '3' -> { // 物料扫描
//                                m.tv_mtlName.text = ""
                            }
                        }
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没能找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC2 -> { // 历史查询 进入
                        m.checkDatas.clear()
//                        val icInvBackup = JsonUtil.strToList(msgObj, ICInvBackup::class.java)
//                        m.checkDatas.addAll(icInvBackup)
//                        m.mAdapter!!.notifyDataSetChanged()
                    }
                    UNSUCC2 -> { // 历史查询  失败
                        m.mAdapter!!.notifyDataSetChanged()
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没能找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SAVE -> { // 保存成功 进入
                        m.reset()
                        m.toasts("保存成功✔")
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUBMIT -> { // 提交成功 返回
                        m.reset()
                        m.toasts("提交成功✔")
                    }
                    UNSUBMIT // 提交失败 返回
                    -> {
                        errMsg = JsonUtil.strToString(msgObj!!)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "服务器忙，请稍候再试！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        when(m.smqFlag) {
                            '1' -> m.setFocusable(m.et_positionCode)
                            '2' -> m.setFocusable(m.et_containerCode)
                            '3' -> m.setFocusable(m.et_code)
                        }
                    }
                    SAOMA -> { // 扫码之后
//                        if(!m.checkSaoMa()) {
//                            m.isTextChange = false
//                            return
//                        }
                        // 执行查询方法
                        m.run_okhttpDatas(null)
                    }
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.ware_icinvbackup_repeat_fragment1, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as ICInvBackup_Repeat_MainActivity

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = ICInvBackup_Repeat_Fragment1_Adapter(mContext!!, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : ICInvBackup_Repeat_Fragment1_Adapter.MyCallBack {
            override fun onClick_weight(entity: ICInvBackup, position: Int) {
                curPos = position
                showInputDialog("重量", entity.weight.toString(), "0.0", RESULT_WEIGHT)
            }

            override fun onClick_num(entity: ICInvBackup, position: Int) {
                curPos = position
                showInputDialog("数量", entity.repeatQty.toString(), "0.0", RESULT_NUM)
            }

            override fun onClick_minPackQty(entity: ICInvBackup, position: Int) {
                curPos = position
                showInputDialog("最小包装数", entity.minPackQty.toString(), "0.0", RESULT_MINPACK)
            }

            override fun onClick_batch(entity: ICInvBackup, position: Int) {
                curPos = position
                showInputDialog("批次号", Comm.isNULLS(entity.fbatchNo), "none", RESULT_BATCH)
            }

            override fun onDelete(entity: ICInvBackup, position: Int) {
                mapId.remove(entity.id)
                checkDatas.removeAt(position)
                mAdapter!!.notifyDataSetChanged()
            }
        })

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            checkSaveSon(smqFlag, pos)
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
        hideSoftInputMode(mContext, et_positionCode)
        hideSoftInputMode(mContext, et_code)
        hideSoftInputMode(mContext, et_containerCode)

        mHandler.sendEmptyMessageDelayed(SETFOCUS,200)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
    }

    @OnClick(R.id.btn_positionSel, R.id.btn_positionScan, R.id.btn_containerSel, R.id.btn_containerScan, R.id.btn_mtlSel, R.id.btn_scan,
            R.id.tv_positionName, R.id.tv_containerName, R.id.tv_mtlName,
            R.id.btn_save, R.id.btn_clone, R.id.btn_submit)
    fun onViewClicked(view: View) {
        var bundle: Bundle? = null
        when (view.id) {
            R.id.btn_positionSel -> { // 选择仓库位置
                smqFlag = '1'
                bundle = Bundle()
                bundle.putSerializable("stock", stock)
                bundle.putSerializable("stockArea", stockArea)
                bundle.putSerializable("storageRack", storageRack)
                bundle.putSerializable("stockPos", stockPos)
                showForResult(Stock_GroupDialogActivity::class.java, SEL_STOCK, bundle)
            }
            R.id.btn_containerSel -> { // 选择容器
                smqFlag = '2'
                bundle = Bundle()
//                bundle.putInt("finterId", icStockCheckProcess!!.fid)
                showForResult(Container_DialogActivity::class.java, SEL_CONTAINER, bundle)
            }
            R.id.btn_mtlSel -> { // 选择物料
                smqFlag = '3'
                bundle = Bundle()
                if(!checkSaoMa()) return
                val strMtlId = StringBuffer()
                val listMtlId = ArrayList<Int>() // 用于判断不重复插入
                checkDatas.forEach {
                    if(!listMtlId.contains(it.mtlId)) {
                        listMtlId.add(it.mtlId)
                    }
                }
                listMtlId.forEach { strMtlId.append(it.toString()+",") }

                if(strMtlId.length > 0) { // 删除最后一个，
                    strMtlId.delete(strMtlId.length-1, strMtlId.length)
                }
                bundle.putInt("isICInvBackUp", 1) // 是否查询盘点的物料
                bundle.putInt("stockId", if(stock != null) stock!!.id else 0 ) // 盘点的仓库
                bundle.putInt("stockAreaId", if(stockArea != null) stockArea!!.id else 0) // 盘点的库区
                bundle.putInt("storageRackId", if(storageRack != null) storageRack!!.id else 0) // 盘点的货架id
                bundle.putInt("stockPosId", if(stockPos != null) stockPos!!.id else 0) // 盘点的库位id
                bundle.putString("strMtlId", strMtlId.toString()) // 当前行的物料id
                showForResult(Mtl_MoreDialogActivity::class.java, SEL_MTL, bundle)
            }
            R.id.btn_positionScan -> { // 调用摄像头扫描（仓库位置）
                if(!checkSave()) return
                smqFlag = '1'
//                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            }
            R.id.btn_containerScan -> { // 调用摄像头扫描（容器）
                if(!checkSave()) return
                smqFlag = '2'
//                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            }
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                smqFlag = '3'
                if(!checkSaoMa()) return
//                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
            }
            R.id.tv_positionName -> { // 位置点击
                smqFlag = '1'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.tv_containerName -> { // 容器点击
                smqFlag = '2'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.tv_mtlName -> { // 物料点击
                smqFlag = '3'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.btn_save -> { // 保存
                if(!checkSave()) return
                if(checkDatas.size == 0) {
                    Comm.showWarnDialog(mContext,"请选择物料或者扫码条码！")
                    return
                }
                val strJson = JsonUtil.objectToString(checkDatas)
                run_modifyByRepeat(strJson)
            }
            R.id.btn_submit -> { // 提交到k3
//                if(!checkProject()) return
                run_submitTok3();
            }
            R.id.btn_clone -> { // 重置
                if (parent!!.isChange) {
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

    fun checkSaoMa() : Boolean{
//        when(smqFlag) {
//            '3' -> { // 物料扫描
//                if(stock == null) {
//                    Comm.showWarnDialog(mContext,"请扫描或选择位置！")
//                    return false
//                }
//            }
//        }
        return true
    }

    /**
     * 检查方案是否有值
     */
    fun checkSave() : Boolean {
        checkDatas.forEachIndexed { index, it ->
            if(it.stock == null) {
                Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，请扫描或者选择位置！")
                // 位置自动获取焦点，直接扫描就能匹配数据
                checkSaveSon('1', index)
                recyclerView.post(Runnable { recyclerView.smoothScrollToPosition(index) })
                return false
            }
            if(it.icItem.useContainer.equals("Y") && it.container == null) {
                Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，请扫描或者选择容器！")
                // 容器自动获取焦点，直接扫描就能匹配数据
                checkSaveSon('2', index)
                recyclerView.post(Runnable { recyclerView.smoothScrollToPosition(index) })
                return false
            }
//            if(it.repeatQty == 0.0) {
//                Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，请输入复盘数！")
//                return false
//            }
            if(it.icItem.batchManager.equals("Y") && Comm.isNULLS(it.fbatchNo).length == 0) {
                Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，请长按数字框输入批次！")
                return false
            }
        }
        return true
    }

    /**
     * 自动填充焦点，和选中行
     */
    fun checkSaveSon(flag : Char, index : Int) {
        // 容器自动获取焦点，直接扫描就能匹配数据
        smqFlag = flag
        curPos = index
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        // 自动选中行
        checkDatas.forEach {
            it.isCheck = false
        }
        checkDatas[index].isCheck = true
        mAdapter!!.notifyDataSetChanged()
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_positionCode -> setFocusable(et_positionCode)
                R.id.et_code -> setFocusable(et_code)
                R.id.et_containerCode -> setFocusable(et_containerCode)
            }
        }
        et_positionCode!!.setOnClickListener(click)
        et_code!!.setOnClickListener(click)
        et_containerCode!!.setOnClickListener(click)

        // 位置---数据变化
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
        // 位置---长按输入条码
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

        // 容器---数据变化
        et_containerCode!!.addTextChangedListener(object : TextWatcher {
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
        // 容器---长按输入条码
        et_containerCode!!.setOnLongClickListener {
            smqFlag = '2'
            showInputDialog("输入条码号", "", "none", WRITE_CODE)
            true
        }
        // 容器---焦点改变
        et_containerCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                lin_focusContainer.setBackgroundResource(R.drawable.back_style_red_focus)

            } else {
                if (lin_focusContainer != null) {
                    lin_focusContainer!!.setBackgroundResource(R.drawable.back_style_gray4)
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
                    smqFlag = '3'
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 物料---长按输入条码
        et_code!!.setOnLongClickListener {
            smqFlag = '3'
            showInputDialog("输入条码号", "", "none", WRITE_CODE)
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
            relative_stockGroup.visibility = View.VISIBLE
            mHandler.postDelayed(Runnable {
                relative_stockGroup.visibility = View.GONE
            },5000)
            true
        }
    }

    /**
     * 查询历史记录
     */
    fun findFun() {
        if(parent!!.isChange) {
            Comm.showWarnDialog(mContext,"请先保存当前数据！")
            return;
        }
        run_findListByParamWms()
    }

    fun resetStockGroup() {
        stock = null
        stockArea = null
        storageRack = null
        stockPos = null
    }

    /**
     * 得到仓库组
     */
    fun getStockGroup(msgObj : String?) {
        tv_stockName.text = "仓库："
        tv_stockAreaName.text = "库区："
        tv_storageRackName.text = "货架："
        tv_stockPosName.text = "库位："
        tv_stockAreaName.visibility = View.INVISIBLE
        tv_storageRackName.visibility = View.INVISIBLE
        tv_stockPosName.visibility = View.INVISIBLE

        if(msgObj != null) {
            resetStockGroup()

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
            tv_stockName.text = Html.fromHtml("仓库：<font color='#6a5acd'>"+stock!!.stockName+"</font>")
            tv_positionName.text = stock!!.stockName
        }
        if(stockArea != null ) {
            tv_stockAreaName.visibility = View.VISIBLE
            tv_stockAreaName.text = Html.fromHtml("库区：<font color='#6a5acd'>"+stockArea!!.fname+"</font>")
            tv_positionName.text = stockArea!!.fname
        }
        if(storageRack != null ) {
            tv_storageRackName.visibility = View.VISIBLE
            tv_storageRackName.text = Html.fromHtml("货架：<font color='#6a5acd'>"+storageRack!!.fnumber+"</font>")
            tv_positionName.text = storageRack!!.fnumber
        }
        if(stockPos != null ) {
            tv_stockPosName.visibility = View.VISIBLE
            tv_stockPosName.text = Html.fromHtml("库位：<font color='#6a5acd'>"+stockPos!!.stockPositionName+"</font>")
            tv_positionName.text = stockPos!!.stockPositionName
        }

        // 人为替换仓库信息
        if(checkDatas.size > 0 && curPos > -1) {
            checkDatas[curPos].stock = null
            checkDatas[curPos].stockArea = null
            checkDatas[curPos].storageRack = null
            checkDatas[curPos].stockPos = null

            if(stock != null) {
                checkDatas[curPos].stock = stock
                checkDatas[curPos].stockId = stock!!.fitemId
                checkDatas[curPos].stockName = stock!!.fname
                checkDatas[curPos].stockId_wms = stock!!.id
            }
            if(stockArea != null) {
                checkDatas[curPos].stockArea = stockArea
                checkDatas[curPos].stockAreaId_wms = stockArea!!.id
            }
            if(storageRack != null) {
                checkDatas[curPos].storageRack = storageRack
                checkDatas[curPos].storageRackId_wms = storageRack!!.id
            }
            if(stockPos != null) {
                checkDatas[curPos].stockPos = stockPos
                checkDatas[curPos].stockPosId = stockPos!!.fitemId
                checkDatas[curPos].stockPosId_wms = stockPos!!.id
            }
            mAdapter!!.notifyDataSetChanged()
        }

        // 自动跳到物料焦点
        smqFlag = '3'
        mHandler.sendEmptyMessage(SETFOCUS)
    }

    /**
     * 得到容器
     */
    fun getContainer(m : Container) {
        container = m
        tv_containerName.text = m!!.fnumber
        if(checkDatas.size > 0 && checkDatas[curPos].icItem.useContainer.equals("Y")) {
            checkDatas[curPos].container = m
            checkDatas[curPos].containerId = m.id
            mAdapter!!.notifyDataSetChanged()
        }
    }

    private fun reset() {
        setEnables(et_code, R.color.transparent, true)
        if(stock != null) {
            smqFlag = '3'
        } else {
            smqFlag = '1'
        }
        et_code!!.setText("")
        tv_mtlName.text = ""
        mapId.clear()
        parent!!.isChange = false
        checkDatas.clear()
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)

        mAdapter!!.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SEL_STOCK -> {// 仓库	返回
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
//                    if(checkDatas.size == 0) {
//                        run_okhttpDatas(null)
//                    }
                }
                SEL_CONTAINER -> {//查询容器	返回
                    val container = data!!.getSerializableExtra("obj") as Container
                    getContainer(container)
                }
                SEL_MTL -> {//查询物料	返回
                    val list = data!!.getSerializableExtra("obj") as List<ICItem>
                    val strIcItemId = StringBuffer()
                    list.forEach {
                        strIcItemId.append(it.fitemid.toString() + ",")
                    }
                    // 删除最后一个，
                    if (strIcItemId.length > 0) {
                        strIcItemId.delete(strIcItemId.length - 1, strIcItemId.length)
                    }
                    run_okhttpDatas(strIcItemId.toString())
//                    getMtlAfter(list,1)
                }
                BaseFragment.CAMERA_SCAN -> {// 扫一扫成功  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val code = bundle.getString(BaseFragment.DECODED_CONTENT_KEY, "")
                        when (smqFlag) {
                            '1' -> setTexts(et_positionCode, code)
                            '2' -> setTexts(et_containerCode, code)
                            '3' -> setTexts(et_code, code)
                        }
                    }
                }
                WRITE_CODE -> {// 输入条码返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        when (smqFlag) {
                            '1' -> setTexts(et_positionCode, value.toUpperCase())
                            '2' -> setTexts(et_containerCode, value.toUpperCase())
                            '3' -> setTexts(et_code, value.toUpperCase())
                        }
                    }
                }
                RESULT_WEIGHT -> { // 重量
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        checkDatas[curPos].weight = num
                        curPos = -1
                        mAdapter!!.notifyDataSetChanged()
                    }
                }
                RESULT_NUM -> { // 数量
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        checkDatas[curPos].repeatQty = num
                        curPos = -1
                        mAdapter!!.notifyDataSetChanged()
                    }
                }
                RESULT_MINPACK -> { // 最小包装数
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        checkDatas[curPos].minPackQty = num
                        curPos = -1
                        mAdapter!!.notifyDataSetChanged()
                    }
                }
                RESULT_BATCH -> { // 批次号
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        checkDatas[curPos].fbatchNo = value
                        curPos = -1
                        mAdapter!!.notifyDataSetChanged()
                    }
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 300)
    }

    /**
     * 得到扫码或选择数据
     */
    private fun getMtlAfter(list: List<ICItem>, flag: Int) {
//        parent!!.isChange = true
//        // 循环判断业务
//        for (icItem in list) {
//            // 填充数据
//            val size = checkDatas.size
//            var addRow = true
//            var curPosition = 0
//            for (i in 0 until size) {
//                val sr = checkDatas.get(i)
//                // 有相同的，就不新增了
//                if (sr.fitemid == icItem.fitemid) {
//                    curPosition = i
//                    addRow = false
//                    break
//                }
//            }
//            if (addRow) {
//                // 全部清空
//                icItem.stock = null
//                icItem.stockArea = null
//                icItem.storageRack = null
//                icItem.stockPos = null
//                icItem.container = null
//
//                if(stock != null) {
//                    icItem.stock = stock
//                }
//                if(stockArea != null) {
//                    icItem.stockArea = stockArea
//                }
//                if(storageRack != null) {
//                    icItem.storageRack = storageRack
//                }
//                if(stockPos != null) {
//                    icItem.stockPos = stockPos
//                }
//                if(icItem.useContainer.equals("Y") && container != null) {
//                    icItem.container = container
//                }
//                if(icItem.batchManager.equals("Y")) { // 启用了批次号，就给个默认值
//                    icItem.batchNo = Comm.getSysDate(3)
//                }
//                icItem.isCheck = false
//                icItem.realQty = 1.0
//                checkDatas.add(icItem)
//
//            } else {
//                // 已有相同物料行，就叠加数量
//                val fqty = checkDatas[curPosition].realQty
//                val addVal = BigdecimalUtil.add(fqty, 1.0);
//                checkDatas[curPosition].realQty = addVal
//            }
//        }
//
//        if(flag == 1) {
//            checkDatas.forEach {
//                it.isCheck = false
//            }
//            mAdapter!!.notifyDataSetChanged()
//            recyclerView.post(Runnable { recyclerView.smoothScrollToPosition(checkDatas.size - 1) })
//        }
//        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * 扫码查询对应的方法
     */
    private fun run_okhttpDatas(strIcItemId : String?) {
        showLoadDialog("加载中...", false)
        var mUrl:String? = null
        var barcode:String? = null

        when(smqFlag) {
            '1' -> {
                mUrl = getURL("stockPosition/findBarcodeGroup")
                barcode = getValues(et_positionCode)
            }
            '2' -> {
                mUrl = getURL("container/findBarcode")
                barcode = getValues(et_containerCode)
            }
            '3' -> {
                mUrl = getURL("icInvBackup/findBarcodeList")
                barcode = getValues(et_code)

            }
        }
        val formBody = FormBody.Builder()
                .add("barcode", barcode)
                .add("strIcItemId", if(smqFlag == '3' && strIcItemId != null) strIcItemId else "" ) // 盘点扫码查询物料用的
                .add("stockId", if(smqFlag == '3' && stock!=null) stock!!.id.toString() else "")
                .add("stockAreaId", if(smqFlag == '3' && stockArea!=null) stockArea!!.id.toString() else "")
                .add("storageRackId", if(smqFlag == '3' && storageRack!=null) storageRack!!.id.toString() else "")
                .add("stockPositionId", if(smqFlag == '3' && stockPos!=null) stockPos!!.id.toString() else "")
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
                LogUtil.e("run_okhttpDatas --> onResponse", result)
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
     * 修改
     */
    private fun run_modifyByRepeat(strJson : String) {
        showLoadDialog("保存中...", false)

        val mUrl = getURL("icInvBackup/modifyByRepeat")
        val formBody = FormBody.Builder()
                .add("strJson", strJson)
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
     * 提交
     */
    private fun run_submitTok3() {
        showLoadDialog("提交中...", false)

        getUserInfo()
        val mUrl = getURL("icInvBackup/submitTok3")
        val mJson = JsonUtil.objectToString(checkDatas)
        val formBody = FormBody.Builder()
                .add("strJson", mJson)
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
                mHandler.sendEmptyMessage(UNSUBMIT)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUBMIT, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUBMIT, result)
                LogUtil.e("run_submitTok3 --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 历史查询
     */
    private fun run_findListByParamWms() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("icInvBackup/findListByParamWms")
        val formBody = FormBody.Builder()
                .add("finterId", if (icStockCheckProcess != null) icStockCheckProcess!!.fid.toString() else "") // 方案id
                .add("toK3", "1")
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