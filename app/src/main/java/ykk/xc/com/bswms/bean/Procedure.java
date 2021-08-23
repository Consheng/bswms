package ykk.xc.com.bswms.bean;

import java.io.Serializable;

/**
 * 工序
 *
 * @author qxp 2018-11-09
 *
 */
public class Procedure implements Serializable {

	private int id;
	/* 工序编号 */
	private String procedureNumber;
	/* 工序名称 */
	private String procedureName;
	/* 工序状态（2禁用、1启用） */
	private int status;

	private String createDate;
	/* 修改日期 */
	private String fModifyDate;

	/* 创建者id */
	private int createrId;
	/* 创建这名称 */
	private String createrName;

	/* 用于标志勾选状态 */
	private int checkFlag;

	/* 工序说明 */
	private String remark;

	//工序条码
	private String barCode;

	// 临时字段，不存表
	private int processflowId; // 工艺路线主表id，用户汇报后查询工艺路线

	public Procedure() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProcedureNumber() {
		return procedureNumber;
	}

	public void setProcedureNumber(String procedureNumber) {
		this.procedureNumber = procedureNumber;
	}

	public String getProcedureName() {
		return procedureName;
	}

	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getfModifyDate() {
		return fModifyDate;
	}

	public void setfModifyDate(String fModifyDate) {
		this.fModifyDate = fModifyDate;
	}

	public int getCreaterId() {
		return createrId;
	}

	public void setCreaterId(int createrId) {
		this.createrId = createrId;
	}

	public String getCreaterName() {
		return createrName;
	}

	public void setCreaterName(String createrName) {
		this.createrName = createrName;
	}

	public int getCheckFlag() {
		return checkFlag;
	}

	public void setCheckFlag(int checkFlag) {
		this.checkFlag = checkFlag;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getBarCode() {
		return barCode;
	}

	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}

	public int getProcessflowId() {
		return processflowId;
	}

	public void setProcessflowId(int processflowId) {
		this.processflowId = processflowId;
	}
}