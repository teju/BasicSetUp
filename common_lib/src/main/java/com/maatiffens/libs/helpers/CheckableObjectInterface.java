package com.maatiffens.libs.helpers;


import com.maatiffens.libs.objects.SimpleObjectInterface;

public interface CheckableObjectInterface extends SimpleObjectInterface {
	public void setChecked(boolean isChecked);
	public boolean isChecked();
	
}
