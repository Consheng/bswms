package ykk.xc.com.bswms.bean.k3Bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import ykk.xc.com.bswms.bean.Department;

/**
 * @Description:订单实体
 *
 * @author qxp 2019年2月26日 下午4:33:59
 */
public class SeOrder implements Serializable {

	private int finterid;/* 订单内码 */

	private String fbillno;/* 编 号 */

	private int fcustid;/* 购货单位 */

	private String fdate;/* 日期 */

	private String ffetchstyle;/* 交货方式 */

	private String ffetchdate;/* 交货日期 */

	private String ffetchadd;/* 交货地点 */

	private int fempid;/* 业务员 */

	private int fcheckerid;/* 审核人 */

	private int fbillerid;/* 制单 */

	private String fnote;/* 备注 */

	private int fclosed;/* 是否关闭 */

	private int ftrantype;/* 单据类型 */

	private int fmangerid;/* 主管 */

	private Short fstatus;/* 状态 */

	private String fuuid;/* 唯一标识 */

	private Date fchangedate;/* 变更日期 */

	private String fchangecauses;/* 变更原因 */

	private int fchangemark;/* 变更标志 */

	private int fchangeuser;/* 变更人 */

	private String fconsignee;/* 收货方 */

	private Short fprintcount;/* 打印次数 */

	private int fcustaddress;/* 客户地址 */

	private String fexplanation;/* 摘要 */

	/* 店铺名称 */
	private String storeName;
	/* 物流方式 */
	private String logisticsWay;
	/* 物流编号 */
	private String logisticsNo;
	/* 快递公司编号 */
	private String logisticsComNo;
	/* 部门id */
	private int fdeptId;
	/* 部门名称 */
	private String deptName;
	/* 部门编码 */
	private String deptNumber;

	//临时字段
	/* 订单子表物料数量 */
	private int entryQty;
	/* 物料名称 */
	private String itemName;
	/* 物料代码 */
	private String itemNumber;
	/* 购货单位名称 */
	private String custName;
	/* 购货单位代码 */
	private String custNumber;

	/* 快递公司名称 不存库 */
	private String expressNmae;
	/* 快递公司代码 不存库 */
	private String expressNumber;
	private String salType;	// 销售类型（bh:备货，xs:销售）

	private Customer cust; // 客户对象
	private Department department; // 部门对象

	// 临时字段，不存表
	private String printExpressNo;	// 出库后打印的快递单

	public SeOrder() {
		super();
	}

	public int getFinterid() {
		return finterid;
	}

	public void setFinterid(int finterid) {
		this.finterid = finterid;
	}

	public String getFbillno() {
		return fbillno;
	}

	public void setFbillno(String fbillno) {
		this.fbillno = fbillno;
	}

	public int getFcustid() {
		return fcustid;
	}

	public void setFcustid(int fcustid) {
		this.fcustid = fcustid;
	}

	public String getFdate() {
		return fdate;
	}

	public void setFdate(String fdate) {
		this.fdate = fdate;
	}

	public int getFempid() {
		return fempid;
	}

	public void setFempid(int fempid) {
		this.fempid = fempid;
	}

	public int getFcheckerid() {
		return fcheckerid;
	}

	public void setFcheckerid(int fcheckerid) {
		this.fcheckerid = fcheckerid;
	}

	public int getFbillerid() {
		return fbillerid;
	}

	public void setFbillerid(int fbillerid) {
		this.fbillerid = fbillerid;
	}

	public String getFnote() {
		return fnote;
	}

	public void setFnote(String fnote) {
		this.fnote = fnote;
	}

	public int getFclosed() {
		return fclosed;
	}

	public void setFclosed(int fclosed) {
		this.fclosed = fclosed;
	}

	public int getFtrantype() {
		return ftrantype;
	}

	public void setFtrantype(int ftrantype) {
		this.ftrantype = ftrantype;
	}

	public int getFmangerid() {
		return fmangerid;
	}

	public void setFmangerid(int fmangerid) {
		this.fmangerid = fmangerid;
	}

	public Short getFstatus() {
		return fstatus;
	}

	public void setFstatus(Short fstatus) {
		this.fstatus = fstatus;
	}

	public String getFuuid() {
		return fuuid;
	}

	public void setFuuid(String fuuid) {
		this.fuuid = fuuid;
	}

	public Date getFchangedate() {
		return fchangedate;
	}

	public void setFchangedate(Date fchangedate) {
		this.fchangedate = fchangedate;
	}

	public String getFchangecauses() {
		return fchangecauses;
	}

	public void setFchangecauses(String fchangecauses) {
		this.fchangecauses = fchangecauses;
	}

	public int getFchangemark() {
		return fchangemark;
	}

	public void setFchangemark(int fchangemark) {
		this.fchangemark = fchangemark;
	}

	public int getFchangeuser() {
		return fchangeuser;
	}

	public void setFchangeuser(int fchangeuser) {
		this.fchangeuser = fchangeuser;
	}

	public String getFconsignee() {
		return fconsignee;
	}

	public void setFconsignee(String fconsignee) {
		this.fconsignee = fconsignee;
	}

	public Short getFprintcount() {
		return fprintcount;
	}

	public void setFprintcount(Short fprintcount) {
		this.fprintcount = fprintcount;
	}

	public int getFcustaddress() {
		return fcustaddress;
	}

	public void setFcustaddress(int fcustaddress) {
		this.fcustaddress = fcustaddress;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getLogisticsWay() {
		return logisticsWay;
	}

	public void setLogisticsWay(String logisticsWay) {
		this.logisticsWay = logisticsWay;
	}

	public String getLogisticsNo() {
		return logisticsNo;
	}

	public void setLogisticsNo(String logisticsNo) {
		this.logisticsNo = logisticsNo;
	}

	public String getLogisticsComNo() {
		return logisticsComNo;
	}

	public void setLogisticsComNo(String logisticsComNo) {
		this.logisticsComNo = logisticsComNo;
	}

	public int getFdeptId() {
		return fdeptId;
	}

	public void setFdeptId(int fdeptId) {
		this.fdeptId = fdeptId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getDeptNumber() {
		return deptNumber;
	}

	public void setDeptNumber(String deptNumber) {
		this.deptNumber = deptNumber;
	}

	public int getEntryQty() {
		return entryQty;
	}

	public void setEntryQty(int entryQty) {
		this.entryQty = entryQty;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getItemNumber() {
		return itemNumber;
	}

	public void setItemNumber(String itemNumber) {
		this.itemNumber = itemNumber;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public Customer getCust() {
		return cust;
	}

	public void setCust(Customer cust) {
		this.cust = cust;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public String getFfetchstyle() {
		return ffetchstyle;
	}

	public void setFfetchstyle(String ffetchstyle) {
		this.ffetchstyle = ffetchstyle;
	}

	public String getFfetchdate() {
		return ffetchdate;
	}

	public void setFfetchdate(String ffetchdate) {
		this.ffetchdate = ffetchdate;
	}

	public String getFfetchadd() {
		return ffetchadd;
	}

	public void setFfetchadd(String ffetchadd) {
		this.ffetchadd = ffetchadd;
	}

	public String getFexplanation() {
		return fexplanation;
	}

	public void setFexplanation(String fexplanation) {
		this.fexplanation = fexplanation;
	}

	public String getCustNumber() {
		return custNumber;
	}

	public void setCustNumber(String custNumber) {
		this.custNumber = custNumber;
	}

	public String getExpressNmae() {
		return expressNmae;
	}

	public void setExpressNmae(String expressNmae) {
		this.expressNmae = expressNmae;
	}

	public String getExpressNumber() {
		return expressNumber;
	}

	public void setExpressNumber(String expressNumber) {
		this.expressNumber = expressNumber;
	}

	public String getSalType() {
		return salType;
	}

	public void setSalType(String salType) {
		this.salType = salType;
	}

	public String getPrintExpressNo() {
		return printExpressNo;
	}

	public void setPrintExpressNo(String printExpressNo) {
		this.printExpressNo = printExpressNo;
	}

	@Override
	public String toString() {
		return "SeOrder [finterid=" + finterid + ", fbillno=" + fbillno + ", fcustid=" + fcustid + ", fdate=" + fdate
				+ ", ffetchstyle=" + ffetchstyle + ", ffetchdate=" + ffetchdate + ", ffetchadd=" + ffetchadd
				+ ", fempid=" + fempid + ", fcheckerid=" + fcheckerid + ", fbillerid=" + fbillerid + ", fnote=" + fnote
				+ ", fclosed=" + fclosed + ", ftrantype=" + ftrantype + ", fmangerid=" + fmangerid + ", fstatus="
				+ fstatus + ", fuuid=" + fuuid + ", fchangedate=" + fchangedate + ", fchangecauses=" + fchangecauses
				+ ", fchangemark=" + fchangemark + ", fchangeuser=" + fchangeuser + ", fconsignee=" + fconsignee
				+ ", fprintcount=" + fprintcount + ", fcustaddress=" + fcustaddress + ", fexplanation=" + fexplanation
				+ ", storeName=" + storeName + ", logisticsWay=" + logisticsWay + ", logisticsNo=" + logisticsNo
				+ ", logisticsComNo=" + logisticsComNo + ", fdeptId=" + fdeptId + ", deptName=" + deptName
				+ ", deptNumber=" + deptNumber + ", entryQty=" + entryQty + ", itemName=" + itemName + ", itemNumber="
				+ itemNumber + ", custName=" + custName + ", custNumber=" + custNumber + ", expressNmae=" + expressNmae
				+ ", expressNumber=" + expressNumber + ", cust=" + cust + ", department=" + department + "]";
	}

}