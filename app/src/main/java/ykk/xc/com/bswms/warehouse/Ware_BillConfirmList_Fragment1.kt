package ykk.xc.com.bswms.warehouse

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.ware_bill_confirm_list_fragment1.*
import kotlinx.android.synthetic.main.ware_bill_confirm_list_main.*
import okhttp3.*
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.ICStockBill
import ykk.xc.com.bswms.bean.User
import ykk.xc.com.bswms.comm.BaseFragment
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.produce.Prod_Transfer2_MainActivity
import ykk.xc.com.bswms.util.JsonUtil
import ykk.xc.com.bswms.util.LogUtil
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import ykk.xc.com.bswms.warehouse.adapter.Ware_BillConfirmListFragment1_Adapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 日期：2020-07-24 11:17
 * 描述：待确认列表
 * 作者：ykk
 */
class Ware_BillConfirmList_Fragment1 : BaseFragment() {

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val DELETE = 201
        private val UNDELETE = 501
        private val UPLOAD = 202
        private val UNUPLOAD = 502

        private val VISIBLE = 1
    }
    private val context = this
    private var parent: Ware_BillConfirmList_MainActivity? = null
    private val checkDatas = ArrayList<ICStockBill>()
    private var okHttpClient: OkHttpClient? = null
    private var mAdapter: Ware_BillConfirmListFragment1_Adapter? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private var curPos:Int = -1 // 当前行
    private var timesTamp:String? = null // 时间戳
    private val df = DecimalFormat("#.######")
    var isInit = false
    var isLoadData = false

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Ware_BillConfirmList_Fragment1) : Handler() {
        private val mActivity: WeakReference<Ware_BillConfirmList_Fragment1>

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
                    SUCC1 -> { // 查询单据 进入
                        m.checkDatas.clear()
                        val list = JsonUtil.strToList(msgObj, ICStockBill::class.java)
                        m.checkDatas.addAll(list)

                        m.mAdapter!!.notifyDataSetChanged()
                    }
                    UNSUCC1 -> { // 查询单据  失败
                        m.checkDatas.clear()
                        m.mAdapter!!.notifyDataSetChanged()
                        m.toasts("很抱歉，没有找到数据！")
                    }
                    DELETE -> { // 删除单据 进入
                        m.curPos = -1
                        m.run_findList()
                    }
                    UNDELETE -> { // 删除单据  失败
                        Comm.showWarnDialog(m.mContext,"服务器繁忙，请稍后再试！")
                    }
                    UPLOAD -> { // 上传单据 进入
//                        val retMsg = JsonUtil.strToString(msgObj)
//                        if(retMsg.length > 0 && retMsg.indexOf("succ") == -1) {
//                            Comm.showWarnDialog(m.mContext, retMsg)
//                        } else {
                            m.toasts("上传成功")
                            m.run_findList()
//                        }
                    }
                    UNUPLOAD -> { // 上传单据  失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "服务器繁忙，请稍后再试！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    VISIBLE -> m.parent!!.btn_batchUpload.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.ware_bill_confirm_list_fragment1, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Ware_BillConfirmList_MainActivity

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = Ware_BillConfirmListFragment1_Adapter(mContext!!, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : Ware_BillConfirmListFragment1_Adapter.MyCallBack {
            override fun onSearch(entity: ICStockBill, position: Int) {
                val bundle = Bundle()
                bundle.putInt("id", entity.id)
                bundle.putBoolean("isUpload", true) // 是否可以上传，只有从确认列表才能上传单据
                if(entity.billType.equals("SCCLDB")) { // 生产材料调拨
                    show(Prod_Transfer2_MainActivity::class.java, bundle)
                } else { // 自由调拨
                    show(Ware_Transfer_MainActivity::class.java, bundle)
                }

            }
            override fun onUpload(entity: ICStockBill, position: Int) {
                val list = ArrayList<ICStockBill>()
                list.add(entity)
                val strJson = JsonUtil.objectToString(list)
                run_uploadToK3(strJson)
            }
            override fun onDelete(entity: ICStockBill, position: Int) {
                curPos = position
                run_remove(entity.id)
            }
        })

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            if(curPos > -1) {
                checkDatas[curPos].isShowButton = false
            }
            curPos = pos
            checkDatas[pos].isShowButton = true
            mAdapter!!.notifyDataSetChanged()
            // 如果是最后一个item,点击就滑动到最下面
            if(checkDatas.size > 2 && checkDatas.size-1 == pos) {
                recyclerView.post(Runnable { recyclerView.smoothScrollToPosition(pos) })
            }
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
        isInit = true
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(isVisibleToUser) {
            // 显示上传按钮
            mHandler.sendEmptyMessageDelayed(VISIBLE, 200)
            if( isInit && !isLoadData) {
                findFun()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(userVisibleHint == true) {
            findFun()
        }
    }

    /**
     * 查询
     */
    fun findFun() {
        isLoadData = true
        curPos = -1
        run_findList()
    }

    /**
     * 一键上传
     */
    fun batchUpload() {
        if(checkDatas.size > 0) {
            val build = AlertDialog.Builder(mContext)
            build.setIcon(R.drawable.caution)
            build.setTitle("系统提示")
            build.setMessage("您确定要全部上传吗？")
            build.setPositiveButton("是") { dialog, which ->
                val strJson = JsonUtil.objectToString(checkDatas)
                run_uploadToK3(strJson)
            }
            build.setNegativeButton("否", null)
            build.setCancelable(false)
            build.show()
        }
    }

//    @OnClick(R.id.tv_deptSel, R.id.tv_suppSel, R.id.tv_stockSel, R.id.btn_scan_stockPos, R.id.btn_stockPosSel, R.id.btn_scan, R.id.btn_mtlSel, R.id.btn_save, R.id.btn_clone)
//    fun onViewClicked(view: View) {
//        var bundle: Bundle? = null
//        when (view.id) {
//            R.id.tv_deptSel -> { // 选择部门
//                showForResult(Dept_DialogActivity::class.java, SEL_DEPT, null)
//            }
//            R.id.tv_suppSel -> { // 选择供应商
//                showForResult(Supplier_DialogActivity::class.java, SEL_SUPP, null)
//            }
//            R.id.tv_stockSel -> { // 选择收货仓库
//                showForResult(Stock_DialogActivity::class.java, SEL_STOCK, null)
//            }
//            R.id.btn_stockPosSel -> { // 选择调出库位
//                if(!checkSelStockPos()) return
//                var bundle = Bundle()
//                bundle.putInt("fspGroupId", stock!!.fspGroupId);
//                showForResult(StockPos_DialogActivity::class.java, SEL_STOCKPOS, bundle)
//            }
//            R.id.btn_mtlSel -> { // 选择物料
//                if(!checkSelStockPos()) return
//                bundle = Bundle()
//                bundle.putInt("fStockId", stock!!.fitemId)
//                bundle.putInt("fStockPlaceID", if(stockPos != null) stockPos!!.fspId else 0)
//                showForResult(StockTransfer_Material_DialogActivity::class.java, SEL_MTL, bundle)
//            }
//            R.id.btn_scan_stockPos -> { // 调用摄像头扫描（调出库位）
//                if(!checkSelStockPos()) return
//                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
//            }
//            R.id.btn_scan -> { // 调用摄像头扫描（物料）
//                if(!checkSelStockPos()) return
//                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
//            }
//            R.id.btn_save -> { // 保存
//                if(checkDatas.size == 0) {
//                    Comm.showWarnDialog(mContext,"请选择物料或者扫码条码！")
//                    return
//                }
//                run_save();
//            }
//            R.id.btn_clone -> { // 重置
//                if (parent!!.isChange) {
//                    val build = AlertDialog.Builder(mContext)
//                    build.setIcon(R.drawable.caution)
//                    build.setTitle("系统提示")
//                    build.setMessage("您有未保存的数据，继续重置吗？")
//                    build.setPositiveButton("是") { dialog, which -> reset(true) }
//                    build.setNegativeButton("否", null)
//                    build.setCancelable(false)
//                    build.show()
//
//                } else {
//                    reset(true)
//                }
//            }
//        }
//    }

    override fun setListener() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
            }
        }
    }

    /**
     * 查询单据
     */
    private fun run_findList() {
        showLoadDialog("加载中...")
        val mUrl = getURL("stockBill_WMS/findList")
        val formBody = FormBody.Builder()
                .add("isToK3", "0") // 查询未上传的
                .add("childSize", "1") // 必须有分录的单据
                .add("fdate", getValues(parent!!.tv_dateSel))
                .add("billType", "ZYDB,SCCLDB")
                .add("confirmUserId", user!!.id.toString()) // 查询待确认列表中确认人对应的出入库表的id
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
                LogUtil.e("run_findList --> onResponse", result)
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
     * 删除单据
     */
    private fun run_remove(id : Int) {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("stockBill_WMS/remove")
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
                mHandler.sendEmptyMessage(UNDELETE)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_remove --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNDELETE, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(DELETE, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 上传单据
     */
    private fun run_uploadToK3(strJson : String) {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("stockBill_WMS/uploadToK3")
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
                mHandler.sendEmptyMessage(UNUPLOAD)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_uploadToK3 --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNUPLOAD, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(UPLOAD, result)
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