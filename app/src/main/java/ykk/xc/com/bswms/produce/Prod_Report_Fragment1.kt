package ykk.xc.com.bswms.produce

import android.app.Activity
import android.app.AlertDialog
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
import kotlinx.android.synthetic.main.prod_report_fragment1.*
import okhttp3.*
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.*
import ykk.xc.com.bswms.bean.prod.ProdOrder
import ykk.xc.com.bswms.comm.BaseFragment
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.produce.adapter.Prod_Report_Fragment1Adapter
import ykk.xc.com.bswms.util.JsonUtil
import ykk.xc.com.bswms.util.LogUtil
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.io.IOException
import java.io.Serializable
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：生产汇报---扫码
 * 作者：ykk
 */
class Prod_Report_Fragment1 : BaseFragment() {

    companion object {
        private val SEL_PROCEDURE = 60

        private val SAVE = 200
        private val UNSAVE = 500
        private val SUCC1 = 201
        private val UNSUCC1 = 501
        private val SUCC2 = 202
        private val UNSUCC2 = 502
        private val FIND_QTY = 203
        private val UNFIND_QTY = 503

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val WRITE_CODE = 3
    }
    private val context = this
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private val df = DecimalFormat("#.######")
    private var parent: Prod_Report_MainActivity? = null
    private var mAdapter: Prod_Report_Fragment1Adapter? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var timesTamp:String? = null // 时间戳
    private var smqFlag = '1' // 扫描类型1：物料扫描
    private var curPos:Int = -1 // 当前行
    private val checkDatas = ArrayList<ProdReport>()
    private var bt :BarCodeTable? = null // 条码表对象


    // 消息处理
    private val mHandler = MyHandler(this)
    private class MyHandler(activity: Prod_Report_Fragment1) : Handler() {
        private val mActivity: WeakReference<Prod_Report_Fragment1>

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
                        m.bt = JsonUtil.strToObject(msgObj, BarCodeTable::class.java)
                        if(m.bt!!.listProcedure.size > 1) {
                            var bundle = Bundle()
                            bundle.putSerializable("checkDatas", m.bt!!.listProcedure as Serializable)
                            m.showForResult(Procedure_Sel_DialogActivity::class.java, SEL_PROCEDURE, bundle)
                        } else {
                            m.setProdReport(m.bt!!.listProcedure[0])
                        }
                        m.isTextChange = false
                    }
                    UNSUCC1 -> { // 扫码失败
                        m.isTextChange = false
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC2 -> { // 查询成功 进入
                        val list = JsonUtil.strToList(msgObj, ProdReport::class.java)
                        m.checkDatas.clear()
                        m.checkDatas.addAll(list)
                        m.mAdapter!!.notifyDataSetChanged()
                        m.run_findSumQty()
                    }
                    UNSUCC2 -> { // 查询  失败
                        if(m.checkDatas.size > 0) {
                            m.checkDatas.clear()
                            m.mAdapter!!.notifyDataSetChanged()
                        }
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                        m.run_findSumQty()
                    }
                    SAVE -> { // 保存成功 进入
                        m.toasts("保存成功√")
                        m.run_findSumQty()
                        /*m.tv_dateSel.text = Comm.getSysDate(7)
                        m.run_findListByParam()*/
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    FIND_QTY -> { // 查询报工总数 进入
                        val fqty = JsonUtil.strToString(msgObj)
                        m.tv_toDayCount.text = "报工总数："+fqty
                    }
                    UNFIND_QTY -> { // 查询报工总数失败
                        m.tv_toDayCount.text = "报工总数：0"
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        when(m.smqFlag) {
                            '1'-> m.setFocusable(m.et_code)
                        }
                    }
                    SAOMA -> { // 扫码之后
                        // 执行查询方法
                        m.run_smDatas()
                    }
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.prod_report_fragment1, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Prod_Report_MainActivity

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = Prod_Report_Fragment1Adapter(mContext!!, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : Prod_Report_Fragment1Adapter.MyCallBack {
            //            override fun onModify(entity: ProdReport, position: Int) {
//                EventBus.getDefault().post(EventBusEntity(31, entity))
//                // 滑动第二个页面
//                parent!!.viewPager!!.setCurrentItem(1, false)
//            }
            override fun onDelete(entity: ProdReport, position: Int) {
//                curPos = position
                val listPrintDate = ArrayList<ExpressNoData>()
//                run_removeEntry(entity.id)
            }
        })

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            val entry = checkDatas.get(pos)

            curPos = pos
//            showInputDialog("数量", entry.fqty.toString(), "0.0", RESULT_NUM)
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
        tv_begDateSel.text = Comm.getSysDate(7)
        tv_endDateSel.text = Comm.getSysDate(7)

        run_findSumQty()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
    }

    @OnClick(R.id.btn_scan, R.id.btn_save, R.id.btn_clone, R.id.tv_begDateSel, R.id.tv_endDateSel, R.id.btn_search)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                smqFlag = '1'
                ScanUtil.startScan(mContext, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.tv_begDateSel -> { // 开始日期选择
                Comm.showDateDialog(mContext, view, 0)
            }
            R.id.tv_endDateSel -> { // 结束日期选择
                Comm.showDateDialog(mContext, view, 0)
            }
            R.id.btn_search -> { // 查询
                run_findListByParam()
            }
            R.id.btn_save -> { // 保存
                if(!checkSave()) return
//                run_save(null);
            }
            R.id.btn_clone -> { // 重置
                if (checkSaveHint()) {
                    val build = AlertDialog.Builder(mContext)
                    build.setIcon(R.drawable.caution)
                    build.setTitle("系统提示")
                    build.setMessage("您有未保存的数据，继续重置吗？")
                    build.setPositiveButton("是") { dialog, which -> reset(0) }
                    build.setNegativeButton("否", null)
                    build.setCancelable(false)
                    build.show()

                } else {
                    reset(0)
                }
            }
        }
    }

    /**
     * 检查数据
     */
    fun checkSave() : Boolean {
        if(checkDatas.size == 0) {
            Comm.showWarnDialog(mContext, "请扫描物料条码！")
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
                R.id.et_code -> setFocusable(et_code)
            }
        }
        et_code!!.setOnClickListener(click)

        // 物料---数据变化
        et_code!!.addTextChangedListener(object : TextWatcher {
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
        /*// 物料---长按输入条码
        et_code!!.setOnLongClickListener {
            smqFlag = '1'
            showInputDialog("输入条码号", getValues(et_code), "none", WRITE_CODE)
            true
        }*/
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

    }

    /**
     * 0：表示点击重置，1：表示保存后重置
     */
    private fun reset(flag : Int) {
        checkDatas.clear()
        mAdapter!!.notifyDataSetChanged()

        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
        parent!!.isChange = false
        smqFlag = '1'
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    private fun setProdReport(procedure :Procedure) {
        val prodOrder = JsonUtil.stringToObject(bt!!.relationObj, ProdOrder::class.java)
        val pr = ProdReport()
        pr.barcodeTableId = bt!!.id
        pr.userId = user!!.id
        pr.procedureId = procedure.id
        pr.fqty = 1.0
        pr.deptId = prodOrder.workShopId
        pr.icItemId = prodOrder.icItemId
        pr.icmoFinterId = prodOrder.prodId
        pr.icmoFbillNo = prodOrder.prodNo
        pr.processflowId = procedure.processflowId
        pr.reportType = 'A'
        run_save(pr)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                WRITE_CODE -> {// 输入条码  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        et_code!!.setText(value.toUpperCase())
                    }
                }
                SEL_PROCEDURE -> {
                    val procedure = data!!.getSerializableExtra("obj") as Procedure
                    setProdReport(procedure)
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
            '1' -> setTexts(et_code, barcode)
        }
    }

    /**
     * 扫码查询对应的方法
     */
    private fun run_smDatas() {
        showLoadDialog("加载中...", false)
        var mUrl:String? = null
        when(smqFlag) {
            '1' -> {
                mUrl = getURL("prodOrder/findBarcodeByReportWork")
            }
        }
        val formBody = FormBody.Builder()
                .add("barcode", getValues(et_code))
                .add("userId", user!!.id.toString())
                .add("reportType", "A") // 正常汇报类型
//                .add("deptId", user!!.deptId.toString())
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
    private fun run_save(pr :ProdReport) {
        showLoadDialog("保存中...", false)
        var mUrl = getURL("prodReport/add")
        var mJson = JsonUtil.objectToString(pr)
        val formBody = FormBody.Builder()
                .add("strJson", mJson)
                .add("empId",user!!.empId.toString())
                .add("erpUserId",user!!.erpUserId.toString())
                .add("empName",user!!.empName)
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
     * 查询
     */
    private fun run_findListByParam() {
        showLoadDialog("保存中...", true)
        var mUrl = getURL("prodReport/findListByParam")
        val formBody = FormBody.Builder()
                .add("begDate", getValues(tv_begDateSel))
                .add("endDate", getValues(tv_endDateSel))
                .add("userId", user!!.id.toString())
                .add("reportType", "A") // 正常汇报类型
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
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC2, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC2, result)
                LogUtil.e("run_findListByParam --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 查询今天汇报总数
     */
    private fun run_findSumQty() {
        showLoadDialog("保存中...", true)
        var mUrl = getURL("prodReport/findSumQty")
        val formBody = FormBody.Builder()
                .add("begDate", getValues(tv_begDateSel))
                .add("endDate", getValues(tv_endDateSel))
                .add("userId", user!!.id.toString())
                .add("reportType", "A") // 正常汇报类型
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNFIND_QTY)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNFIND_QTY, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(FIND_QTY, result)
                LogUtil.e("run_findSumQty --> onResponse", result)
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