package com.activiti.model;

public class ViewFormData {

	private String formKey;
	private String form;

	public ViewFormData(String formKey, String form) {
		super();
		this.formKey = formKey;
		this.form = form;
	}

	public ViewFormData() {
		super();
	}

	public String getFormKey() {
		return formKey;
	}

	public void setFormKey(String formKey) {
		this.formKey = formKey;
	}

	public String getForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

}
