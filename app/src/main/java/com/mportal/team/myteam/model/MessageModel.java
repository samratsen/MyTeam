package com.mportal.team.myteam.model;

import java.io.Serializable;

public class MessageModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7483189475332210292L;

	private String body;
	private String dir;
	private int mid;
	private String name;
	private String status;
	private long time=0;
	private String tn;
	private String type;
	
	private long dateBreakTime=0;// this is for breaking results into day segments
	private long unreadCount=0;
	private boolean isFailed=false;
	
	// contact provider fields
	private String contactId;
	private String contactName;
	private String contactPhotoURI;
	// End of contact provider fields
	
	public MessageModel(){
		
	}
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public int getMid() {
		return mid;
	}
	public void setMid(int mid) {
		this.mid = mid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getTn() {
		return tn;
	}
	public void setTn(String tn) {
		this.tn = tn;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public long getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(long unreadCount) {
		this.unreadCount = unreadCount;
	}

	public boolean isFailed() {
		return isFailed;
	}

	public void setFailed(boolean isfailed) {
		this.isFailed = isfailed;
	}

	public long getDateBreakTime() {
		return dateBreakTime;
	}

	public void setDateBreakTime(long dateBreakTime) {
		this.dateBreakTime = dateBreakTime;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactPhotoURI() {
		return contactPhotoURI;
	}

	public void setContactPhotoURI(String contactPhotoURI) {
		this.contactPhotoURI = contactPhotoURI;
	}
	
	

}
