package ykk.xc.com.bswms.produce

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import kotlinx.android.synthetic.main.prod_report2_fragment1.*
import okhttp3.*
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.basics.ICitemClasses_DialogActivity
import ykk.xc.com.bswms.basics.ICitemStructure_DialogActivity
import ykk.xc.com.bswms.basics.ICitemStyle_DialogActivity
import ykk.xc.com.bswms.bean.*
import ykk.xc.com.bswms.comm.BaseFragment
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.produce.adapter.Prod_Report2_Fragment1Adapter
import ykk.xc.com.bswms.util.JsonUtil
import ykk.xc.com.bswms.util.LogUtil
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：完工汇报
 * 作者：ykk
 */
class Prod_Report2_Fragment1 : BaseFragment() {

    companion object {
        private val SEL_CLASSES = 60
        private val SEL_STYLE = 61
        private val SEL_STRUCTURE = 62
        private val SEL_PROCEDURE = 63

        private val SAVE = 200
        private val UNSAVE = 500
        private val SUCC1 = 201
        private val UNSUCC1 = 501
        private val FIND_QTY = 202
        private val UNFIND_QTY = 502

        private val RESULT_NUM = 1
    }
    private val context = this
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private val df = DecimalFormat("#.######")
    private var parent: Prod_Report_MainActivity? = null
    private var mAdapter: Prod_Report2_Fragment1Adapter? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var timesTamp:String? = null // 时间戳
    private var smqFlag = '1' // 扫描类型1：物料扫描
    private var curPos:Int = -1 // 当前行
    private val checkDatas = ArrayList<ProdReportSel>()
    private var bt :BarCodeTable? = null // 条码表对象
    private val prodReportSel = ProdReportSel()


    // 消息处理
    private val mHandler = MyHandler(this)
    private class MyHandler(activity: Prod_Report2_Fragment1) : Handler() {
        private val mActivity: WeakReference<Prod_Report2_Fragment1>

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
                    SUCC1 -> { // 查询成功 进入
                        val list = JsonUtil.strToList(msgObj, ProdReportSel::class.java)
                        m.checkDatas.clear()
                        m.checkDatas.addAll(list)
                        m.mAdapter!!.notifyDataSetChanged()
                        m.run_findSumQty()
                    }
                    UNSUCC1 -> { // 查询  失败
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
                        m.tv_fqty.text = ""
                        m.prodReportSel.fqty = 0.0
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
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.prod_report2_fragment1, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Prod_Report_MainActivity

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = Prod_Report2_Fragment1Adapter(mContext!!, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : Prod_Report2_Fragment1Adapter.MyCallBack {
            //            override fun onModify(entity: ProdReportSel, position: Int) {
//                EventBus.getDefault().post(EventBusEntity(31, entity))
//                // 滑动第二个页面
//                parent!!.viewPager!!.setCurrentItem(1, false)
//            }
            override fun onDelete(entity: ProdReportSel, position: Int) {
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
        tv_begDateSel.text = Comm.getSysDate(7)
        tv_endDateSel.text = Comm.getSysDate(7)

        prodReportSel.userId = user!!.id
        prodReportSel.procedureId = 0
        prodReportSel.deptId = user!!.deptId
        prodReportSel.classesId = 0
        prodReportSel.classesName = ""
        prodReportSel.styleId = 0
        prodReportSel.styleName = ""
        prodReportSel.structureId = 0
        prodReportSel.structureName = ""
        prodReportSel.fqty = 0.0

        run_findSumQty()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
    }

    @OnClick(R.id.tv_classesSel, R.id.tv_styleSel, R.id.tv_structureSel, R.id.tv_procedureSel, R.id.tv_fqty, R.id.btn_save, R.id.tv_begDateSel, R.id.tv_endDateSel, R.id.btn_search)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.tv_classesSel -> { // 选择 产品类别
                showForResult(ICitemClasses_DialogActivity::class.java, SEL_CLASSES, null)
            }
            R.id.tv_styleSel -> { // 选择 产品款式
                showForResult(ICitemStyle_DialogActivity::class.java, SEL_STYLE, null)
            }
            R.id.tv_structureSel -> { // 选择 产品结构
                showForResult(ICitemStructure_DialogActivity::class.java, SEL_STRUCTURE, null)
            }
            R.id.tv_procedureSel -> { // 选择 工序
                showForResult(Procedure_Sel_DialogActivity::class.java, SEL_PROCEDURE, null)
            }
            R.id.tv_fqty -> { // 选择 数量
                showInputDialog("报工数量", "", "0.0", RESULT_NUM)
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
//                if(getValues(tv_classesSel).length == 0) {
//                    Comm.showWarnDialog(mContext,"请选择（产品类别）！")
//                    return
//                }
//                if(getValues(tv_styleSel).length == 0) {
//                    Comm.showWarnDialog(mContext,"请选择（产品款式）！")
//                    return
//                }
//                if(getValues(tv_structureSel).length == 0) {
//                    Comm.showWarnDialog(mContext,"请选择（产品结构）！")
//                    return
//                }
//                if(getValues(tv_procedureSel).length == 0) {
//                    Comm.showWarnDialog(mContext,"请选择（工序）！")
//                    return
//                }
//                if(prodReportSel.fqty == 0.0) {
//                    Comm.showWarnDialog(mContext,"请输入（报工数量），且大于0！")
//                    return
//                }
                if(autoSel()) {
                    run_save()
                }
            }

        }
    }

    override fun setListener() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SEL_CLASSES -> {//查询产品类别    返回
                    val m = data!!.getSerializableExtra("obj") as ICItemClasses
                    prodReportSel.classesId = m.id
                    prodReportSel.classesName = m.fname
                    tv_classesSel.text = m!!.fname
                    autoSel()
                }
                SEL_STYLE -> {//查询产品款式    返回
                    val m = data!!.getSerializableExtra("obj") as ICItemStyle
                    prodReportSel.styleId = m.id
                    prodReportSel.styleName = m.fname
                    tv_styleSel.text = m!!.fname
                    autoSel()
                }
                SEL_STRUCTURE -> {//查询产品结构    返回
                    val m = data!!.getSerializableExtra("obj") as ICItemStructure
                    prodReportSel.structureId = m.id
                    prodReportSel.structureName = m.fname
                    tv_structureSel.text = m!!.fname
                    autoSel()
                }
                SEL_PROCEDURE -> {
                    val procedure = data!!.getSerializableExtra("obj") as Procedure
                    prodReportSel.procedureId = procedure.id
                    tv_procedureSel.text = procedure.procedureName
                    autoSel()
                }
                RESULT_NUM -> { // 数量	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        tv_fqty.text = df.format(num)
                        prodReportSel.fqty = num
                    }
                }
            }
        }
    }

    private fun autoSel () :Boolean {
        var isBool = true
        if(getValues(tv_classesSel).length == 0) {
            isBool = false
            showForResult(ICitemClasses_DialogActivity::class.java, SEL_CLASSES, null)

        } else if(getValues(tv_styleSel).length == 0) {
            isBool = false
            showForResult(ICitemStyle_DialogActivity::class.java, SEL_STYLE, null)

        } else if(getValues(tv_structureSel).length == 0) {
            isBool = false
            showForResult(ICitemStructure_DialogActivity::class.java, SEL_STRUCTURE, null)

        } else if(getValues(tv_procedureSel).length == 0) {
            isBool = false
            showForResult(Procedure_Sel_DialogActivity::class.java, SEL_PROCEDURE, null)

        } else if(prodReportSel.fqty == 0.0) {
            isBool = false
            showInputDialog("报工数量", "", "0.0", RESULT_NUM)
        }
        return isBool
    }

    /**
     * 保存
     */
    private fun run_save() {
        showLoadDialog("保存中...", false)
        var mUrl = getURL("prodReportSel/add")
        var mJson = JsonUtil.objectToString(prodReportSel)
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
     * 查询
     */
    private fun run_findListByParam() {
        showLoadDialog("保存中...", true)
        var mUrl = getURL("prodReportSel/findListByParam")
        val formBody = FormBody.Builder()
                .add("begDate", getValues(tv_begDateSel))
                .add("endDate", getValues(tv_endDateSel))
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
                mHandler.sendEmptyMessage(UNSUCC1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC1, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC1, result)
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
        var mUrl = getURL("prodReportSel/findSumQty")
        val formBody = FormBody.Builder()
                .add("begDate", getValues(tv_begDateSel))
                .add("endDate", getValues(tv_endDateSel))
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
                mHandler.sendEmptyMessage(FIND_QTY)
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