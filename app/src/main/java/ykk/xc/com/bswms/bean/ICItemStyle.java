package ykk.xc.com.bswms.bean;

import java.io.Serializable;

/**
 * 款式表
 * @author Administrator
 *
*/
public class ICItemStyle implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private int icitemClassesId; 	// 产品类别id
	private String fnumber;			// 代码
	private String fname; 			// 名称

	private ICItemClasses icItemclasses;

	public ICItemStyle() {
		super();
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIcitemClassesId() {
		return icitemClassesId;
	}
	public void setIcitemClassesId(int icitemClassesId) {
		this.icitemClassesId = icitemClassesId;
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

	public ICItemClasses getIcItemclasses() {
		return icItemclasses;
	}

	public void setIcItemclasses(ICItemClasses icItemclasses) {
		this.icItemclasses = icItemclasses;
	}

	@Override
	public String toString() {
		return "ICItemStyle [id=" + id + ", icitemClassesId=" + icitemClassesId + ", fnumber=" + fnumber + ", fname="
				+ fname + ", icItemclasses=" + icItemclasses + "]";
	}

}

