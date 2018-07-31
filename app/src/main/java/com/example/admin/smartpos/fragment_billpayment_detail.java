package com.example.admin.smartpos;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.style.BulletSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.justtide.osapp.util.DialogService;
import com.justtide.osapp.util.HttpClient;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Response;

public class fragment_billpayment_detail extends Fragment {

	private TextView phoneNu;
	private TextView amount;
	private String billType;
	private Button submit;
	private String txtamount;
	private String txtPhoneNu;
	private String bankName;
	private Intent intent;
	private String total;
	private String paymentType = "Bill_Pay";
	private String username;
	private String accnoBilltype;
	private View mProgressView;
	private int min;
	EditText edTxtAmount, edTxtAccountNum;
	private Spinner sp_payment_methods;
	ArrayList<String> values = new ArrayList();
	ArrayList<String> accountRefs = new ArrayList();
	int selectedItem;
	private int PaymentOption = -1;
	String TransactionId = "";
	String TransactionAmount = "";
	private AlertDialog dialog;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


		return inflater.inflate(R.layout.fragment_billpayment_detail, container, false);
	}

	private void read() {

		phoneNu = (TextView) getActivity().findViewById(R.id.editText_AccountNo);
		amount = (TextView) getActivity().findViewById(R.id.editText_BillAmount);
		submit = (Button) getActivity().findViewById(R.id.button_billpayment);
		billType = getArguments().getString("billtype");

		ImageView imgView = (ImageView) getActivity().findViewById(R.id.imgrandom_BillPayment);

		Log.d("bill_payment", "oncreate");


		if (billType.equals("HUTCH")) {
			imgView.setBackgroundResource(R.drawable.ic_hutch_163);
		} else if (billType.equals("MOB")) {
			imgView.setBackgroundResource(R.drawable.ic_mobitel_163);
		} else if (billType.equals("ETI")) {
			imgView.setBackgroundResource(R.drawable.ic_etisalat_163);
		} else if (billType.equals("AIR")) {
			imgView.setBackgroundResource(R.drawable.ic_ic_airtel_163);
		} else if (billType.equals("SLT")) {
			imgView.setBackgroundResource(R.drawable.ic_slt_163);
		} else if (billType.equals("SLTCDM")) {
			imgView.setBackgroundResource(R.drawable.ic_citilink_163);
		} else if (billType.equals("BELPOST")) {
			imgView.setBackgroundResource(R.drawable.ic_lankabell_163);
		} else if (billType.equals("CEB")) {
			imgView.setBackgroundResource(R.drawable.ic_ceb_163);
		} else if (billType.equals("WAT")) {
			imgView.setBackgroundResource(R.drawable.ic_waterboard_163);
		} else if (billType.equals("LEC")) {
			imgView.setBackgroundResource(R.drawable.ic_leco_163);
		} else if (billType.equals("CDB")) {
			imgView.setBackgroundResource(R.drawable.ic_cdb_163);
		} else if (billType.equals("LOL")) {
			imgView.setBackgroundResource(R.drawable.ic_lolc_163);
		} else if (billType.equals("ASI")) {
			imgView.setBackgroundResource(R.drawable.ic_asiance_alliance_163);
		} else if (billType.equals("UNI")) {
			imgView.setBackgroundResource(R.drawable.ic_union_assurence_163);
		} else if (billType.equals("JAN")) {
			imgView.setBackgroundResource(R.drawable.ic_janashakthi_163);
		} else if (billType.equals("AVI")) {
			imgView.setBackgroundResource(R.drawable.ic_aia_163);
		} else if (billType.equals("HNB")) {
			imgView.setBackgroundResource(R.drawable.ic_hnb_163);
		} else if (billType.equals("COO")) {
			imgView.setBackgroundResource(R.drawable.ic_coop_life_163);
		} else if (billType.equals("TXI")) {
			imgView.setBackgroundResource(R.drawable.ic_link_taxi_163);
		} else if (billType.equals("KMC")) {
			imgView.setBackgroundResource(R.drawable.ic_kandy_mc_163);
		} else if (billType.equals("NMC")) {
			imgView.setBackgroundResource(R.drawable.ic_ne_mc_163);
		} else if (billType.equals("BPS")) {
			imgView.setBackgroundResource(R.drawable.ic_bps_163);
		} else if (billType.equals("LIH")) {
			imgView.setBackgroundResource(R.drawable.ic_little_hearts_163);
		} else if (billType.equals("HAG")) {
			imgView.setBackgroundResource(R.drawable.ic_help_age_163);
		}

	}


	private boolean Validation(String PhoneNo) {
		boolean valid = false;
		if (PhoneNo.length() == 10) {
			String Digit = PhoneNo.substring(1, 3);
			if (billType.equals("HUTCH")) {

				if (Digit.equals("78")) {
					valid = true;
				}
			}
			if (billType.equals("MOB")) {
				if (Digit.equals("71") || Digit.equals("70")) {
					valid = true;
				}
			}
			if (billType.equals("ETI")) {
				if (Digit.equals("72")) {
					valid = true;
				}
			}
			if (billType.equals("AIR")) {
				if (Digit.equals("75")) {
					valid = true;
				}
			}
		}
		return valid;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ImageButton relative_Back = (ImageButton) view.findViewById(R.id.relative_BacktoBillPayCashCategory);
		relative_Back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.relative_BacktoBillPayCashCategory, null);
			}
		});


		Button button_payBill = (Button) view.findViewById(R.id.button_payBill);
		button_payBill.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AsyncTask_Validation validation = new AsyncTask_Validation();
				validation.execute();

			}
		});

		EditText txtAccountNo = (EditText) getActivity().findViewById(R.id.editText_AccountNo);
		txtAccountNo.requestFocus();

		read();
		getActivity().setTitle("BIll Payment");
	}

	class AsyncTask_Validation extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {

				EditText txtAmount = (EditText) getActivity().findViewById(R.id.editText_BillAmount);
				EditText txtAccountNo = (EditText) getActivity().findViewById(R.id.editText_AccountNo);
				TransactionAmount = txtAmount.getText().toString();

				if (Validation(txtAccountNo.getText().toString()) == false) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
							builder.setMessage("Invalied Mobile No");
							dialog = builder.create();
							dialog.show();
						}
					});

					return null;
				}


				String AmtVal = txtAmount.getText().toString();
				if (AmtVal.equals("")) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
							builder.setMessage("Invalied Amount");
							dialog = builder.create();
							dialog.show();
						}
					});

					return null;
				}

				double Amt = 0;
				Amt = Double.valueOf(txtAmount.getText().toString());
				if (Amt == 0) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
							builder.setMessage("Invalied Amount");
							dialog = builder.create();
							dialog.show();
						}
					});

					return null;
				}
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						DialogService.progressDialog((activity_main) getContext(), true);
					}
				});
				String url = "http://" + getString(R.string.server_name) + "/smartpos/WebAPI.php?Type=TransactionCommit&TerminalId=1&DocumentType=BP&Amount=" + txtAmount.getText().toString() + "&AccountNo=" + txtAccountNo.getText().toString() + "";
				HttpClient client = new HttpClient();
				Response response = client.get(url);

				if (!response.isSuccessful()) {
					return null;
				} else {

					String result = response.body().string();
					JSONObject reader = new JSONObject(result);
					JSONArray nameTable = reader.getJSONArray("Result");
					JSONObject item = nameTable.getJSONObject(0);
					TransactionId = item.getString("TranId");

					if (!TransactionId.toString().isEmpty()) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								String Para = TransactionId + "-" + TransactionAmount + "- Bill Payment";
								((activity_main) getActivity()).displaySelectedScreen(R.id.button_Pay, Para);
							}
						});


					} else {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
								builder.setCancelable(false);
								builder.setMessage("Some error found");
								dialog = builder.create();
								dialog.show();
							}
						});

					}

					DialogService.progressDialog((activity_main) getContext(), false);
				}
			} catch (IOException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DialogService.progressDialog((activity_main) getContext(), false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("IOException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
					}
				});
			} catch (JSONException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DialogService.progressDialog((activity_main) getContext(), false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("JSONException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
					}
				});
			} catch (NetworkOnMainThreadException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DialogService.progressDialog((activity_main) getContext(), false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("NetworkOnMainThreadException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
					}
				});
			}
			return null;
		}
	}

}
