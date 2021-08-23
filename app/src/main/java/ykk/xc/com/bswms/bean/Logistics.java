package ykk.xc.com.bswms.bean;

import java.io.Serializable;

/**
 * 物料公司
 * @author Administrator
 *
 */
public class Logistics implements Serializable {
	private int id;
	//	物流公司代码
	private String logisticsNumber;
	//	物流公司名称
	private String logisticsName;
	//	聚水潭月结账号
	private String gatheringPoolAccount;
	//	聚水潭开发校验码
	private String gatheringPoolCode;
	//	新增/修改时间
	private String createTime;
	//	操作人
	private String createUser;
	//付款代码
	private int paymentMethodNumber;
	//付款方式
	private String paymentMethod;
	//类型
	private String type;
	//快递代码
	private String expressageNumber;
	//快递方式
	private String expressage;
	//快递方式
	private int isDefault;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLogisticsNumber() {
		return logisticsNumber;
	}

	public void setLogisticsNumber(String logisticsNumber) {
		this.logisticsNumber = logisticsNumber;
	}

	public String getLogisticsName() {
		return logisticsName;
	}

	public void setLogisticsName(String logisticsName) {
		this.logisticsName = logisticsName;
	}

	public String getGatheringPoolAccount() {
		return gatheringPoolAccount;
	}

	public void setGatheringPoolAccount(String gatheringPoolAccount) {
		this.gatheringPoolAccount = gatheringPoolAccount;
	}

	public String getGatheringPoolCode() {
		return gatheringPoolCode;
	}

	public void setGatheringPoolCode(String gatheringPoolCode) {
		this.gatheringPoolCode = gatheringPoolCode;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public int getPaymentMethodNumber() {
		return paymentMethodNumber;
	}

	public void setPaymentMethodNumber(int paymentMethodNumber) {
		this.paymentMethodNumber = paymentMethodNumber;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExpressageNumber() {
		return expressageNumber;
	}

	public void setExpressageNumber(String expressageNumber) {
		this.expressageNumber = expressageNumber;
	}

	public String getExpressage() {
		return expressage;
	}

	public void setExpressage(String expressage) {
		this.expressage = expressage;
	}

	public int getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(int isDefault) {
		this.isDefault = isDefault;
	}

}

