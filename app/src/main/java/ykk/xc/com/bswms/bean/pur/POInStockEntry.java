package ykk.xc.com.bswms.bean.pur;

import java.io.Serializable;

import ykk.xc.com.bswms.bean.k3Bean.ICItem;

/**
 * @Description:收料通知单单分录
 *
 */
public class POInStockEntry implements Serializable {
	/* 分录内码 */
	private int fdetailid;
	/* 采购订单内码 */
	private int finterid;
	/* 分录号 */
	private int fentryid;
	/* 物料内码 */
	private int fitemid;
	/* 订货数量 */
	private double fqty;
	/* 到货数量 */
	private double fcommitqty;
	/* 单价 */
	private double fprice;
	/* 金额 */
	private double famount;
	/* 备注 */
	private String fnote;
	/* 单位内码 */
	private int funitid;

	/* 原分录号 */
	private int fsourceentryid;
	/* 原分录id */
	private int fsourceid;

	// 收料主表对象
	private POInStock poInStock;
	private ICItem icItem;

	// 临时字段，不存表
	private String unitName;	// 单位名称
	private double useableQty; // 可用数
	private double realQty; // 实际数
	private int isCheck; // 是否选中

	public POInStockEntry() {
	}

	public int getFdetailid() {
		return fdetailid;
	}

	public void setFdetailid(int fdetailid) {
		this.fdetailid = fdetailid;
	}

	public int getFinterid() {
		return finterid;
	}

	public void setFinterid(int finterid) {
		this.finterid = finterid;
	}

	public int getFentryid() {
		return fentryid;
	}

	public void setFentryid(int fentryid) {
		this.fentryid = fentryid;
	}

	public int getFitemid() {
		return fitemid;
	}

	public void setFitemid(int fitemid) {
		this.fitemid = fitemid;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public double getFcommitqty() {
		return fcommitqty;
	}

	public void setFcommitqty(double fcommitqty) {
		this.fcommitqty = fcommitqty;
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

	public int getFunitid() {
		return funitid;
	}

	public void setFunitid(int funitid) {
		this.funitid = funitid;
	}

	public int getFsourceentryid() {
		return fsourceentryid;
	}

	public void setFsourceentryid(int fsourceentryid) {
		this.fsourceentryid = fsourceentryid;
	}

	public int getFsourceid() {
		return fsourceid;
	}

	public void setFsourceid(int fsourceid) {
		this.fsourceid = fsourceid;
	}

	public POInStock getPoInStock() {
		return poInStock;
	}

	public void setPoInStock(POInStock poInStock) {
		this.poInStock = poInStock;
	}

	public ICItem getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem icItem) {
		this.icItem = icItem;
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

	public int getIsCheck() {
		return isCheck;
	}

	public void setIsCheck(int isCheck) {
		this.isCheck = isCheck;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}


}