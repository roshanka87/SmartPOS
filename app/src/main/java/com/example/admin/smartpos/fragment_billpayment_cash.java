package com.example.admin.smartpos;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import static com.justtide.osapp.util.HideKeyBoard.hideKeyboard;

public class fragment_billpayment_cash extends Fragment {

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

		hideKeyboard(getActivity());
		return inflater.inflate(R.layout.fragment_billpayment_cash, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ImageButton relative_Back = (ImageButton) view.findViewById(R.id.relative_BacktoBillPay);
		relative_Back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.relative_BacktoBillPay,null);
			}
		});

		Button button_Mobile = (Button) view.findViewById(R.id.button_Mobile);
		button_Mobile.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.button_Mobile,null);
			}
		});
		Button button_Telephone = (Button) view.findViewById(R.id.button_Telephone);
		button_Telephone.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.button_Telephone,null);
			}
		});
		Button button_Internet = (Button) view.findViewById(R.id.button_Internet);
		button_Internet.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.button_Internet,null);
			}
		});
		Button button_Utility = (Button) view.findViewById(R.id.button_Utility);
		button_Utility.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.button_Utility,null);
			}
		});
		Button button_Finance = (Button) view.findViewById(R.id.button_Finance);
		button_Finance.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.button_Finance,null);
			}
		});

		getActivity().setTitle("Pay by Cash");
	}
}
