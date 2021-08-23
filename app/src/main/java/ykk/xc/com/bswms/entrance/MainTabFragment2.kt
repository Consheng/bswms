package ykk.xc.com.bswms.entrance


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import ykk.xc.com.bswms.R
import ykk.xc.com.bswms.bean.User
import ykk.xc.com.bswms.comm.BaseFragment
import ykk.xc.com.bswms.comm.Comm
import ykk.xc.com.bswms.produce.*
import ykk.xc.com.bswms.warehouse.OutInStock_Search_MainActivity

/**
 * 生产
 */
class MainTabFragment2 : BaseFragment() {

    private var user: User? = null

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.aa_main_item2, container, false)
    }

    override fun initView() {
        getUserInfo()
    }

    @OnClick(R.id.relative1, R.id.relative2, R.id.relative3, R.id.relative4, R.id.relative5, R.id.relative6)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.relative1 -> {  // 待上传
                val bundle = Bundle()
                bundle.putInt("pageId", 4)
                bundle.putString("billType", "SCRK")
                show(OutInStock_Search_MainActivity::class.java, bundle)
//                show(Prod_Box_MainActivity::class.java, null)
            }
            R.id.relative2 -> { // 生产入库
                show(Prod_InStock_MainActivity::class.java, null)
//                show(Prod_Box_UnBind_MainActivity::class.java, null)
            }
            R.id.relative3  -> { // 工序汇报
                show(Prod_Report_MainActivity::class.java, null)
            }
            R.id.relative4  -> { // 完工汇报
                show(Prod_Report2_MainActivity::class.java, null)
            }
            R.id.relative5  -> { // 报工审核
                if(user!!.postType == 1) {
                    Comm.showWarnDialog(activity,"您没有权限，主管权限才能进入！")
                } else {
                    show(Prod_Report_Pass_MainActivity::class.java, null)
                }
            }
            R.id.relative6  -> { // 报工查询
                show(Prod_Report_SearchActivity::class.java, null)
            }
        }
    }

    /**
     * 得到用户对象
     */
    private fun getUserInfo() {
        if (user == null) user = showUserByXml()
    }
}
