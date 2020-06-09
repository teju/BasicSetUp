package com.maatiffens.libs.generics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maatiffens.common_library.R;

public class GenericListFragment extends GenericFragment {

	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_generic_list, container, false);
	}
}
