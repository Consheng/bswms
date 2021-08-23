package ykk.xc.com.bswms.warehouse

import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.ware_inventory_search_by_dept.*
import okhttp3.*
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.basics.Dept_DialogActivity
import ykk.xc.com.bswms.bean.Department
import ykk.xc.com.bswms.bean.k3Bean.Inventory_K3
import ykk.xc.com.bswms.comm.BaseDialogActivity
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.JsonUtil
import ykk.xc.com.bswms.util.xrecyclerview.XRecyclerView
import ykk.xc.com.bswms.warehouse.adapter.Ware_InventorySearchByDept_Adapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * 库存查询
 */
class Ware_InventorySearchByDeptActivity : BaseDialogActivity(), XRecyclerView.LoadingListener {

    companion object {
        private val SEL_DEPT = 10
        private val SUCC1 = 200
        private val UNSUCC1 = 501
    }
    private val context = this
    private val listDatas = ArrayList<Inventory_K3>()
    private var mAdapter: Ware_InventorySearchByDept_Adapter? = null
    private val okHttpClient = OkHttpClient()
    private var limit = 1
    private var isRefresh: Boolean = false
    private var isLoadMore: Boolean = false
    private var isNextPage: Boolean = false
    private var dept :Department? = null

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Ware_InventorySearchByDeptActivity) : Handler() {
        private val mActivity: WeakReference<Ware_InventorySearchByDeptActivity>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()
                when (msg.what) {
                    SUCC1 // 成功
                    -> {
                        val list = JsonUtil.strToList2(msg.obj as String, Inventory_K3::class.java)
                        m.listDatas.addAll(list!!)
                        m.mAdapter!!.notifyDataSetChanged()

                        if (m.isRefresh) {
                            m.xRecyclerView.refreshComplete(true)
                        } else if (m.isLoadMore) {
                            m.xRecyclerView.loadMoreComplete(true)
                        }

                        m.xRecyclerView.setLoadingMoreEnabled(m.isNextPage)
                    }
                    UNSUCC1 // 数据加载失败！
                    -> {
                        m.mAdapter!!.notifyDataSetChanged()
                        m.toasts("抱歉，没有加载到数据！")
                    }
                }
            }
        }

    }

    override fun setLayoutResID(): Int {
        return R.layout.ware_inventory_search_by_dept
    }

    override fun initView() {
        xRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        xRecyclerView.setLayoutManager(LinearLayoutManager(context))
        mAdapter = Ware_InventorySearchByDept_Adapter(context, listDatas)
        xRecyclerView.setAdapter(mAdapter)
        xRecyclerView.setLoadingListener(context)

        xRecyclerView.setPullRefreshEnabled(false) // 上啦刷新禁用
        xRecyclerView.setLoadingMoreEnabled(false); // 不显示下拉刷新的view

        /*mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            val m = listDatas[pos - 1]
            val intent = Intent()
            intent.putExtra("obj", m)
            context.setResult(RESULT_OK, intent)
            context.finish()
        }*/
    }

    override fun initData() {
        val bundle = context.intent.extras
        if (bundle != null) {
        }

//        initLoadDatas()
    }


    // 监听事件
    @OnClick(R.id.btn_close, R.id.btn_search, R.id.tv_deptSel, R.id.btn_clear)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                closeHandler(mHandler)
                context.finish()
            }
            R.id.btn_search -> { // 查询
                hideKeyboard(currentFocus)
                if(dept == null) {
                    Comm.showWarnDialog(context,"请选择车间！")
                    return
                }
                initLoadDatas()
            }
            R.id.tv_deptSel -> { // 车间选择
                showForResult(Dept_DialogActivity::class.java, SEL_DEPT, null)
            }
            R.id.btn_clear -> { // 清空
                dept = null
                tv_deptSel.text = ""
                initLoadDatas()
            }
        }
    }

    private fun initLoadDatas() {
        limit = 1
        listDatas.clear()
        run_okhttpDatas()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                SEL_DEPT -> {//查询部门	返回
                    dept = data!!.getSerializableExtra("obj") as Department
                    tv_deptSel.text = dept!!.departmentName
                    initLoadDatas()
                }
            }
        }
    }

    /**
     * 通过okhttp加载数据
     */
    private fun run_okhttpDatas() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("icInventory/findSumQtyList")
        val deptId = if(dept != null) dept!!.fitemID.toString() else ""
        val formBody = FormBody.Builder()
//                .add("fqtyGt0", "1")
                .add("deptId", deptId)
                .add("mtlNumberAndName", getValues(et_search).trim())
                .add("limit", limit.toString())
                .add("pageSize", "30")
                .build()

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    mHandler.sendEmptyMessage(UNSUCC1)
                    return
                }
                isNextPage = JsonUtil.isNextPage(result)

                val msg = mHandler.obtainMessage(SUCC1, result)
                Log.e("run_okhttpDatas --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    override fun onRefresh() {
        isRefresh = true
        isLoadMore = false
        initLoadDatas()
    }

    override fun onLoadMore() {
        isRefresh = false
        isLoadMore = true
        limit += 1
        run_okhttpDatas()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closeHandler(mHandler)
            context.finish()
        }
        return false
    }

    override fun onDestroy() {
        closeHandler(mHandler)
        super.onDestroy()
    }

}
