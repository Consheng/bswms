package ykk.xc.com.bswms.bean.k3Bean;

import java.io.Serializable;

import ykk.xc.com.bswms.bean.Stock;

/**
 * 盘点方案表
 */
public class ICStockCheckProcess implements Serializable {
	private int fid; // 方案id
	private String fprocessId; // 方案名称
	private Stock stock;
	
	public ICStockCheckProcess() {
		super();
	}
	
	public int getFid() {
		return fid;
	}
	public void setFid(int fid) {
		this.fid = fid;
	}
	public String getFprocessId() {
		return fprocessId;
	}
	public void setFprocessId(String fprocessId) {
		this.fprocessId = fprocessId;
	}
	public Stock getStock() {
		return stock;
	}
	public void setStock(Stock stock) {
		this.stock = stock;
	}
	
}
