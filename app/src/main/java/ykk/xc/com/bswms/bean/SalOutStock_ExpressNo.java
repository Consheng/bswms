package ykk.xc.com.bswms.bean;

import java.io.Serializable;

/**
 * Wms 记录电商出库时（条码、快递单号、订单）的对应关系
 * @author Administrator
 *
 */
public class SalOutStock_ExpressNo implements Serializable {
	private int id;
	private String salOrderNo;	// 订单号
	private int salOrderEntry;	// 订单分录id
	private String barcode;		// 条码号
	private String expressNo;	// 快递号
	private int createUserId;	// 创建人
	private String createDate;	// 创建日期
	private int isEnable;		// 是否禁用

	public SalOutStock_ExpressNo() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSalOrderNo() {
		return salOrderNo;
	}

	public void setSalOrderNo(String salOrderNo) {
		this.salOrderNo = salOrderNo;
	}

	public int getSalOrderEntry() {
		return salOrderEntry;
	}

	public void setSalOrderEntry(int salOrderEntry) {
		this.salOrderEntry = salOrderEntry;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getExpressNo() {
		return expressNo;
	}

	public void setExpressNo(String expressNo) {
		this.expressNo = expressNo;
	}

	public int getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public int getIsEnable() {
		return isEnable;
	}

	public void setIsEnable(int isEnable) {
		this.isEnable = isEnable;
	}


}
