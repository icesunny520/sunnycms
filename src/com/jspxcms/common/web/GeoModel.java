/**
 * 
 */
package com.jspxcms.common.web;

/**
 * 
 * 车辆定位信息
 *
 * Create on Mar 4, 2016 8:48:04 PM
 *
 * @author ZhouYan
 * 
 */
public class GeoModel {

	private String longitude;
	private String latitude;
	private String direction;
	private String busNum;

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getBusNum() {
		return busNum;
	}

	public void setBusNum(String busNum) {
		this.busNum = busNum;
	}

}
