package ykk.xc.com.bswms.sales

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import com.gprinter.command.EscCommand
import com.gprinter.command.LabelCommand
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import kotlinx.android.synthetic.main.sal_ds_out_print.*
import okhttp3.*
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.ExpressNoData
import ykk.xc.com.bswms.bean.User
import ykk.xc.com.bswms.comm.BaseActivity
import ykk.xc.com.bswms.comm.BaseFragment
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.util.Base64Utils
import ykk.xc.com.bswms.util.JsonUtil
import ykk.xc.com.bswms.util.LogUtil
import ykk.xc.com.bswms.util.blueTooth.*
import ykk.xc.com.bswms.util.blueTooth.Constant.MESSAGE_UPDATE_PARAMETER
import ykk.xc.com.bswms.util.blueTooth.DeviceConnFactoryManager.CONN_STATE_FAILED
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * 日期：2019-10-16 09:14
 * 描述：电商销售出库
 * 作者：ykk
 */
class Sal_DS_OutStockPrintActivity : BaseActivity() {
    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 500

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val WRITE_CODE = 3
    }

    private val context = this
    private val TAG = "Sal_DS_OutStockPrintActivity"
    val fragment1 = Sal_DS_OutStockFragment1()
    // 蓝牙打印用到的
    private var isConnected: Boolean = false // 蓝牙是否连接标识
    private val id = 0 // 设备id
    private var threadPool: ThreadPool? = null
    private val CONN_STATE_DISCONN = 0x007 // 连接状态断开
    private val PRINTER_COMMAND_ERROR = 0x008 // 使用打印机指令错误
    private val CONN_PRINTER = 0x12
    private var listMap = ArrayList<ExpressNoData>() // 打印保存的数据
    private var cainiaoPrintData :String? = null // 菜鸟打印数据

    private var okHttpClient: OkHttpClient? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var user: User? = null

    // 消息处理
    private val mHandler = MyHandler(this)
    private class MyHandler(activity: Sal_DS_OutStockPrintActivity) : Handler() {
        private val mActivity: WeakReference<Sal_DS_OutStockPrintActivity>

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
                    SUCC1 -> { // 得到打印数据 进入
//                        val list = JsonUtil.strToList(msgObj, ExpressNoData::class.java)
//                        m.setFragment1DataByPrint(list) // 打印

                        if(msgObj!!.indexOf("ykk_jsonArr") > -1) {
                            val list = JsonUtil.strToList(msgObj, ExpressNoData::class.java)
                            m.cainiaoPrintData = null
                            m.setPrintData(list) // 打印

                        } else {
                            m.cainiaoPrintData = JsonUtil.strToString(msgObj)
                            m.setPrintData(null)
                        }
                    }
                    UNSUCC1 -> { // 得到打印数据  失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.context, errMsg)
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        m.setFocusable(m.et_code)
                    }
                    SAOMA -> { // 扫码之后
                        m.run_findPrintData()
                    }
                    m.CONN_STATE_DISCONN -> if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[m.id] != null) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[m.id].closePort(m.id)
                    }
                    m.PRINTER_COMMAND_ERROR -> Utils.toast(m.context, m.getString(R.string.str_choice_printer_command))
                    m.CONN_PRINTER -> Utils.toast(m.context, m.getString(R.string.str_cann_printer))
                    MESSAGE_UPDATE_PARAMETER -> {
                        val strIp = msg.data.getString("Ip")
                        val strPort = msg.data.getString("Port")
                        //初始化端口信息
                        DeviceConnFactoryManager.Build()
                                //设置端口连接方式
                                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                                //设置端口IP地址
                                .setIp(strIp)
                                //设置端口ID（主要用于连接多设备）
                                .setId(m.id)
                                //设置连接的热点端口号
                                .setPort(Integer.parseInt(strPort))
                                .build()
                        m.threadPool = ThreadPool.getInstantiation()
                        m.threadPool!!.addTask(Runnable { DeviceConnFactoryManager.getDeviceConnFactoryManagers()[m.id].openPort() })
                    }
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.sal_ds_out_print
    }

    override fun initData() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(120, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(120, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }

        hideSoftInputMode(et_code)
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)

        bundle()
        getUserInfo()
    }

    private fun bundle() {
        val bundle = context.intent.extras
        if (bundle != null) {
        }
    }

    @OnClick(R.id.btn_close, R.id.btn_scan)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {// 关闭
                context.finish()
            }
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                ScanUtil.startScan(context, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
//                cainiaoPrintData = "abc"
//                setPrintData(null)
            }
        }
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
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 物料---长按输入条码
        et_code!!.setOnLongClickListener {
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
    }

    /**
     * 查询打印数据
     */
    private fun run_findPrintData() {
        isTextChange = false
        showLoadDialog("准备打印...", false)
        val mUrl = getURL("appPrint/printExpressNoBySaoMa")
        val formBody = FormBody.Builder()
                .add("barcode", getValues(et_code))
                .add("userName", user!!.username)
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
                LogUtil.e("run_findPrintData --> onResponse", result)
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
     * Fragment回调得到数据
     */
    private fun setPrintData(list: List<ExpressNoData>?) {
        if(cainiaoPrintData == null) {
            listMap.clear()
            listMap.addAll(list!!)
        }

        if (isConnected) {
            if(cainiaoPrintData != null) {
                byteFormatPrint()
            } else {
                if(list!![0].judge.equals("Y")) { //  是否为丰蜜接口格式打印
                    setStartPrint_SFFM(list!!)
                } else {
                    setStartPrint_SF(list!!)
                }
            }

        } else {
            // 打开蓝牙配对页面
            startActivityForResult(Intent(this, BluetoothDeviceListDialog::class.java), Constant.BLUETOOTH_REQUEST_CODE)
        }
    }

    /**
     * 打印顺丰格式
     */
    private fun setStartPrint_SF(list: List<ExpressNoData>) {
        list.forEach {
            val curDate = Comm.getSysDate(0)
            val tsc = LabelCommand()
            setTscBegin(tsc, 10, true)
            // --------------- 打印区-------------Begin

            // 上下流水结构，先画线后打印其他
            // 画横线
            tsc.addBar(-10, 90, 788, 2)
            // 画竖线
            tsc.addBar(520, 90, 2, 252)
            // （右）横线
            tsc.addBar(520, 138, 262, 2)
            // 画横线
            tsc.addBar(-10, 216, 788, 2)
            // 画横线
            tsc.addBar(-10, 346, 788, 2)
            // 画横线（收方下面）
            tsc.addBar(-10, 472, 788, 2)
            // 画横线（寄付月结下面）
            tsc.addBar(-10, 556, 788, 2)
            // 画竖线（寄方右边）
            tsc.addBar(520, 556, 2, 160)
            // 画横线（寄方下面）
            tsc.addBar(-10, 670, 520, 2)
            // 画竖线（托寄物内容右边）
            tsc.addBar(268, 670, 2, 48)
            // 画横线（回执单-顺丰速运下面）
            tsc.addBar(-10, 830, 788, 2)
            // 画横线（收件人下面）
            tsc.addBar(-10, 960, 788, 2)

            // （左）条码
            tsc.add1DBarcode(50, 108, LabelCommand.BARCODETYPE.CODE128M, 82, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, 2, 5, it.getT01())   // 顺丰快递单
            // （右）顺丰标快
            tsc.addText(536, 103, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT03()+"")   // 顺丰标快 (陆运 )
            tsc.addText(600, 146, LabelCommand.FONTTYPE.FONT_4, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2, it.getT04()+"") // E
            tsc.addText(10, 230, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "目的地：")
            tsc.addText(108, 252, LabelCommand.FONTTYPE.FONT_4, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_3, LabelCommand.FONTMUL.MUL_3, it.getT02()+"") // 466
            tsc.addText(540, 236, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT09()+"") // 店铺id
            tsc.addText(540, 310, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, curDate+"") // 打单日期
            tsc.addText(10, 356, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "收方："+it.getT10()+"") // 收方人
            tsc.addText(276, 356, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT11()+"") // 收方电话
//            tsc.addText(10, 388, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.get("t12")+"") // 收方地址
            // 收件地址超长，计算自动换行（计算两行）
            val t12 = it.getT12()
            val t12Len = t12!!.length
            if(t12Len > 28) {
                tsc.addText(10, 392, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t12.substring(0, 28)+ "") // 收方地址
                if(t12.substring(28, t12Len).trim().length > 0) {
                    tsc.addText(10, 423, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t12.substring(28, t12Len)+ "") // 收方地址
                }
            } else {
                tsc.addText(10, 392, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t12+ "") // 收方地址
            }
            tsc.addText(10, 480, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT05()+"") // 寄付月结
            tsc.addText(178, 480, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "月结卡号："+it.getT06()+"") // 月结卡号
            tsc.addText(540, 480, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT07()+"") // 转寄协议客户
            tsc.addText(10, 566, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "寄方："+it.getT15()+"") // 寄付人
            tsc.addText(540, 566, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "收方签署：")
            tsc.addText(276, 566, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT16()+"") // 寄付电话
//            tsc.addText(10, 600, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.get("t17")+"") // 寄方地址
            // 寄件地址超长，计算自动换行（计算两行）
            val t17 = it.getT17()
            val t17Len = t17!!.length
            if(t17Len > 22) {
                tsc.addText(10, 600, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t17.substring(0, 22)+ "") // 寄方地址
                if(t17.substring(22, t17Len).trim().length > 0) {
                    tsc.addText(10, 631, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t17.substring(22, t17Len)+ "") // 寄方地址
                }
            } else {
                tsc.addText(10, 600, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t17+ "") // 收方地址
            }
//            tsc.addText(10, 678, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "托寄物内容：")
            val t18 = it.getT18()
            val t18Len = t18.length
            if(t18Len > 4) {
                tsc.addText(10, 678, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "托寄物内容："+t18.substring(0,4)+"")
                tsc.addText(150, 709, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t18.substring(4,t18Len)+"")
            } else {
                tsc.addText(10, 678, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "托寄物内容：")
            }
            tsc.addText(278, 678, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "派件员：")
            tsc.addText(540, 678, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "日期：")
            // 寄方回执单
            val b = BitmapFactory.decodeResource(resources, R.drawable.shunfeng)
            tsc.addBitmap(18, 736, LabelCommand.BITMAP_MODE.OVERWRITE, 210, b)
            tsc.add1DBarcode(326, 736, LabelCommand.BARCODETYPE.CODE128M, 70, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, 2, 5, it.getT01()) // 顺丰快递单
            tsc.addText(10, 838, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "收件："+it.getT10()+"") // 收方人
            tsc.addText(276, 838, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT11()+"") // 收方电话
//            tsc.addText(10, 873, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.get("t12")+"") // 收方地址
            // 收件地址超长，计算自动换行（计算两行）
            if(t12Len > 28) {
                tsc.addText(10, 873, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t12.substring(0, 28)+"") // 收方地址
                if(t12.substring(28, t12Len).trim().length > 0) {
                    tsc.addText(10, 904, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t12.substring(28, t12Len) + "") // 收方地址
                }
            } else {
                tsc.addText(10, 873, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t12+"") // 收方地址
            }
            tsc.addText(10, 968, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT08()+"") // 店铺名称
            tsc.addText(460, 968, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT09()+"")  // 店铺id
//            tsc.addText(10, 1006, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "备注："+it.get("t13")+"")  // 卖家备注
            // 备注超长，计算自动换行（计算四行）
            val t13 = it.getT13()
            val t13Len = t13!!.length
            if(t13Len > 30) {
                // 第一行
                tsc.addText(10, 1006, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "备注："+t13.substring(0, 30)+"") // 卖家备注
                if(t13.substring(30, t13Len).length > 30) { // 第二行
                    tsc.addText(82, 1036, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(30, 60) + "") // 卖家备注
                    if(t13.substring(60, t13Len).length > 30) { // 第三行
                        tsc.addText(82, 1066, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(60, 90) + "") // 卖家备注
                        // 第四行
                        tsc.addText(82, 1096, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(90, t13Len) + "") // 卖家备注
                    } else {
                        tsc.addText(82, 1066, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(60, t13Len) + "") // 卖家备注
                    }
                } else {
                    tsc.addText(82, 1036, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(30, t13Len) + "") // 卖家备注
                }
            } else {
                // 第一行
                tsc.addText(10, 1006, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "备注："+t13+"") // 卖家备注
            }
            tsc.addText(10, 1160, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT14()+"")   // 支付时间
            tsc.addText(300, 1160, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, curDate+"") // 打单时间

            // --------------- 打印区-------------End
            setTscEnd(tsc)
        }
    }

    /**
     * 丰蜜格式打印
     */
    private fun setStartPrint_SFFM(list: List<ExpressNoData>) {
        list.forEach {
            val curDate = Comm.getSysDate(0)
            val tsc = LabelCommand()
            setTscBegin(tsc, 10, false)
            // --------------- 打印区-------------Begin

            // 上下流水结构，先画线后打印其他
            // （1）画------（快递单上面）
            tsc.addBar(0, 146, 780, 2)
            // （2）画------（快递单下面）
            tsc.addBar(0, 300, 780, 2)
            // （3）画------（收件人上面）
            tsc.addBar(0, 380, 780, 2)
            // （4）画|||||||（收件人右边）
            tsc.addBar(460, 380, 2, 1300)
            // （5）画------（收件人下面）
            tsc.addBar(0, 510, 460, 2)
            // （6）画------（寄件人下面）
            tsc.addBar(0, 640, 460, 2)
            // （7）画|||||||（寄件人下面）
            tsc.addBar(250, 640, 2, 210)
            // （8）画------
            tsc.addBar(0, 720, 250, 2)
            // （9）画------
            tsc.addBar(0, 850, 460, 2)

            // 寄托物
            tsc.addText(190, 35, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "寄托物："+it.t18)
            // 电商标快
            tsc.addText(478, 35, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, ""+it.proCode)
            // 第几次打印
            tsc.addText(40, 110, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "第"+(parseInt(it.printCount)+1)+"次打印 ")
            // 打印时间
            tsc.addText(235, 110, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "打印时间："+curDate+" ")
            // 条形码（上）
            tsc.add1DBarcode(50, 165, LabelCommand.BARCODETYPE.CODE39, 90, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, 2, 5, it.getT01())   // 顺丰快递单
            // 目的地
            tsc.addText(40, 315, LabelCommand.FONTTYPE.FONT_3, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2, it.getT02()+"")   // 目的地
            // 条码（下）
            tsc.add1DBarcode(586, 450, LabelCommand.BARCODETYPE.CODE39, 90, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_90, 2, 5, it.getT01())   // 顺丰快递单

            // 收件人图片---------------------------------------------
            val shouBit = BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shou)
            if(shouBit != null) {
                tsc.addBitmap(30, 390, LabelCommand.BITMAP_MODE.OVERWRITE, 50, shouBit)
            }
            tsc.addText(100, 390, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT10()+"") // 收方人
            // 收方电话中间4位加密显示
            val strT11 = StringBuilder(it.t11)
            strT11.replace(3,7,"****")
            tsc.addText(250, 390, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, strT11.toString()+"") // 收方电话
            // 收件地址超长，计算自动换行（计算两行）
            val t12 = it.getT12()
            val t12Len = t12!!.length
            if(t12Len > 15) {
                tsc.addText(100, 420, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t12.substring(0, 15)+ "") // 收方地址
                if(t12.substring(15, t12Len).trim().length > 15) { // 第二行
                    tsc.addText(100, 450, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t12.substring(15, 30)+ "") // 收方地址
                    // 第三行
                    tsc.addText(100, 480, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t12.substring(30, t12Len)+ "") // 收方地址

                } else {
                    tsc.addText(100, 450, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t12.substring(15, t12Len)+ "") // 收方地址
                }

            } else {
                tsc.addText(100, 420, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t12+ "") // 收方地址
            }

            // 寄件人图片---------------------------------------------
            val jiBit = BitmapFactory.decodeResource(resources, R.drawable.shunfeng_ji)
            tsc.addBitmap(30, 520, LabelCommand.BITMAP_MODE.OVERWRITE, 50, jiBit)
            // 寄方人
            tsc.addText(100, 520, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT15()+"")
            // 寄方电话中间4位加密显示
            val strT16 = StringBuilder(it.t16)
            strT16.replace(3,7,"****")
            tsc.addText(250, 520, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, strT16.toString()+"")
            // 寄方地址
            // 寄方地址超长，计算自动换行（计算两行）
            val t17 = it.getT17()
            val t17Len = t17!!.length
            if(t17Len > 15) {
                tsc.addText(100, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t17.substring(0, 15)+ "") // 收方地址
                if(t17.substring(15, t17Len).trim().length > 15) { // 第二行
                    tsc.addText(100, 580, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t17.substring(15, 30)+ "") // 收方地址
                    // 第三行
                    tsc.addText(100, 610, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t17.substring(30, t17Len)+ "") // 收方地址

                } else {
                    tsc.addText(100, 580, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t17.substring(15, t17Len)+ "") // 收方地址
                }
            } else {
                tsc.addText(100, 550, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t17+ "") // 收方地址
            }

            // 顺丰二维码图片
            if(it.twoDimensionCode.length > 0) {
                tsc.addQRCode(260, 650, LabelCommand.EEC.LEVEL_L, 5, LabelCommand.ROTATION.ROTATION_0, it.twoDimensionCode)
            }

            // 支付时间
            tsc.addText(15, 730, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT14()+"")
            // 店铺名称
            tsc.addText(15, 770, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT08()+"")
            // 店铺id
            tsc.addText(15, 805, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, it.getT09()+"")

            // 备注超长，计算自动换行（计算六行）
            val t13 = it.getT13()
            val t13Len = t13!!.length
            if(t13Len > 15) {
                // 第一行（15个字符）
                tsc.addText(15, 856, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "备注："+t13.substring(0, 15)+"") // 卖家备注
                if(t13.substring(15, t13Len).length > 18) { // 第二行（18个字符）
                    tsc.addText(15, 885, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(15, 33) + "") // 卖家备注
                    if(t13.substring(33, t13Len).length > 18) { // 第三行（18个字符）
                        tsc.addText(15, 914, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(33, 51) + "") // 卖家备注
                        if(t13.substring(51, t13Len).length > 18) { // 第四行（18个字符）
                            tsc.addText(15, 943, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(51, 69) + "") // 卖家备注
                            if(t13.substring(69, t13Len).length > 18) { // 第五行（18个字符）
                                tsc.addText(15, 972, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(69, 87) + "") // 卖家备注
                                // 第六行（18个字符）
                                tsc.addText(15, 1001, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(87, t13Len) + "") // 卖家备注
                            } else {
                                tsc.addText(15, 972, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(69, t13Len) + "") // 卖家备注
                            }
                        } else {
                            tsc.addText(15, 943, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(51, t13Len) + "") // 卖家备注
                        }
                    } else {
                        tsc.addText(15, 914, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(33, t13Len) + "") // 卖家备注
                    }
                } else {
                    tsc.addText(15, 885, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, t13.substring(15, t13Len) + "") // 卖家备注
                }
            } else {
                // 第一行
                tsc.addText(15, 856, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "备注："+t13+"") // 卖家备注
            }

            // --------------- 打印区-------------End
            setTscEnd(tsc)
        }
    }

    /**
     * 得到时效图片
     */
    private fun getShiXiaoBitMap(proCode :String): Bitmap? {
        when(proCode) {
            "T1" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t1)
            "T4" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t4)
            "T5" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t5)
            "T6" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t6)
            "T8" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t8)
            "T9" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t9)
            "T13" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t13)
            "T14" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t14)
            "T23" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t23)
            "T29" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t29)
            "T36" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t36)
            "T68" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t68)
            "T77" -> return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t77)
        }
        if(proCode.length == 0) {
            return BitmapFactory.decodeResource(resources, R.drawable.shunfeng_shixiao_t1)
        }
        return null
    }

    /**
     * 菜鸟打印方式
     */
    private fun byteFormatPrint() {
//        var str = "H4sIAAAAAAAAAKVVW08bRxR+R+I/rPwYDTBnZnZ3Jm/cKqG0gWIeovyH/CjbiVQItLaFs8R2sGuM7awDXjDYVaIkUlJFIg1tRVUpoijq7Npr72VIE3VXlj1H3/nOZc75nFy6u6gBxtNYu3cPAR/8mJxYWFpdnF9bWr6t4cmJ+W+TkxPJL0auLd5Z0zhHQlCUWEsmCZue+4YkEEYg38TTTjHjfmyn0Xv00Kk1P8YtO68d5/Ri53VizAYYhIrOBUad8+s1Czjf39BKbyv39/rV8omlmcV9n44QioDpEToi34RLMrtCtFtARsEZYiZXxS526x07Vdmw3thO58z6p/lxd+u4VLO6vx38ne3XrCel/LvKc61zdtLo5XOZrtN59fhhe/ekGKDWMVNR15/7GGCIk2iuHmb7opuyc+VffSSVbTK4skuVD+VP7YPtP+zT8qdWu/BLz8ps7m4NTqUL+zRIYYIymgvN/FCue9/rJ7ZzXqhvX9qnrXZzvd/LbIYoGKgo7O7hhptFKlO/LNRdt+3L3Hn98vhod6u5HsnCMJU1t7NXj68ym/n08VHLanWePmtYzn4nffjq8K9UOnsVolAPYD978L64l0rvlbpO6cfii1S60Wj/WWjlzvfe9eyfK0EKrh66Q/vZm1RmdEMEmRircAQTmMF8hhhBLFWmBfpNEDcxDSINZXSYgVGOskTODBXKbWh1J7RBTDlq2X6vWaxqmgZUFwQAU52FT5JkbnZ1fnlhUSNyZLk7Y0B4AjGXAiOKJPHCyi2KZb3CACJbFwpsKgN/xfoE2UDd6/CGjffHXd//UbjPQYmS48lWtV1ta94DnAiTS2nU9aCjrkz3a2QwrHFBblN59Z9XwdqDaZ/CEDoCql6T46OjM7sbaLwwvqQDN+Qz7IA/M9xEOiPDkZFT6M0MQ8a1MyPVliknv1DL/l75UHg5EnKp2npUmj0dL/8U0E+q1vqx4EgM06PVDXjqAYwwyX/yAI7d95hobvmOxhFhrrwhKjfcQNgvRRYIRHmdrsZV8/F3FFVIVxFNzXPN7fTTjx4M3iCcGEr43ODxkQz4tdB5/xlctCwL3IKoW5B3xjHL4GwKgeh1FsMUiEsX2YrAiFLlv2/ufb8cHGSu3LPhuAzDyYExOR3HJyxioFEEiyJ0YoYNbs4BAxjYM8kiBPExQhdhJ5A3FrFgKpBBYdQKeXTdJI8x7CCV8xejnpI9jFBNSfK4KYYSMZAAiFhMPVacZwunwHikSVOxPk4xKduR8jy/SH1B9u8H+mEwU4qPib5DOpqV96qQjSES5FoZ3EXy65Arq0u35Q4gCP38F/hW/ikICwAA"
        // 发送数据
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null) {
            return
        }
//        var byteArr = Base64Utils.decode(str.toCharArray())
        var byteArr = Base64Utils.decode(cainiaoPrintData!!.toCharArray())
        byteArr = Base64Utils.compress(byteArr)
        // 解决打印重复打印的问题
        // PRINT 1,1对应的byte值（80,82,73,78,84,32,49,44,49,13,10），如果末尾有两份这种一样的值，就删除后面一段
        if(byteArr[byteArr.size-1].toInt() == 10 && byteArr[byteArr.size-2].toInt() == 13 && byteArr[byteArr.size-3].toInt() == 49 && byteArr[byteArr.size-4].toInt() == 44 && byteArr[byteArr.size-5].toInt() == 49 && byteArr[byteArr.size-6].toInt() == 32 && byteArr[byteArr.size-7].toInt() == 84 && byteArr[byteArr.size-8].toInt() == 78 && byteArr[byteArr.size-9].toInt() == 73 && byteArr[byteArr.size-10].toInt() == 82 && byteArr[byteArr.size-11].toInt() == 80 &&
                byteArr[byteArr.size-12].toInt() == 10 && byteArr[byteArr.size-13].toInt() == 13 && byteArr[byteArr.size-14].toInt() == 49 && byteArr[byteArr.size-15].toInt() == 44 && byteArr[byteArr.size-16].toInt() == 49 && byteArr[byteArr.size-17].toInt() == 32 && byteArr[byteArr.size-18].toInt() == 84 && byteArr[byteArr.size-19].toInt() == 78 && byteArr[byteArr.size-20].toInt() == 73 && byteArr[byteArr.size-21].toInt() == 82 && byteArr[byteArr.size-22].toInt() == 80) {

            val byteResult = ByteArray(byteArr.size-11)
            System.arraycopy(byteArr, 0, byteResult, 0, byteResult.size)
            byteArr = byteResult
        }

        val vbr = Vector<Byte>()
        for(bt in byteArr.iterator()) {
            vbr.add(bt)
        }
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(vbr)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // 当选择蓝牙的时候按了返回键
            if (data == null) return
            when (requestCode) {
                /*蓝牙连接*/
                Constant.BLUETOOTH_REQUEST_CODE -> {
                    /*获取蓝牙mac地址*/
                    val macAddress = data.getStringExtra(BluetoothDeviceListDialog.EXTRA_DEVICE_ADDRESS)
                    //初始化话DeviceConnFactoryManager
                    DeviceConnFactoryManager.Build()
                            .setId(id)
                            //设置连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                            //设置连接的蓝牙mac地址
                            .setMacAddress(macAddress)
                            .build()
                    //打开端口
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort()
                }
                BaseFragment.CAMERA_SCAN -> {// 扫一扫成功  返回
                    val hmsScan = data!!.getParcelableExtra(ScanUtil.RESULT) as HmsScan
                    if (hmsScan != null) {
                        setTexts(et_code, hmsScan.originalValue)
                    }
                }
                WRITE_CODE -> {// 输入条码  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        et_code!!.setText(value.toUpperCase())
                    }
                }
            }

        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS,200)
    }

    /**
     * 打印前段配置
     * @param tsc
     */
    private fun setTscBegin(tsc: LabelCommand, gap: Int, bigFlag :Boolean) {
        // 设置标签尺寸，按照实际尺寸设置
        if(bigFlag) tsc.addSize(100, 150)
        else tsc.addSize(75, 130)
        // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
        tsc.addGap(gap)
        // 设置打印方向
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL)
        // 开启带Response的打印，用于连续打印
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON)
        // 设置原点坐标
        tsc.addReference(0, 0)
        // 撕纸模式开启
        tsc.addTear(EscCommand.ENABLE.ON)
        // 清除打印缓冲区
        tsc.addCls()
    }

    /**
     * 打印后段配置
     * @param tsc
     */
    private fun setTscEnd(tsc: LabelCommand) {
        // 打印标签
        tsc.addPrint(1, 1)
        // 打印标签后 蜂鸣器响

        tsc.addSound(2, 100)
        tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255)
        val datas = tsc.command
        // 发送数据
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null) {
            return
        }
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas)
    }

    /**
     * 蓝牙监听广播
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                // 蓝牙连接断开广播
                ACTION_USB_DEVICE_DETACHED, BluetoothDevice.ACTION_ACL_DISCONNECTED -> mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget()
                DeviceConnFactoryManager.ACTION_CONN_STATE -> {
                    val state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1)
                    val deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1)
                    when (state) {
                        DeviceConnFactoryManager.CONN_STATE_DISCONNECT -> if (id == deviceId) {
                            tv_connState.setText(getString(R.string.str_conn_state_disconnect))
                            tv_connState.setTextColor(Color.parseColor("#666666")) // 未连接-灰色
                            isConnected = false
                        }
                        DeviceConnFactoryManager.CONN_STATE_CONNECTING -> {
                            tv_connState.setText(getString(R.string.str_conn_state_connecting))
                            tv_connState.setTextColor(Color.parseColor("#6a5acd")) // 连接中-紫色
                            isConnected = false
                        }
                        DeviceConnFactoryManager.CONN_STATE_CONNECTED -> {
                            //                            tv_connState.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
                            tv_connState.setText(getString(R.string.str_conn_state_connected))
                            tv_connState.setTextColor(Color.parseColor("#008800")) // 已连接-绿色
                            // 连接成功，开始打印
                            if(cainiaoPrintData != null) {
                                byteFormatPrint()

                            } else {
                                if(listMap[0].judge.equals("Y")) { //  是否为丰蜜接口格式打印
                                    setStartPrint_SFFM(listMap!!)
                                } else {
                                    setStartPrint_SF(listMap!!)
                                }
                            }

                            isConnected = true
                        }
                        CONN_STATE_FAILED -> {
                            Utils.toast(context, getString(R.string.str_conn_fail))
                            tv_connState.setText(getString(R.string.str_conn_state_disconnect))
                            tv_connState.setTextColor(Color.parseColor("#666666")) // 未连接-灰色
                            isConnected = false
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    /**
     * 得到用户对象
     */
    private fun getUserInfo() {
        if (user == null) user = showUserByXml()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_DEVICE_DETACHED)
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE)
        registerReceiver(receiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy()")
        DeviceConnFactoryManager.closeAllPort()
        if (threadPool != null) {
            threadPool!!.stopThreadPool()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // 按了删除键，回退键
        //        if(!isKeyboard && (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL || event.getKeyCode() == KeyEvent.KEYCODE_DEL)) {
        // 240 为PDA两侧面扫码键，241 为PDA中间扫码键
        return if (!(event.keyCode == 240 || event.keyCode == 241)) {
            false
        } else super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            context.finish()
        }
        return false
    }
}