package ykk.xc.com.bswms.produce

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.procedure_sel_dialog.*
import okhttp3.*
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.Procedure
import ykk.xc.com.bswms.bean.User
import ykk.xc.com.bswms.comm.BaseDialogActivity
import ykk.xc.com.bswms.produce.adapter.Procedure_Sel_DialogAdapter
import ykk.xc.com.bswms.util.JsonUtil
import ykk.xc.com.bswms.util.basehelper.BaseRecyclerAdapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * 选择工序dialog
 */
class Procedure_Sel_DialogActivity : BaseDialogActivity() {

    private val context = this
    private val listDatas = ArrayList<Procedure>()
    private var mAdapter: Procedure_Sel_DialogAdapter? = null
    private val okHttpClient = OkHttpClient()
    private var user: User? = null

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Procedure_Sel_DialogActivity) : Handler() {
        private val mActivity: WeakReference<Procedure_Sel_DialogActivity>

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
                        val list = JsonUtil.strToList(msg.obj as String, Procedure::class.java)
                        m.listDatas.clear()
                        m.listDatas.addAll(list!!)
                        m.mAdapter!!.notifyDataSetChanged()

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
        return R.layout.procedure_sel_dialog
    }

    override fun initView() {
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = Procedure_Sel_DialogAdapter(context!!, listDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            val m = listDatas[pos]
            val intent = Intent()
            intent.putExtra("obj", m)
            context.setResult(Activity.RESULT_OK, intent)
            context.finish()
        }

        getUserInfo()
    }

    override fun initData() {
        val bundle = context.intent.extras
        if (bundle != null) {
            if(bundle.containsKey("checkDatas")) {
                btn_search.visibility = View.INVISIBLE
                val checkDatas = bundle.getSerializable("checkDatas") as List<Procedure>
                listDatas.addAll(checkDatas)
                mAdapter!!.notifyDataSetChanged()
            }

        } else {
            run_okhttpDatas()
        }
    }


    // 监听事件
    @OnClick(R.id.btn_close, R.id.btn_search)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                closeHandler(mHandler)
                context.finish()
            }
            R.id.btn_search -> run_okhttpDatas()
        }
    }

    /**
     * 通过okhttp加载数据
     */
    private fun run_okhttpDatas() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("procedure/findListByAllotWork")
        val formBody = FormBody.Builder()
                .add("staffId", user!!.id.toString())
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

                val msg = mHandler.obtainMessage(SUCC1, result)
                Log.e("run_okhttpDatas --> onResponse", result)
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

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 501
    }
}
