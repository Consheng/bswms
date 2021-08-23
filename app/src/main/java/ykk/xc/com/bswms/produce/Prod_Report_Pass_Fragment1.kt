package ykk.xc.com.bswms.produce

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import kotlinx.android.synthetic.main.prod_report_pass_fragment1.*
import okhttp3.*
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.BarCodeTable
import ykk.xc.com.bswms.bean.ProdReportSel
import ykk.xc.com.bswms.bean.User
import ykk.xc.com.bswms.comm.BaseFragment
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.produce.adapter.Prod_Report_Pass_Fragment1Adapter
import ykk.xc.com.bswms.util.JsonUtil
import ykk.xc.com.bswms.util.LogUtil
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：报工审核
 * 作者：ykk
 */
class Prod_Report_Pass_Fragment1 : BaseFragment() {

    companion object {
        private val PASS = 200
        private val UNPASS = 500
        private val SUCC1 = 201
        private val UNSUCC1 = 501
    }
    private val context = this
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private val df = DecimalFormat("#.######")
    private var parent: Prod_Report_Pass_MainActivity? = null
    private var mAdapter: Prod_Report_Pass_Fragment1Adapter? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var timesTamp:String? = null // 时间戳
    private val checkDatas = ArrayList<ProdReportSel>()


    // 消息处理
    private val mHandler = MyHandler(this)
    private class MyHandler(activity: Prod_Report_Pass_Fragment1) : Handler() {
        private val mActivity: WeakReference<Prod_Report_Pass_Fragment1>

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
                    }
                    UNSUCC1 -> { // 查询  失败
                        if(m.checkDatas.size > 0) {
                            m.checkDatas.clear()
                            m.mAdapter!!.notifyDataSetChanged()
                        }
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    PASS -> { // 审核成功 进入
                        m.toasts("操作成功√")
                        m.run_findListByParam()
                    }
                    UNPASS -> { // 审核失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "操作失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.prod_report_pass_fragment1, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Prod_Report_Pass_MainActivity

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = Prod_Report_Pass_Fragment1Adapter(mContext!!, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : Prod_Report_Pass_Fragment1Adapter.MyCallBack {
            override fun onDelete(entity: ProdReportSel, position: Int) {
                val build = AlertDialog.Builder(mContext)
                build.setIcon(R.drawable.caution)
                build.setTitle("系统提示")
                build.setMessage("是否删除选中数据？")
                build.setPositiveButton("是") { dialog, which -> run_modify(entity.id.toString(), "3") }
                build.setNegativeButton("否", null)
                build.setCancelable(false)
                build.show()

            }
        })

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            val entry = checkDatas.get(pos)
            if(entry.isCheckRow) {
                entry.isCheckRow = false
            } else {
                entry.isCheckRow = true
            }
            var isBool = false
            checkDatas.forEach {
                if(it.isCheckRow) {
                    isBool = true
                }
            }
            if(isBool) {
                lin_pass.visibility = View.VISIBLE
            } else {
                lin_pass.visibility = View.GONE
            }
            mAdapter!!.notifyDataSetChanged()
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
        tv_begDate.text = Comm.getSysDate(7)
        tv_endDate.text = Comm.getSysDate(7)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
        }
    }

    @OnClick(R.id.tv_begDate, R.id.tv_endDate, R.id.btn_pass)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.tv_begDate -> { // 开始日期选择
                Comm.showDateDialog(mContext, view, 0)
            }
            R.id.tv_endDate -> { // 结束日期选择
                Comm.showDateDialog(mContext, view, 0)
            }
            R.id.btn_pass -> {
                val strId = StringBuffer()
                checkDatas.forEach {
                    if(it.isCheckRow) {
                        if (strId.length == 0) strId.append(it.id)
                        else strId.append("," + it.id)
                    }
                }
                run_modify(strId.toString(), "1")
            }
        }
    }

    fun parentSearch() {
        run_findListByParam()
    }

    override fun setListener() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*when (requestCode) {
            WRITE_CODE -> {// 输入条码  返回
                if (resultCode == Activity.RESULT_OK) {
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        et_code!!.setText(value.toUpperCase())
                    }
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS,300)*/
    }

    /**
     * 审核
     */
    private fun run_modify(strId :String, passStatus :String) {
        showLoadDialog("保存中...", false)
        var mUrl = getURL("prodReportSel/modifyPassStutus")
        val formBody = FormBody.Builder()
                .add("strId", strId)
                .add("passUserId", user!!.id.toString())
                .add("passStatus", passStatus) // 审核状态(0:未审核，1：已审核，3：已删除)
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNPASS)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNPASS, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(PASS, result)
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
                .add("deptId", user!!.deptId.toString())
                .add("begDate", getValues(tv_begDate))
                .add("endDate", getValues(tv_endDate))
                .add("passStatus", "0") // 只查询未审核的
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