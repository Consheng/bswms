package ykk.xc.com.bswms.bean;

import java.io.Serializable;

import ykk.xc.com.bswms.bean.k3Bean.ICItem;

/**
 * @author lin
 * @createDate 2020-06-17 10:55
 * @brief :  生产汇报记录
 */
public class ProdReport implements Serializable {
	private int id;
	private int barcodeTableId;		// 对应条码表Id
	private int userId;				// 用户id
	private int processflowId; 		// 工艺路线主表id
	private int procedureId;		// 工序id
	private String reportDate;		// 报工日期
	private String reportTime;		// 报工时间
	private double fqty;			// 报工数
	private int deptId;				// 部门id
	private int icItemId;			// 物料id
	private int icmoFinterId;		// 生产任务单id
	private String icmoFbillNo;		// 生产任务单号
	private char reportType;		// 汇报类型：A:正常汇报，B:返工汇报

	private User user;
	private ICItem icItem;
	private Procedure procedure;
	private Department department;
	private BarCodeTable barCodeTable;

	// 临时字段，不存表
	private double amount;	// 金额

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBarcodeTableId() {
		return barcodeTableId;
	}

	public void setBarcodeTableId(int barcodeTableId) {
		this.barcodeTableId = barcodeTableId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getProcedureId() {
		return procedureId;
	}

	public void setProcedureId(int procedureId) {
		this.procedureId = procedureId;
	}

	public String getReportTime() {
		return reportTime;
	}

	public void setReportTime(String reportTime) {
		this.reportTime = reportTime;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public int getDeptId() {
		return deptId;
	}

	public void setDeptId(int deptId) {
		this.deptId = deptId;
	}

	public int getIcItemId() {
		return icItemId;
	}

	public void setIcItemId(int icItemId) {
		this.icItemId = icItemId;
	}

	public int getIcmoFinterId() {
		return icmoFinterId;
	}

	public void setIcmoFinterId(int icmoFinterId) {
		this.icmoFinterId = icmoFinterId;
	}

	public String getIcmoFbillNo() {
		return icmoFbillNo;
	}

	public void setIcmoFbillNo(String icmoFbillNo) {
		this.icmoFbillNo = icmoFbillNo;
	}

	public String getReportDate() {
		return reportDate;
	}

	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ICItem getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem icItem) {
		this.icItem = icItem;
	}

	public Procedure getProcedure() {
		return procedure;
	}

	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public int getProcessflowId() {
		return processflowId;
	}

	public void setProcessflowId(int processflowId) {
		this.processflowId = processflowId;
	}

	public BarCodeTable getBarCodeTable() {
		return barCodeTable;
	}

	public void setBarCodeTable(BarCodeTable barCodeTable) {
		this.barCodeTable = barCodeTable;
	}

	public char getReportType() {
		return reportType;
	}

	public void setReportType(char reportType) {
		this.reportType = reportType;
	}


}
