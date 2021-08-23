package ykk.xc.com.bswms.entrance

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Message
import android.os.Process
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.OnClick
import kotlinx.android.synthetic.main.aa_main2.*
import okhttp3.*
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.AppInfo
import ykk.xc.com.bswms.bean.User
import ykk.xc.com.bswms.comm.ActivityCollector
import ykk.xc.com.bswms.comm.BaseActivity
import ykk.xc.com.bswms.purchase.Pur_To_Receive_MainActivity
import ykk.xc.com.bswms.util.IDownloadContract
import ykk.xc.com.bswms.util.IDownloadPresenter
import ykk.xc.com.bswms.util.JsonUtil
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * 选择任务单dialog
 */
class Main_Supplier_Activity : BaseActivity(), IDownloadContract.View {

    companion object {
        private val UPDATE = 201
        private val UNUPDATE = 501

        private val UPDATE_PLAN = 1
    }

    private val context = this
    private var user: User? = null
    private val okHttpClient = OkHttpClient()
    private var mPresenter: IDownloadPresenter? = null
    private var isCheckUpdate = false // 是否已经检查过更新

    // 消息处理
    private val mHandler = MyHandler(this)

    /**
     * 显示下载的进度
     */
    private var downloadDialog: Dialog? = null
    private var progressBar: ProgressBar? = null
    private var tvDownPlan: TextView? = null
    private var mProgress = 0

    private class MyHandler(activity: Main_Supplier_Activity) : Handler() {
        private val mActivity: WeakReference<Main_Supplier_Activity>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()

                when (msg.what) {
                    UPDATE -> { // 更新版本  成功
                        m.isCheckUpdate = true
                        val appInfo = JsonUtil.strToObject(msg.obj as String, AppInfo::class.java)
                        if (m.getAppVersionCode(m.context) != appInfo!!.appVersion) {
                            m.showNoticeDialog(appInfo.appRemark)
                        }
                    }
                    UNUPDATE -> { // 更新版本  失败！
                    }
                    UPDATE_PLAN -> { // 更新进度
                        m.progressBar!!.progress = m.mProgress
                        m.tvDownPlan!!.text = String.format(Locale.CHINESE, "%d%%", m.mProgress)
                    }
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.aa_main2
    }

    override fun initView() {
    }

    override fun initData() {
        getUserInfo()
        tv_title!!.text = "操作员：" + user!!.username

        mPresenter = IDownloadPresenter(context)
        if (!isCheckUpdate) {
            // 执行更新版本请求
            run_findAppInfo()
        }
    }

    // 监听事件
    @OnClick(R.id.btn_close, R.id.relative1, R.id.relative2)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                val build = AlertDialog.Builder(context)
                build.setIcon(R.drawable.caution)
                build.setTitle("系统提示")
                build.setMessage("主人，确定要离开我吗？")
                build.setPositiveButton("是的") { dialog, which ->
                    closeHandler(mHandler)
                    ActivityCollector.finishAll()
                    System.exit(0) //凡是非零都表示异常退出!0表示正常退出!
                }
                build.setNegativeButton("取消", null)
                build.setCancelable(false)
                build.show()
            }
            R.id.relative1 -> { // 采购送货
                show(Pur_To_Receive_MainActivity::class.java, null)
            }
            R.id.relative2 -> { // 待送货

            }
        }
    }

    /**
     * 获取服务端的App信息
     */
    private fun run_findAppInfo() {
        val mUrl = getURL("appInfo/findAppInfo")
        val formBody = FormBody.Builder()
                .build()

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        // step 3：创建 Call 对象
        val call = okHttpClient.newCall(request)

        //step 4: 开始异步请求
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNUPDATE)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                Log.e("run_findAppInfo --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    mHandler.sendEmptyMessage(UNUPDATE)
                    return
                }
                val msg = mHandler.obtainMessage(UPDATE, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    private fun showDownloadDialog() {
        val builder = AlertDialog.Builder(context)

        builder.setTitle("软件更新")
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.progress, null)
        progressBar = v.findViewById<View>(R.id.progress) as ProgressBar
        tvDownPlan = v.findViewById<View>(R.id.tv_downPlan) as TextView
        builder.setView(v)
        // 开发员用的，长按进度条，就关闭下载框
        tvDownPlan!!.setOnLongClickListener {
            downloadDialog!!.dismiss()
            true
        }
        // 如果用户点击取消就销毁掉这个系统
        //        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
        //            @Override
        //            public void delClick(DialogInterface dialog, int which) {
        ////                context.finish();
        //                dialog.dismiss();
        //            }
        //        });
        downloadDialog = builder.create()
        downloadDialog!!.show()
        downloadDialog!!.setCancelable(false)
        downloadDialog!!.setCanceledOnTouchOutside(false)
    }

    /**
     * 提示下载框
     */
    private fun showNoticeDialog(remark: String) {
        val alertDialog = AlertDialog.Builder(context)
                .setTitle("更新版本").setMessage(remark)
                .setPositiveButton("下载") { dialog, which ->
                    // 得到ip和端口
                    val spfConfig = spf(getResStr(R.string.saveConfig))
                    val ip = spfConfig.getString("ip", "192.168.3.198")
                    val port = spfConfig.getString("port", "8080")
                    val url = "http://$ip:$port/apks/bswms.apk"

                    showDownloadDialog()
                    mPresenter!!.downApk(context, url)
                    dialog.dismiss()
                }
                //                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                //                    public void delClick(DialogInterface dialog, int which) {
                //                        dialog.dismiss();
                //                    }
                //                })
                .create()// 创建
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()// 显示
    }

    /**
     * 得到本机的版本信息
     */
    private fun getAppVersionCode(context: Context?): Int {
        val pack: PackageManager
        val info: PackageInfo
        // String versionName = "";
        try {
            pack = context!!.packageManager
            info = pack.getPackageInfo(context.packageName, 0)
            return info.versionCode
            // versionName = info.versionName;
        } catch (e: Exception) {
            Log.e("getAppVersionName(Context context)：", e.toString())
        }

        return 0
    }

    override fun showUpdate(version: String) {}

    override fun showProgress(progress: Int) {
        context.mProgress = progress
        mHandler.sendEmptyMessage(UPDATE_PLAN)
    }

    override fun showFail(msg: String) {
        toasts(msg)
    }

    override fun showComplete(file: File) {
        if (downloadDialog != null) downloadDialog!!.dismiss()

        try {
            val intent = Intent(Intent.ACTION_VIEW)

            //7.0以上需要添加临时读取权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val authority = context.applicationContext.packageName + ".fileProvider"
                val fileUri = FileProvider.getUriForFile(context, authority, file)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive")

            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
            }
            startActivity(intent)

            //弹出安装窗口把原程序关闭。
            //避免安装完毕点击打开时没反应
            Process.killProcess(Process.myPid())

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 得到用户对象
     */
    private fun getUserInfo() {
        if (user == null) user = showUserByXml()
    }

}
