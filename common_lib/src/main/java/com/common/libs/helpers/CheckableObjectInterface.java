package com.common.libs.helpers;


import com.common.libs.objects.SimpleObjectInterface;

public interface CheckableObjectInterface extends SimpleObjectInterface {
	public void setChecked(boolean isChecked);
	public boolean isChecked();
	
}
