package ykk.xc.com.bswms.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ykk.xc.com.bswms.bean.k3Bean.ICItem;
import ykk.xc.com.bswms.comm.Comm;

/**
 * Wms 本地的出入库	Entry表
 * @author Administrator
 *
 */
public class ICStockBillEntry implements Serializable {
	private int id; 					//
	private int icstockBillId;			// 主表id
	private int finterId;				// 主表id
	private int fitemId;				// 物料id
	private int fentryId;				// 分录id
	private int fdcStockId;				// 调入仓库id
	private int fdcSPId;				// 调入库位id
	private int fscStockId;				// 调出仓库id
	private int fscSPId;				// 调出库位id
	private double fqty;				// 数量
	private double fprice;				// 单价
	private double ftaxRate;			// 税率
	private double ftaxPrice;			// 含税单价
	private int funitId;				// 单位id
	private int fsourceInterId;			// 来源内码id
	private int fsourceEntryId;			// 来源分录id
	private int fsourceTranType;		// 来源类型
	private String fsourceBillNo;		// 来源单号
	private double fsourceQty;			// 来源单数量
	private int forderInterId;			// 来源订单id
	private String forderBillNo;		// 来源订单号
	private int forderEntryId;			// 来源订单分录id
	private int fdetailId; 				// 来源分类唯一行标识
	// 调入仓库组
	private int stockId_wms; 			// WMS 调入--仓库id
	private int stockAreaId_wms; 		// WMS 调入--库区id
	private int storageRackId_wms; 		// WMS 调入--货架id
	private int stockPosId_wms; 		// WMS 调入--库位id
	private int containerId; 			// WMS 调入--容器id
	// 调出仓库组
	private int stockId2_wms; 			// WMS 调出--仓库id
	private int stockAreaId2_wms; 		// WMS 调出--库区id
	private int storageRackId2_wms; 	// WMS 调出--货架id
	private int stockPosId2_wms; 		// WMS 调出--库位id
	private int containerId2; 			// WMS 调出--容器id

	private String remark;				// 备注
	private double qcPassQty;			// 质检合格数
	private String fkfDate; 			// 生产/采购日期
	private int fkfPeriod;				// 保质期
	private int sourceThisId;			// 来源本身id
	private int boxBarCodeId;			// 装箱表id
	private int isComplimentary; 		// 是否赠品，2000005代表是，2000006代表否
	private int assistUnitId;		// 辅助计量单位id
	private double assistQty;		// 辅助计量单位数量

	private ICStockBill icstockBill;
	private Stock stock;
	private StockArea stockArea;
	private StorageRack storageRack;
	private StockPosition stockPos;
	private Container container;
	private Stock stock2;
	private StockArea stockArea2;
	private StorageRack storageRack2;
	private StockPosition stockPos2;
	private Container container2;
	private ICItem icItem;
	private Unit unit;
	private Unit assistUnit;
	private BoxBarCode boxBarCode;

	// 临时字段，不存表
	private boolean showButton; 		// 是否显示操作按钮
	private double allotQty; // 调拨数
	private String smBatchCode; // 扫码的批次号
	private String smSnCode; // 扫码的序列号
	private double smQty;	// 扫码后计算出的数
	private String strBatchCode; // 拼接的批次号
	private String strBarcode;	// 用于显示拼接的条码号
	private String k3Number; // 主表的k3Number
	private double inventoryNowQty; // 当前扫码的可用库存数
	//	private List<ExpressNoData> listExpressNo; // 临时快递单号
	private ExpressNoData expressNoData; // 临时快递单号
	private int icItemType; // 产品类型 2000007：主产品，2000008：副产品，2000009：原材料，2000010：其它
	private double inStockQty; // 调入仓库库存
	private double outStockQty; // 调出仓库库存

	private ICStockBillEntry sourceThis; // 来源本身对象
	private List<ICStockBillEntry_Barcode> icstockBillEntry_Barcodes; // 条码记录

	public ICStockBillEntry() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIcstockBillId() {
		return icstockBillId;
	}

	public void setIcstockBillId(int icstockBillId) {
		this.icstockBillId = icstockBillId;
	}

	public int getFinterId() {
		return finterId;
	}

	public void setFinterId(int finterId) {
		this.finterId = finterId;
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public int getFentryId() {
		return fentryId;
	}

	public void setFentryId(int fentryId) {
		this.fentryId = fentryId;
	}

	public int getFdcStockId() {
		return fdcStockId;
	}

	public void setFdcStockId(int fdcStockId) {
		this.fdcStockId = fdcStockId;
	}

	public int getFdcSPId() {
		return fdcSPId;
	}

	public void setFdcSPId(int fdcSPId) {
		this.fdcSPId = fdcSPId;
	}

	public int getFscStockId() {
		return fscStockId;
	}

	public void setFscStockId(int fscStockId) {
		this.fscStockId = fscStockId;
	}

	public int getFscSPId() {
		return fscSPId;
	}

	public void setFscSPId(int fscSPId) {
		this.fscSPId = fscSPId;
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

	public int getFunitId() {
		return funitId;
	}

	public void setFunitId(int funitId) {
		this.funitId = funitId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public double getFtaxRate() {
		return ftaxRate;
	}

	public void setFtaxRate(double ftaxRate) {
		this.ftaxRate = ftaxRate;
	}

	public double getFtaxPrice() {
		return ftaxPrice;
	}

	public void setFtaxPrice(double ftaxPrice) {
		this.ftaxPrice = ftaxPrice;
	}

	public int getFsourceEntryId() {
		return fsourceEntryId;
	}

	public void setFsourceEntryId(int fsourceEntryId) {
		this.fsourceEntryId = fsourceEntryId;
	}

	public int getFsourceTranType() {
		return fsourceTranType;
	}

	public void setFsourceTranType(int fsourceTranType) {
		this.fsourceTranType = fsourceTranType;
	}

	public String getFsourceBillNo() {
		return fsourceBillNo;
	}

	public void setFsourceBillNo(String fsourceBillNo) {
		this.fsourceBillNo = fsourceBillNo;
	}

	public int getFsourceInterId() {
		return fsourceInterId;
	}

	public void setFsourceInterId(int fsourceInterId) {
		this.fsourceInterId = fsourceInterId;
	}

	public int getForderInterId() {
		return forderInterId;
	}

	public void setForderInterId(int forderInterId) {
		this.forderInterId = forderInterId;
	}

	public String getForderBillNo() {
		return forderBillNo;
	}

	public void setForderBillNo(String forderBillNo) {
		this.forderBillNo = forderBillNo;
	}

	public int getForderEntryId() {
		return forderEntryId;
	}

	public void setForderEntryId(int forderEntryId) {
		this.forderEntryId = forderEntryId;
	}

	public int getFdetailId() {
		return fdetailId;
	}

	public void setFdetailId(int fdetailId) {
		this.fdetailId = fdetailId;
	}

	public boolean isShowButton() {
		return showButton;
	}

	public void setShowButton(boolean showButton) {
		this.showButton = showButton;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public StockArea getStockArea() {
		return stockArea;
	}

	public void setStockArea(StockArea stockArea) {
		this.stockArea = stockArea;
	}

	public StorageRack getStorageRack() {
		return storageRack;
	}

	public void setStorageRack(StorageRack storageRack) {
		this.storageRack = storageRack;
	}

	public StockPosition getStockPos() {
		return stockPos;
	}

	public void setStockPos(StockPosition stockPos) {
		this.stockPos = stockPos;
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public ICItem getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem icItem) {
		this.icItem = icItem;
	}

	public double getFsourceQty() {
		return fsourceQty;
	}

	public void setFsourceQty(double fsourceQty) {
		this.fsourceQty = fsourceQty;
	}

	public int getStockId_wms() {
		return stockId_wms;
	}

	public void setStockId_wms(int stockId_wms) {
		this.stockId_wms = stockId_wms;
	}

	public int getStockAreaId_wms() {
		return stockAreaId_wms;
	}

	public void setStockAreaId_wms(int stockAreaId_wms) {
		this.stockAreaId_wms = stockAreaId_wms;
	}

	public int getStorageRackId_wms() {
		return storageRackId_wms;
	}

	public void setStorageRackId_wms(int storageRackId_wms) {
		this.storageRackId_wms = storageRackId_wms;
	}

	public int getStockPosId_wms() {
		return stockPosId_wms;
	}

	public void setStockPosId_wms(int stockPosId_wms) {
		this.stockPosId_wms = stockPosId_wms;
	}

	public int getContainerId() {
		return containerId;
	}

	public void setContainerId(int containerId) {
		this.containerId = containerId;
	}

	public double getQcPassQty() {
		return qcPassQty;
	}

	public void setQcPassQty(double qcPassQty) {
		this.qcPassQty = qcPassQty;
	}

	public String getFkfDate() {
		return fkfDate;
	}

	public void setFkfDate(String fkfDate) {
		this.fkfDate = fkfDate;
	}

	public ICStockBill getIcstockBill() {
		return icstockBill;
	}

	public void setIcstockBill(ICStockBill icstockBill) {
		this.icstockBill = icstockBill;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public double getAllotQty() {
		return allotQty;
	}

	public void setAllotQty(double allotQty) {
		this.allotQty = allotQty;
	}

	public int getFkfPeriod() {
		return fkfPeriod;
	}

	public void setFkfPeriod(int fkfPeriod) {
		this.fkfPeriod = fkfPeriod;
	}

	public List<ICStockBillEntry_Barcode> getIcstockBillEntry_Barcodes() {
		if(icstockBillEntry_Barcodes == null) {
			icstockBillEntry_Barcodes = new ArrayList<>();
		}
		return icstockBillEntry_Barcodes;
	}

	public void setIcstockBillEntry_Barcodes(List<ICStockBillEntry_Barcode> icstockBillEntry_Barcodes) {
		this.icstockBillEntry_Barcodes = icstockBillEntry_Barcodes;
	}

	public String getSmBatchCode() {
		return smBatchCode;
	}

	public void setSmBatchCode(String smBatchCode) {
		this.smBatchCode = smBatchCode;
	}

	public String getSmSnCode() {
		return smSnCode;
	}

	public void setSmSnCode(String smSnCode) {
		this.smSnCode = smSnCode;
	}

	public String getStrBatchCode() {
		// 存在大写的逗号（，）,且大于1
		if(Comm.isNULLS(strBatchCode).indexOf("，") > -1 && Comm.isNULLS(strBatchCode).length() > 0) {
			return strBatchCode.substring(0, strBatchCode.length()-1);
		}
		return strBatchCode;
	}

	public void setStrBatchCode(String strBatchCode) {
		this.strBatchCode = strBatchCode;
	}

	public String getStrBarcode() {
		return strBarcode;
	}

	public void setStrBarcode(String strBarcode) {
		this.strBarcode = strBarcode;
	}

	public int getStockId2_wms() {
		return stockId2_wms;
	}

	public void setStockId2_wms(int stockId2_wms) {
		this.stockId2_wms = stockId2_wms;
	}

	public int getStockAreaId2_wms() {
		return stockAreaId2_wms;
	}

	public void setStockAreaId2_wms(int stockAreaId2_wms) {
		this.stockAreaId2_wms = stockAreaId2_wms;
	}

	public int getStorageRackId2_wms() {
		return storageRackId2_wms;
	}

	public void setStorageRackId2_wms(int storageRackId2_wms) {
		this.storageRackId2_wms = storageRackId2_wms;
	}

	public int getStockPosId2_wms() {
		return stockPosId2_wms;
	}

	public void setStockPosId2_wms(int stockPosId2_wms) {
		this.stockPosId2_wms = stockPosId2_wms;
	}

	public int getContainerId2() {
		return containerId2;
	}

	public void setContainerId2(int containerId2) {
		this.containerId2 = containerId2;
	}

	public Stock getStock2() {
		return stock2;
	}

	public void setStock2(Stock stock2) {
		this.stock2 = stock2;
	}

	public StockArea getStockArea2() {
		return stockArea2;
	}

	public void setStockArea2(StockArea stockArea2) {
		this.stockArea2 = stockArea2;
	}

	public StorageRack getStorageRack2() {
		return storageRack2;
	}

	public void setStorageRack2(StorageRack storageRack2) {
		this.storageRack2 = storageRack2;
	}

	public StockPosition getStockPos2() {
		return stockPos2;
	}

	public void setStockPos2(StockPosition stockPos2) {
		this.stockPos2 = stockPos2;
	}

	public Container getContainer2() {
		return container2;
	}

	public void setContainer2(Container container2) {
		this.container2 = container2;
	}

	public int getSourceThisId() {
		return sourceThisId;
	}

	public void setSourceThisId(int sourceThisId) {
		this.sourceThisId = sourceThisId;
	}

	public ICStockBillEntry getSourceThis() {
		return sourceThis;
	}

	public void setSourceThis(ICStockBillEntry sourceThis) {
		this.sourceThis = sourceThis;
	}

	public String getK3Number() {
		return k3Number;
	}

	public void setK3Number(String k3Number) {
		this.k3Number = k3Number;
	}

	public double getInventoryNowQty() {
		return inventoryNowQty;
	}

	public void setInventoryNowQty(double inventoryNowQty) {
		this.inventoryNowQty = inventoryNowQty;
	}

	public int getBoxBarCodeId() {
		return boxBarCodeId;
	}

	public void setBoxBarCodeId(int boxBarCodeId) {
		this.boxBarCodeId = boxBarCodeId;
	}

	public BoxBarCode getBoxBarCode() {
		return boxBarCode;
	}

	public void setBoxBarCode(BoxBarCode boxBarCode) {
		this.boxBarCode = boxBarCode;
	}

	public int getIsComplimentary() {
		return isComplimentary;
	}

	public void setIsComplimentary(int isComplimentary) {
		this.isComplimentary = isComplimentary;
	}

//	public List<ExpressNoData> getListExpressNo() {
//		if(listExpressNo == null) listExpressNo = new ArrayList<>();
//		return listExpressNo;
//	}
//
//	public void setListExpressNo(List<ExpressNoData> listExpressNo) {
//		this.listExpressNo = listExpressNo;
//	}

	public ExpressNoData getExpressNoData() {
		return expressNoData;
	}

	public void setExpressNoData(ExpressNoData expressNoData) {
		this.expressNoData = expressNoData;
	}

	public int getAssistUnitId() {
		return assistUnitId;
	}

	public void setAssistUnitId(int assistUnitId) {
		this.assistUnitId = assistUnitId;
	}

	public double getAssistQty() {
		return assistQty;
	}

	public void setAssistQty(double assistQty) {
		this.assistQty = assistQty;
	}

	public Unit getAssistUnit() {
		return assistUnit;
	}
	public void setAssistUnit(Unit assistUnit) {
		this.assistUnit = assistUnit;
	}
	public int getIcItemType() {
		return icItemType;
	}
	public void setIcItemType(int icItemType) {
		this.icItemType = icItemType;
	}

	public double getInStockQty() {
		return inStockQty;
	}

	public void setInStockQty(double inStockQty) {
		this.inStockQty = inStockQty;
	}

	public double getOutStockQty() {
		return outStockQty;
	}

	public void setOutStockQty(double outStockQty) {
		this.outStockQty = outStockQty;
	}

	public double getSmQty() {
		return smQty;
	}

	public void setSmQty(double smQty) {
		this.smQty = smQty;
	}
}
