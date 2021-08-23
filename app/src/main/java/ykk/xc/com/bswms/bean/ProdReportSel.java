package ykk.xc.com.bswms.bean;

import java.io.Serializable;

/**
 * @author ykk
 * @createDate 2020-06-22 14:02
 * @brief :  完工汇报记录
 */
public class ProdReportSel implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private int userId;				// 用户id
	private int procedureId;		// 工序id
	private int deptId;				// 部门id
	private int classesId;			// 产品类别id
	private String classesName;		// 产品类别名称
	private int styleId;			// 产品款式id
	private String styleName;		// 产品款式名称
	private int structureId;		// 产品结构id
	private String structureName;	// 产品结构名称
	private String reportDate;		// 报工日期
	private String reportTime;		// 报工时间
	private double fqty;			// 报工数
	private int passStatus;			// 审核状态(0:未审核，1：已审核)
	private int passUserId;			// 审核人id
	private String passDate;		// 审核时间

	private User user;
	private Procedure procedure;
	private Department department;

	// 临时字段，不存表
	private boolean checkRow;	// 是否选中行

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public int getDeptId() {
		return deptId;
	}
	public void setDeptId(int deptId) {
		this.deptId = deptId;
	}
	public int getClassesId() {
		return classesId;
	}
	public void setClassesId(int classesId) {
		this.classesId = classesId;
	}
	public String getClassesName() {
		return classesName;
	}
	public void setClassesName(String classesName) {
		this.classesName = classesName;
	}
	public int getStyleId() {
		return styleId;
	}
	public void setStyleId(int styleId) {
		this.styleId = styleId;
	}
	public String getStyleName() {
		return styleName;
	}
	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}
	public int getStructureId() {
		return structureId;
	}
	public void setStructureId(int structureId) {
		this.structureId = structureId;
	}
	public String getStructureName() {
		return structureName;
	}
	public void setStructureName(String structureName) {
		this.structureName = structureName;
	}
	public String getReportDate() {
		return reportDate;
	}
	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
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
	public int getPassStatus() {
		return passStatus;
	}
	public void setPassStatus(int passStatus) {
		this.passStatus = passStatus;
	}
	public int getPassUserId() {
		return passUserId;
	}
	public void setPassUserId(int passUserId) {
		this.passUserId = passUserId;
	}
	public String getPassDate() {
		return passDate;
	}
	public void setPassDate(String passDate) {
		this.passDate = passDate;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Procedure getProcedure() {
		return procedure;
	}
	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}
	public boolean isCheckRow() {
		return checkRow;
	}
	public void setCheckRow(boolean checkRow) {
		this.checkRow = checkRow;
	}
	public Department getDepartment() {
		return department;
	}
	public void setDepartment(Department department) {
		this.department = department;
	}


}

