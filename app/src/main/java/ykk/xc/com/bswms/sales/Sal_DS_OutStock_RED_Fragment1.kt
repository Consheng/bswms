package ykk.xc.com.bswms.sales

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import kotlinx.android.synthetic.main.sal_ds_outstock_red_fragment1.*
import kotlinx.android.synthetic.main.sal_ds_outstock_red_main.*
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.EventBusEntity
import ykk.xc.com.bswms.bean.ICStockBill
import ykk.xc.com.bswms.bean.User
import ykk.xc.com.bswms.bean.k3Bean.Emp
import ykk.xc.com.bswms.bean.k3Bean.SeOrderEntry
import ykk.xc.com.bswms.comm.BaseFragment
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.JsonUtil
import ykk.xc.com.bswms.util.LogUtil
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：销售退货
 * 作者：ykk
 */
class Sal_DS_OutStock_RED_Fragment1 : BaseFragment() {

    companion object {
        private val SEL_EMP1 = 62
        private val SEL_EMP2 = 63
        private val SEL_EMP3 = 64
        private val SEL_EMP4 = 65
        private val SAVE = 201
        private val UNSAVE = 501
        private val FIND_SOURCE = 202
        private val UNFIND_SOURCE = 502
        private val MODIFY_STATUS = 203
        private val UNMODIFY_STATUS = 503
        private val FIND_ICSTOCKBILL = 204
        private val UNFIND_ICSTOCKBILL = 504

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val WRITE_CODE = 3
    }

    private val context = this
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private var parent: Sal_DS_OutStock_RED_MainActivity? = null
    private val df = DecimalFormat("#.###")
    private var timesTamp:String? = null // 时间戳
    var icStockBill = ICStockBill() // 保存的对象
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    //    var isReset = false // 是否点击了重置按钮.
    var seOrderEntryList:List<SeOrderEntry>? = null
    private var icStockBillId = 0 // 上个页面传来的id
    private var refundType = 1 // 扫描类型1：退货单号，2：物料条码

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Sal_DS_OutStock_RED_Fragment1) : Handler() {
        private val mActivity: WeakReference<Sal_DS_OutStock_RED_Fragment1>

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
                    SAVE -> {// 保存成功 进入
                        val strId_pdaNo = JsonUtil.strToString(msgObj)
                        if(m.icStockBill.id == 0) {
                            val arr = strId_pdaNo.split(":") // id和pdaNo数据拼接（1:IC201912121）
                            m.icStockBill.id = m.parseInt(arr[0])
                            m.icStockBill.pdaNo = arr[1]
                            m.tv_pdaNo.text = arr[1]
                        }
                        m.parent!!.isMainSave = true
                        m.parent!!.viewPager.setScanScroll(true); // 放开左右滑动
                        m.toasts("保存成功✔")
//                        m.lin_expressNo.visibility = View.GONE  // 把退货单号隐藏
                        m.et_expressCode.isEnabled = false
                        m.btn_scan.isEnabled = false
                        m.btn_scan.visibility = View.GONE
                        m.setEnables(m.lin_focusNo, R.drawable.back_style_gray1c, false)
                        // 滑动第二个页面
                        m.parent!!.viewPager!!.setCurrentItem(1, false)
                        m.parent!!.isChange = if(m.icStockBillId == 0) true else false
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    FIND_SOURCE ->{ // 查询源单 返回
                        val list = JsonUtil.strToList(msgObj, SeOrderEntry::class.java)
                        m.seOrderEntryList = list
                        m.icStockBill.fcustId = list[0].seOrder.cust.fitemId
                        m.icStockBill.fdeptId = list[0].seOrder.fdeptId
                        if(m.refundType == 1) {
                            m.icStockBill.expressNo = m.getValues(m.et_expressCode)
                            m.icStockBill.expressCompany = m.isNULLS(list[0].expressCompany)
                        } else {
                            m.icStockBill.expressNo = ""
                            m.icStockBill.expressCompany = ""
                        }
                        m.tv_custSel.text = list[0].seOrder.cust.fname
                        m.tv_deptSel.text = list[0].seOrder.department.departmentName
                        m.isTextChange = false
                    }
                    UNFIND_SOURCE ->{ // 查询源单失败！ 返回
                        m.isTextChange = false
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "扫描的快递单不正确，请检查！！！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    FIND_ICSTOCKBILL -> { // 查询主表信息 成功
                        val icsBill = JsonUtil.strToObject(msgObj, ICStockBill::class.java)
                        m.setICStockBill(icsBill)
                    }
                    UNFIND_ICSTOCKBILL -> { // 查询主表信息 失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "查询信息有错误！2秒后自动关闭..."
                        Comm.showWarnDialog(m.mContext, errMsg)
                        m.mHandler.postDelayed(Runnable {
                            m.mContext!!.finish()
                        },2000)
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        m.setFocusable(m.et_expressCode)
                    }
                    SAOMA -> { // 扫码之后
                        m.run_seOrderEntryList()
                    }
                }
            }
        }
    }

    fun setICStockBill(m : ICStockBill) {
        icStockBill.id = m.id
        icStockBill.pdaNo = m.pdaNo
        icStockBill.fdate = m.fdate
        icStockBill.fsupplyId = m.fsupplyId
        icStockBill.fdeptId = m.fdeptId
        icStockBill.fempId = m.fempId
        icStockBill.fsmanagerId = m.fsmanagerId
        icStockBill.fmanagerId = m.fmanagerId
        icStockBill.ffmanagerId = m.ffmanagerId
        icStockBill.fbillerId = m.fbillerId
        icStockBill.fselTranType = m.fselTranType

        icStockBill.yewuMan = m.yewuMan          // 业务员
        icStockBill.baoguanMan = m.baoguanMan          // 保管人
        icStockBill.fuzheMan = m.fuzheMan           // 负责人
        icStockBill.yanshouMan = m.yanshouMan            // 验收人
        icStockBill.createUserId = m.createUserId        // 创建人id
        icStockBill.createUserName = m.createUserName        // 创建人
        icStockBill.createDate = m.createDate            // 创建日期
        icStockBill.isToK3 = m.isToK3                   // 是否提交到K3
        icStockBill.k3Number = m.k3Number                // k3返回的单号
        icStockBill.unQualifiedStockId = m.unQualifiedStockId       // 不合格仓库id
        icStockBill.missionBillId = m.missionBillId
        icStockBill.fcustId = m.fcustId
        icStockBill.expressNo = m.expressNo
        icStockBill.expressCompany = m.expressCompany

        icStockBill.supplier = m.supplier
        icStockBill.cust = m.cust
        icStockBill.department = m.department
        icStockBill.unQualifiedStock = m.unQualifiedStock

        if(m.cust != null) {
            tv_custSel.text = m.cust.fname
        }
        if(m.department != null) {
            tv_deptSel.text = m.department.departmentName
        }
        tv_pdaNo.text = m.pdaNo
        tv_inDateSel.text = m.fdate
        isTextChange = true
        if(isNULLS(m.expressNo).length == 0) {
            lin_expressNo.visibility = View.GONE
        } else {
            lin_expressNo.visibility = View.VISIBLE
        }
        et_expressCode.setText(m.expressNo)
        isTextChange = false
        tv_emp1Sel.text = m.yewuMan
        tv_emp2Sel.text = m.baoguanMan
        tv_emp3Sel.text = m.fuzheMan
        tv_emp4Sel.text = m.yanshouMan

        parent!!.isChange = false
        parent!!.isMainSave = true
        parent!!.viewPager.setScanScroll(true); // 放开左右滑动
        EventBus.getDefault().post(EventBusEntity(12)) // 发送指令到fragment3，查询分类信息
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.sal_ds_outstock_red_fragment1, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Sal_DS_OutStock_RED_MainActivity
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
        tv_inDateSel.text = Comm.getSysDate(7)
        hideSoftInputMode(mContext, et_expressCode)

        tv_operationManName.text = user!!.erpUserName
        tv_emp1Sel.text = user!!.empName
        tv_emp2Sel.text = user!!.empName
        tv_emp3Sel.text = user!!.empName
        tv_emp4Sel.text = user!!.empName

        icStockBill.billType = "DS_XSCK_BTOR" // 电商销售退货
        icStockBill.ftranType = 21
        icStockBill.frob = -1
        icStockBill.fempId = user!!.empId
        icStockBill.yewuMan = user!!.empName
        icStockBill.fsmanagerId = user!!.empId
        icStockBill.baoguanMan = user!!.empName
        icStockBill.fmanagerId = user!!.empId
        icStockBill.fuzheMan = user!!.empName
        icStockBill.ffmanagerId = user!!.empId
        icStockBill.yanshouMan = user!!.empName
        icStockBill.fbillerId = user!!.erpUserId
        icStockBill.createUserId = user!!.id
        icStockBill.createUserName = user!!.username

        bundle()
    }

    fun bundle() {
        val bundle = mContext!!.intent.extras
        if(bundle != null) {
            if(bundle.containsKey("id")) { // 查询过来的
                lin_refundType.visibility = View.GONE
                et_expressCode.isEnabled = false
                btn_scan.isEnabled = false
                btn_scan.visibility = View.GONE
                setEnables(lin_focusNo, R.drawable.back_style_gray1c, false)
//                lin_expressNo.visibility = View.GONE
                icStockBillId = bundle.getInt("id") // ICStockBill主表id
                // 查询主表信息
                run_findStockBill(icStockBillId)
            } else {
                lin_refundType.visibility = View.VISIBLE
//                lin_expressNo.visibility = View.VISIBLE
                et_expressCode.isEnabled = true
                btn_scan.isEnabled = true
                btn_scan.visibility = View.VISIBLE
                setEnables(lin_focusNo, R.drawable.back_style_blue2, true)
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
    }

    @OnClick(R.id.tv_inDateSel, R.id.btn_save, R.id.btn_clone, R.id.btn_scan, R.id.cb_expressNo, R.id.cb_barcode)
    fun onViewClicked(view: View) {
        var bundle: Bundle? = null
        when (view.id) {
            R.id.tv_inDateSel -> { // 选择日期
                Comm.showDateDialog(mContext, tv_inDateSel, 0)
            }
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                ScanUtil.startScan(mContext, 10001, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create())
            }
            R.id.btn_save -> { // 保存
                if(!checkSave(true)) return
                run_save()
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

    /**
     * 保存检查数据判断
     */
    fun checkSave(isHint :Boolean) : Boolean {
//        if (icStockBill.fsupplyId == 0) {
//            Comm.showWarnDialog(mContext, "请选择供应商！")
//            return false;
//        }
        if(icStockBill.id == 0 && seOrderEntryList == null) {
            if(isHint) Comm.showWarnDialog(mContext, "请扫描未退货的快递单！")
            return false
        }
        if(icStockBill.fsmanagerId == 0) {
            if(isHint) Comm.showWarnDialog(mContext, "请选择保管人！")
            return false
        }
        if(icStockBill.ffmanagerId == 0) {
            if(isHint) Comm.showWarnDialog(mContext, "请选择验收人！")
            return false
        }
        return true;
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_expressCode -> setFocusable(et_expressCode)
            }
        }
        et_expressCode!!.setOnClickListener(click)

        // 退货单号---数据变化
        et_expressCode!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                if (!isTextChange) {
                    isTextChange = true
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 退货单号---长按输入条码
        et_expressCode!!.setOnLongClickListener {
            showInputDialog("客户出库单号", "", "none", WRITE_CODE)
            true
        }

        cb_expressNo.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                refundType = 1
                tv_sourceTitle.text = "退货单号"
                et_expressCode.setHint("请扫描退回的快递单号")
                cb_barcode.isChecked = false
                mHandler.sendEmptyMessageDelayed(SETFOCUS,200)
            }
        }
        cb_barcode.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                refundType = 2
                tv_sourceTitle.text = "出库条码"
                et_expressCode.setHint("请扫描出库使用的条码")
                cb_expressNo.isChecked = false
                mHandler.sendEmptyMessageDelayed(SETFOCUS,200)
            }
        }
    }

    fun reset() {
        lin_refundType.visibility = View.VISIBLE
        lin_expressNo.visibility = View.VISIBLE
        et_expressCode.isEnabled = true
        btn_scan.isEnabled = true
        btn_scan.visibility = View.VISIBLE
        setEnables(lin_focusNo, R.drawable.back_style_blue2, true)
        parent!!.isMainSave = false
        parent!!.viewPager.setScanScroll(false) // 禁止滑动
        tv_pdaNo.text = ""
        tv_inDateSel.text = Comm.getSysDate(7)
        et_expressCode.setText("")
        tv_custSel.text = ""
        tv_deptSel.text = ""
        icStockBill.id = 0
        icStockBill.fselTranType = 0
        icStockBill.pdaNo = ""
        icStockBill.fsupplyId = 0
        icStockBill.fdeptId = 0
        icStockBill.expressNo = ""
        icStockBill.expressCompany = ""
//        icStockBill.fempId = 0
//        icStockBill.fsmanagerId = 0
//        icStockBill.fmanagerId = 0
//        icStockBill.ffmanagerId = 0
//        icStockBill.yewuMan = ""
//        icStockBill.baoguanMan = ""
//        icStockBill.fuzheMan = ""
//        icStockBill.yanshouMan = ""

        icStockBillId = 0
        icStockBill.supplier = null
        icStockBill.cust = null
        icStockBill.department = null
        seOrderEntryList = null
        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
        parent!!.isChange = false
        EventBus.getDefault().post(EventBusEntity(11)) // 发送指令到fragment2，告其清空
        mHandler.sendEmptyMessageDelayed(SETFOCUS,200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SEL_EMP1 -> {//查询业务员	返回
                    val emp = data!!.getSerializableExtra("obj") as Emp
                    tv_emp1Sel.text = emp!!.fname
                    icStockBill.fempId = emp.fitemId
                    icStockBill.yewuMan = emp.fname
                }
                SEL_EMP2 -> {//查询保管人	返回
                    val emp = data!!.getSerializableExtra("obj") as Emp
                    tv_emp2Sel.text = emp!!.fname
                    icStockBill.fsmanagerId = emp.fitemId
                    icStockBill.baoguanMan = emp.fname
                }
                SEL_EMP3 -> {//查询负责人	返回
                    val emp = data!!.getSerializableExtra("obj") as Emp
                    tv_emp3Sel.text = emp!!.fname
                    icStockBill.fmanagerId = emp.fitemId
                    icStockBill.fuzheMan = emp.fname
                }
                SEL_EMP4 -> {//查询验收人	返回
                    val emp = data!!.getSerializableExtra("obj") as Emp
                    tv_emp4Sel.text = emp!!.fname
                    icStockBill.ffmanagerId = emp.fitemId
                    icStockBill.yanshouMan = emp.fname
                }
                WRITE_CODE -> {// 输入条码  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                    }
                }
            }
        }
        // 是否可以自动保存
        if(checkSave(false)) run_save()
    }

    /**
     * 调用华为扫码接口，返回的值
     */
    fun getScanData(barcode :String) {
        setTexts(et_expressCode, barcode)
    }

    /**
     * 保存
     */
    private fun run_save() {
        icStockBill.fdate = getValues(tv_inDateSel)

        showLoadDialog("保存中...", false)
        val mUrl = getURL("stockBill_WMS/save")

        val mJson = JsonUtil.objectToString(icStockBill)
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
     * 根据快递单查询销售订单
     */
    private fun run_seOrderEntryList() {
        showLoadDialog("保存中...", false)
        val mUrl = getURL("seOrder/findExpressNoByBTOR")

        val formBody = FormBody.Builder()
                .add("expressNo", getValues(et_expressCode))
                .add("refundType", refundType.toString()) // 退货类型
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNFIND_SOURCE)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNFIND_SOURCE, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(FIND_SOURCE, result)
                LogUtil.e("run_save --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 修改任务单状态，和接单人
     */
    private fun run_missionBillModifyStatus(id: Int) {
        val mUrl = getURL("missionBill/modifyStatus")

        val formBody = FormBody.Builder()
                .add("id", id.toString())
                .add("receiveUserId", user!!.id.toString())
                .add("missionStatus", "D")
                .add("missionStartTime", "1")
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNMODIFY_STATUS)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNMODIFY_STATUS, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(MODIFY_STATUS, result)
                LogUtil.e("run_missionBillModifyStatus --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     *  查询主表信息
     */
    private fun run_findStockBill(id: Int) {
        val mUrl = getURL("stockBill_WMS/findStockBill")

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
                mHandler.sendEmptyMessage(UNFIND_ICSTOCKBILL)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNFIND_ICSTOCKBILL, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(FIND_ICSTOCKBILL, result)
                LogUtil.e("run_missionBillModifyStatus --> onResponse", result)
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