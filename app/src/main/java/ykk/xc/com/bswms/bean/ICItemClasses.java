package ykk.xc.com.bswms.bean;

import java.io.Serializable;

/**
 * 产品类别
 * @author Administrator
 *
 */
public class ICItemClasses implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private int icitemAttributeId; // 产品属性id
	private String fnumber;			// 代码
	private String fname; 			// 名称

	private ICItemAttribute icItemAttribute;

	public ICItemClasses() {
		super();
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIcitemAttributeId() {
		return icitemAttributeId;
	}
	public void setIcitemAttributeId(int icitemAttributeId) {
		this.icitemAttributeId = icitemAttributeId;
	}
	public String getFnumber() {
		return fnumber;
	}
	public void setFnumber(String fnumber) {
		this.fnumber = fnumber;
	}
	public String getFname() {
		return fname;
	}
	public void setFname(String fname) {
		this.fname = fname;
	}

	public ICItemAttribute getIcItemAttribute() {
		return icItemAttribute;
	}

	public void setIcItemAttribute(ICItemAttribute icItemAttribute) {
		this.icItemAttribute = icItemAttribute;
	}

	@Override
	public String toString() {
		return "ICItemClasses [id=" + id + ", icitemAttributeId=" + icitemAttributeId + ", fnumber=" + fnumber
				+ ", fname=" + fname + ", icItemAttribute=" + icItemAttribute + "]";
	}


}

