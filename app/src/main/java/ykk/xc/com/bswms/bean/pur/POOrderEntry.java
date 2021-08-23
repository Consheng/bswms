package ykk.xc.com.bswms.bean.pur;

import java.io.Serializable;

import ykk.xc.com.bswms.bean.Department;
import ykk.xc.com.bswms.bean.Supplier;
import ykk.xc.com.bswms.bean.k3Bean.Emp;
import ykk.xc.com.bswms.bean.k3Bean.ICItem;

/**
 * @Description:采购订单分录
 *
 */
public class POOrderEntry implements Serializable {

	/*分录内码*/
	private Integer fdetailId;
	/* 采购订单内码 */
	private Integer finterid;
	/* 分录号 */
	private Integer fentryid;
	/* 采购订单号 */
	private String fbillno;
	/* 供应商内码 */
	private Integer fsupplyid;
	/* 供应商 */
	private Supplier supplier;
	/* 单据日期 */
	private String fdate;
	/* 业务员 */
	private Integer fempid;
	/* 业务员 */
	private Emp emp;
	private int fbillerID;	// 制单人
	private int fmangerID; // 主管id
	/* 部门 */
	private Integer fdeptid;
	/* 部门 */
	private Department department;
	/* 物料内码 */
	private Integer fitemid;
	/*物料*/
	private ICItem icItem;
	/* 订货数量 */
	private double fqty;
	/* 单价 */
	private double fprice;
	/* 金额 */
	private double famount;
	/* 备注 */
	private String fnote;
	/* 单位内码 */
	private Integer funitid;
	/*单位名称*/
	private String unitName;
	/*已生码数量 */
	private double createCodeQty;

	// 临时字段，不存表
	private double useableQty; // 可用数
	private double realQty;	// 实际送货数量
	private int rowNo; // 行号
	private boolean check; // 是否选中

	public POOrderEntry() {
	}

	public Integer getFinterid() {
		return finterid;
	}

	public void setFinterid(Integer finterid) {
		this.finterid = finterid;
	}

	public Integer getFentryid() {
		return fentryid;
	}

	public void setFentryid(Integer fentryid) {
		this.fentryid = fentryid;
	}

	public String getFbillno() {
		return fbillno;
	}

	public void setFbillno(String fbillno) {
		this.fbillno = fbillno;
	}

	public Integer getFsupplyid() {
		return fsupplyid;
	}

	public void setFsupplyid(Integer fsupplyid) {
		this.fsupplyid = fsupplyid;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public String getFdate() {
		return fdate;
	}

	public void setFdate(String fdate) {
		this.fdate = fdate;
	}

	public Integer getFempid() {
		return fempid;
	}

	public void setFempid(Integer fempid) {
		this.fempid = fempid;
	}

	public Emp getEmp() {
		return emp;
	}

	public void setEmp(Emp emp) {
		this.emp = emp;
	}

	public Integer getFdeptid() {
		return fdeptid;
	}

	public void setFdeptid(Integer fdeptid) {
		this.fdeptid = fdeptid;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Integer getFitemid() {
		return fitemid;
	}

	public void setFitemid(Integer fitemid) {
		this.fitemid = fitemid;
	}

	public ICItem getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem icItem) {
		this.icItem = icItem;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public double getFprice() {
		return fprice;
	}

	public void setFprice(double fprice) {
		this.fprice = fprice;
	}

	public double getFamount() {
		return famount;
	}

	public void setFamount(double famount) {
		this.famount = famount;
	}

	public String getFnote() {
		return fnote;
	}

	public void setFnote(String fnote) {
		this.fnote = fnote;
	}

	public Integer getFunitid() {
		return funitid;
	}

	public void setFunitid(Integer funitid) {
		this.funitid = funitid;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public double getCreateCodeQty() {
		return createCodeQty;
	}

	public void setCreateCodeQty(double createCodeQty) {
		this.createCodeQty = createCodeQty;
	}

	public Integer getFdetailId() {
		return fdetailId;
	}

	public void setFdetailId(Integer fdetailId) {
		this.fdetailId = fdetailId;
	}

	public double getUseableQty() {
		return useableQty;
	}

	public void setUseableQty(double useableQty) {
		this.useableQty = useableQty;
	}

	public double getRealQty() {
		return realQty;
	}

	public void setRealQty(double realQty) {
		this.realQty = realQty;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public int getFbillerID() {
		return fbillerID;
	}

	public void setFbillerID(int fbillerID) {
		this.fbillerID = fbillerID;
	}

	public int getFmangerID() {
		return fmangerID;
	}

	public void setFmangerID(int fmangerID) {
		this.fmangerID = fmangerID;
	}

	public int getRowNo() {
		return rowNo;
	}

	public void setRowNo(int rowNo) {
		this.rowNo = rowNo;
	}

}