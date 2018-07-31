package com.example.admin.smartpos;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import static com.justtide.osapp.util.HideKeyBoard.hideKeyboard;

public class fragment_billpayment_category extends Fragment implements View.OnClickListener {

	private String one;

	private String a;
	private String b;
	private String c;
	private String d;
	private String e;
	private String f;
	private String g;


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

		hideKeyboard(getActivity());
		return inflater.inflate(R.layout.fragment_billpayment_category, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setValues();

		ImageButton relative_Back = (ImageButton) view.findViewById(R.id.relative_BacktoBillPayCash);
		relative_Back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.relative_BacktoBillPayCash,null);
			}
		});

		ImageView billOne=(ImageView) view.findViewById(R.id.bill_one);
		billOne.setOnClickListener(this);

		ImageView billtwo=(ImageView) view.findViewById(R.id.bill_two);
		billtwo.setOnClickListener(this);

		ImageView billfur=(ImageView) view.findViewById(R.id.bill_four);
		billfur.setOnClickListener(this);

		ImageView billfive=(ImageView) view.findViewById(R.id.bill_five);
		billfive.setOnClickListener(this);

		ImageView billsix=(ImageView) view.findViewById(R.id.bill_six);
		billsix.setOnClickListener(this);

		ImageView billseven=(ImageView) view.findViewById(R.id.bill_seven);
		billseven.setOnClickListener(this);


		getActivity().setTitle("Pay by Cash");
	}


	private void setValues() {

		one = getArguments().getString("1");

		ImageView eka = (ImageView) getActivity().findViewById(R.id.bill_one);
		ImageView two = (ImageView) getActivity().findViewById(R.id.bill_two);
		ImageView three = (ImageView) getActivity().findViewById(R.id.bill_four);
		ImageView four = (ImageView) getActivity().findViewById(R.id.bill_five);
		ImageView five = (ImageView) getActivity().findViewById(R.id.bill_six);
		ImageView six = (ImageView) getActivity().findViewById(R.id.bill_seven);
		TextView donations = (TextView) getActivity().findViewById(R.id.donations);


		if (one.contains("mobile")) {
			eka.setBackgroundResource(R.drawable.ic_hutch_163);
			a = "HUTCH";
			two.setBackgroundResource(R.drawable.ic_mobitel_163);
			b = "MOB";
			three.setBackgroundResource(R.drawable.ic_etisalat_163);
			c = "ETI";
			four.setBackgroundResource(R.drawable.ic_ic_airtel_163);
			d = "AIR";

			five.setVisibility(View.GONE);
			six.setVisibility(View.GONE);

		} else if (one.contains("telephone")) {
			eka.setBackgroundResource(R.drawable.ic_slt_163);
			a = "SLT";
			two.setBackgroundResource(R.drawable.ic_citilink_163);
			b = "SLTCDM";
			three.setBackgroundResource(R.drawable.ic_lankabell_163);
			c = "BELPOST";

			four.setVisibility(View.GONE);
			five.setVisibility(View.GONE);
			six.setVisibility(View.GONE);

		} else if (one.contains("utility")) {

			eka.setBackgroundResource(R.drawable.ic_ceb_163);
			a = "CEB";
			two.setBackgroundResource(R.drawable.ic_waterboard_163);
			b = "WAT";
			three.setBackgroundResource(R.drawable.ic_leco_163);
			c = "LEC";


			four.setVisibility(View.GONE);
			five.setVisibility(View.GONE);
			six.setVisibility(View.GONE);

		} else if (one.contains("finance")) {

			eka.setBackgroundResource(R.drawable.ic_cdb_163);
			a = "CDB";
			two.setBackgroundResource(R.drawable.ic_lolc_163);
			b = "LOL";

			three.setVisibility(View.GONE);
			four.setVisibility(View.GONE);
			five.setVisibility(View.GONE);
			six.setVisibility(View.GONE);


		} else if (one.contains("Insurance")) {

			eka.setBackgroundResource(R.drawable.ic_asiance_alliance_163);
			a = "ASI";
			two.setBackgroundResource(R.drawable.ic_union_assurence_163);
			b = "UNI";
			three.setBackgroundResource(R.drawable.ic_janashakthi_163);
			c = "JAN";
			four.setBackgroundResource(R.drawable.ic_aia_163);
			d = "AVI";
			five.setBackgroundResource(R.drawable.ic_hnb_163);
			e = "HNB";
			six.setBackgroundResource(R.drawable.ic_coop_life_163);
			f = "COO";


		} else if (one.contains("eWallets")) {
			eka.setBackgroundResource(R.drawable.ic_mcash_163);
			a = "MOB";

			two.setVisibility(View.GONE);
			three.setVisibility(View.GONE);
			four.setVisibility(View.GONE);
			five.setVisibility(View.GONE);
			six.setVisibility(View.GONE);

		} else if (one.contains("Events")) {
		} else if (one.contains("Taxi")) {
			eka.setBackgroundResource(R.drawable.ic_link_taxi_163);
			a = "TXI";

			two.setVisibility(View.GONE);
			three.setVisibility(View.GONE);
			four.setVisibility(View.GONE);
			five.setVisibility(View.GONE);
			six.setVisibility(View.GONE);

		} else if (one.contains("Online")) {
		} else if (one.contains("Government")) {

			eka.setBackgroundResource(R.drawable.ic_kandy_mc_163);
			a = "KMC";
			two.setBackgroundResource(R.drawable.ic_ne_mc_163);
			b = "NMC";
			three.setBackgroundResource(R.drawable.ic_bps_163);
			c = "BPS";

			four.setVisibility(View.GONE);
			five.setVisibility(View.GONE);
			six.setVisibility(View.GONE);


		} else if (one.contains("other")) {
			eka.setBackgroundResource(R.drawable.ic_little_hearts_163);
			a = "LIH";
			two.setBackgroundResource(R.drawable.ic_help_age_163);
			b = "HAG";


			donations.setText("Other");
			three.setVisibility(View.GONE);
			four.setVisibility(View.GONE);
			five.setVisibility(View.GONE);
			six.setVisibility(View.GONE);
		} else if (one.contains("internet")) {

			eka.setBackgroundResource(R.drawable.ic_mobitel_163);
			a = "MOB";
			two.setBackgroundResource(R.drawable.ic_etisalat_163);
			b = "ETI";
			three.setBackgroundResource(R.drawable.ic_hutch_163);
			c = "HUTCH";
			four.setBackgroundResource(R.drawable.ic_ic_airtel_163);
			d = "AIR";

			five.setVisibility(View.GONE);
			six.setVisibility(View.GONE);
		}

	}

	public void onClick(View v) {
		if (v.getId() == R.id.bill_one) {
			((activity_main) getActivity()).displaySelectedScreen(R.id.bill_one,a);

		} else if (v.getId() == R.id.bill_two) {
			((activity_main) getActivity()).displaySelectedScreen(R.id.bill_two,b);

		} else if (v.getId() == R.id.bill_four) {
			((activity_main) getActivity()).displaySelectedScreen(R.id.bill_four,c);

		} else if (v.getId() == R.id.bill_five) {
			((activity_main) getActivity()).displaySelectedScreen(R.id.bill_five,d);

		} else if (v.getId() == R.id.bill_six) {
			((activity_main) getActivity()).displaySelectedScreen(R.id.bill_six,e);

		} else if (v.getId() == R.id.bill_seven) {
			((activity_main) getActivity()).displaySelectedScreen(R.id.bill_seven,f);
		}
	}
}
