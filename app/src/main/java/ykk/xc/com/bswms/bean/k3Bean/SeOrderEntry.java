package ykk.xc.com.bswms.bean.k3Bean;

import java.io.Serializable;
import java.util.List;

import ykk.xc.com.bswms.bean.ICStockBillEntry_Barcode;
import ykk.xc.com.bswms.bean.Stock;
import ykk.xc.com.bswms.bean.StockPosition;

/**
 * @Description:订单详细实体
 *
 * @author qxp 2019年2月26日 下午4:34:06
 */
public class SeOrderEntry implements Serializable {
	private int fdetailid;/*销售订单分录明细内码*/

	private int finterid;/* 订单内码 */

	private int fentryid;/* 分录号 */

	private int fitemid;/* 产品代码 */

	private double fqty;/* 基本单位数量 */

	private double fcommitqty;/* 执行数量 */

	private double fprice;/* 单价 */

	private double createCodeQty;/*已生码数量*/

	private double famount;/* 金额 */

	private String fnote;/* 备注 */

	private int funitid;/* 单位 */

	private double fstockqty;/* 出库数量 */

	private int forderbomstatus;/* 订单BOM状态 */

	private int forderbominterid;/* 订单BOM内码 */

	private double lockQty;/*锁库数量*/

	private SeOrder seOrder;
	private ICItem icItem;

	// 临时字段，不存表
	private int isComplimentary; // 是否赠品，2000005代表是，2000006代表否
	private double useableQty; // 可用数
	private double realQty; // 实际数
	private int isCheck; // 是否选中
	private String unitName;
	private int isFocus; 	// 对焦到扫码行
	private String xsckInfo; // 拼接的销售出库单信息( id,单号，分录Id ),用_隔开
	private String expressCompany; // 快递公司
	private int icItemType; // 产品类型 2000007：主产品，2000008：副产品，2000009：原材料，2000010：其它
	private String icItemClassesName; // 产品类别名称（脚垫，坐垫）
	private List<ICStockBillEntry_Barcode> icstockBillEntry_Barcodes; // 条码记录

	private Stock lockStock;	// 锁库仓库
	private StockPosition lockStockPos; // 锁库库位

	public SeOrderEntry() {
		super();
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

	public void setFqty(Double fqty) {
		this.fqty = fqty;
	}

	public double getFcommitqty() {
		return fcommitqty;
	}

	public void setFcommitqty(Double fcommitqty) {
		this.fcommitqty = fcommitqty;
	}

	public double getCreateCodeQty() {
		return createCodeQty;
	}

	public void setCreateCodeQty(double createCodeQty) {
		this.createCodeQty = createCodeQty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public void setFcommitqty(double fcommitqty) {
		this.fcommitqty = fcommitqty;
	}

	public void setFprice(double fprice) {
		this.fprice = fprice;
	}

	public void setFamount(double famount) {
		this.famount = famount;
	}

	public void setFstockqty(double fstockqty) {
		this.fstockqty = fstockqty;
	}

	public double getFprice() {
		return fprice;
	}

	public void setFprice(Double fprice) {
		this.fprice = fprice;
	}

	public double getFamount() {
		return famount;
	}

	public void setFamount(Double famount) {
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

	public double getFstockqty() {
		return fstockqty;
	}

	public void setFstockqty(Double fstockqty) {
		this.fstockqty = fstockqty;
	}

	public int getForderbomstatus() {
		return forderbomstatus;
	}

	public void setForderbomstatus(int forderbomstatus) {
		this.forderbomstatus = forderbomstatus;
	}

	public int getForderbominterid() {
		return forderbominterid;
	}

	public void setForderbominterid(int forderbominterid) {
		this.forderbominterid = forderbominterid;
	}

	public SeOrder getSeOrder() {
		return seOrder;
	}

	public void setSeOrder(SeOrder seOrder) {
		this.seOrder = seOrder;
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

	public double getLockQty() {
		return lockQty;
	}

	public void setLockQty(double lockQty) {
		this.lockQty = lockQty;
	}

	public int getIsComplimentary() {
		return isComplimentary;
	}

	public void setIsComplimentary(int isComplimentary) {
		this.isComplimentary = isComplimentary;
	}

	public int getIsFocus() {
		return isFocus;
	}

	public void setIsFocus(int isFocus) {
		this.isFocus = isFocus;
	}

	public String getXsckInfo() {
		return xsckInfo;
	}

	public void setXsckInfo(String xsckInfo) {
		this.xsckInfo = xsckInfo;
	}

	public String getExpressCompany() {
		return expressCompany;
	}

	public void setExpressCompany(String expressCompany) {
		this.expressCompany = expressCompany;
	}

	public int getIcItemType() {
		return icItemType;
	}

	public void setIcItemType(int icItemType) {
		this.icItemType = icItemType;
	}

	public String getIcItemClassesName() {
		return icItemClassesName;
	}

	public void setIcItemClassesName(String icItemClassesName) {
		this.icItemClassesName = icItemClassesName;
	}

	public List<ICStockBillEntry_Barcode> getIcstockBillEntry_Barcodes() {
		return icstockBillEntry_Barcodes;
	}

	public void setIcstockBillEntry_Barcodes(List<ICStockBillEntry_Barcode> icstockBillEntry_Barcodes) {
		this.icstockBillEntry_Barcodes = icstockBillEntry_Barcodes;
	}

	public Stock getLockStock() {
		return lockStock;
	}

	public void setLockStock(Stock lockStock) {
		this.lockStock = lockStock;
	}

	public StockPosition getLockStockPos() {
		return lockStockPos;
	}

	public void setLockStockPos(StockPosition lockStockPos) {
		this.lockStockPos = lockStockPos;
	}

}