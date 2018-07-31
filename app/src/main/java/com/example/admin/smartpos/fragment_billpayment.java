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

/**
 * Created by admin on 10/07/2018.
 */

public class fragment_billpayment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

		hideKeyboard(getActivity());
        return inflater.inflate(R.layout.fragment_billpayment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton relative_Back = (ImageButton) view.findViewById(R.id.relative_BacktoHome);
        relative_Back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((activity_main) getActivity()).displaySelectedScreen(R.id.relative_BacktoHome,null);
            }
        });

        Button button_CashPayment = (Button) view.findViewById(R.id.button_CashPayment);
		button_CashPayment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((activity_main) getActivity()).displaySelectedScreen(R.id.button_CashPayment,null);
            }
        });

		Button button_CardPayment = (Button) view.findViewById(R.id.button_CardPayment);
		button_CardPayment.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.button_CashPayment,null);
			}
		});

		Button button_DigitlPayment = (Button) view.findViewById(R.id.button_DigitlPayment);
		button_DigitlPayment.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.button_CashPayment,null);
			}
		});

        getActivity().setTitle("Bill Payment");
    }

}
