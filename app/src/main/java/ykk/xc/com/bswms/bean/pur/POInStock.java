package ykk.xc.com.bswms.bean.pur;

import java.io.Serializable;

import ykk.xc.com.bswms.bean.Department;
import ykk.xc.com.bswms.bean.Supplier;
import ykk.xc.com.bswms.bean.k3Bean.Emp;

/**
 * @Description:收料通知单单
 */
public class POInStock implements Serializable {
	/* 收料通知单内码 */
	private int finterid;
	/* 收料通知单号 */
	private String fbillno;
	/* 单据类型 */
	private int ftrantype;
	/* 供应商内码 */
	private int fsupplyid;
	/* 部门 */
	private int fdeptid;
	/* 业务员 */
	private int fempid;
	/* 单据日期 */
	private String fdate;
	/* 收料仓库id */
	private int fstockId;
	/* 审核人 */
	private int fcheckerid;
	/* 制单人 */
	private int fbillerid;
	/* 备注 */
	private String fnote;
	/* 审核日期 */
	private String fcheckDate;
	/* 单据状态 */
	private int fstatus;
	/* 打印次数 */
	private Short fprintcount;
	/*采购订单号*/
	private String forderBillNo;

	/* 供应商名称 */
	private String suppName;
	/* 采购员名称 */
	private String empName;
	/* 部门名称 */
	private String deptName;

	/* 供应商 */
	private Supplier supplier;
	/* 部门 */
	private Department department;
	/* 业务员 */
	private Emp emp;

	//	 临时字段，不存表
	private double sumQty;	// 收料通知单计算的总数
	private boolean wmsUploadStatus; // WMS上传状态

	public POInStock() {
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

	public int getFtrantype() {
		return ftrantype;
	}

	public void setFtrantype(int ftrantype) {
		this.ftrantype = ftrantype;
	}

	public int getFsupplyid() {
		return fsupplyid;
	}

	public void setFsupplyid(int fsupplyid) {
		this.fsupplyid = fsupplyid;
	}

	public int getFdeptid() {
		return fdeptid;
	}

	public void setFdeptid(int fdeptid) {
		this.fdeptid = fdeptid;
	}

	public int getFempid() {
		return fempid;
	}

	public void setFempid(int fempid) {
		this.fempid = fempid;
	}

	public String getFdate() {
		return fdate;
	}

	public void setFdate(String fdate) {
		this.fdate = fdate;
	}

	public int getFstockId() {
		return fstockId;
	}

	public void setFstockId(int fstockId) {
		this.fstockId = fstockId;
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

	public String getFcheckDate() {
		return fcheckDate;
	}

	public void setFcheckDate(String fcheckDate) {
		this.fcheckDate = fcheckDate;
	}

	public int getFstatus() {
		return fstatus;
	}

	public void setFstatus(int fstatus) {
		this.fstatus = fstatus;
	}

	public Short getFprintcount() {
		return fprintcount;
	}

	public void setFprintcount(Short fprintcount) {
		this.fprintcount = fprintcount;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Emp getEmp() {
		return emp;
	}

	public void setEmp(Emp emp) {
		this.emp = emp;
	}

	public String getSuppName() {
		return suppName;
	}

	public void setSuppName(String suppName) {
		this.suppName = suppName;
	}

	public String getEmpName() {
		return empName;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getForderBillNo() {
		return forderBillNo;
	}

	public void setForderBillNo(String forderBillNo) {
		this.forderBillNo = forderBillNo;
	}

	public double getSumQty() {
		return sumQty;
	}

	public void setSumQty(double sumQty) {
		this.sumQty = sumQty;
	}

	public boolean isWmsUploadStatus() {
		return wmsUploadStatus;
	}

	public void setWmsUploadStatus(boolean wmsUploadStatus) {
		this.wmsUploadStatus = wmsUploadStatus;
	}


}