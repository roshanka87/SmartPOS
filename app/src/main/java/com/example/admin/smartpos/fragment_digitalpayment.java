package com.example.admin.smartpos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.messaging.FirebaseMessaging;
import com.justtide.osapp.util.DialogService;
import com.justtide.osapp.util.HttpClient;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import justtide.ThermalPrinter;
import okhttp3.Response;

import static com.example.admin.smartpos.fragment_cardpayment.TAG;
import static com.justtide.osapp.util.HideKeyBoard.hideKeyboard;


public class fragment_digitalpayment extends Fragment {

	private AlertDialog dialog;
	String TransactionId = "";
	String TransactionAmount = "";
	private boolean bLogo ;
	private int ilevel = 0;
	private String ticketString = "";
	private byte[] buffer;
	private Thread mThread;
	ThermalPrinter thermalPrinter = ThermalPrinter.getInstance();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		LocalBroadcastManager.getInstance(getContext()).registerReceiver(mRegistrationBroadcastReceiver,
				new IntentFilter("Parameter.FCM_PUSH_NOTIFICATION"));
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


		return inflater.inflate(R.layout.fragment_digitalpayment, container, false);
	}

	@Override
	public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		EditText txtAmount = (EditText) getActivity().findViewById(R.id.editText_Amount);
		txtAmount.requestFocus();

		ImageButton relative_Back = (ImageButton) view.findViewById(R.id.relative_BacktoHome);
		relative_Back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.relative_BacktoHome, null);
			}
		});


		Button button_Pay = (Button) view.findViewById(R.id.button_Pay);
		button_Pay.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {


				AsyncTask_Validation validation = new AsyncTask_Validation();
				validation.execute();
			}
		});


		getActivity().setTitle("Digital Payment");
	}


	private BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("Parameter.FCM_PUSH_NOTIFICATION")) {
				String Value = intent.getStringExtra("fcmData").toString();
				String[] separated = Value.split(":");


				String Result = separated[1].toString();

				Log.i(TAG, "onReceive: " + Result);
				if (Result.equals(TransactionId)) {
					String Para = TransactionId + "-" + TransactionAmount + "- Digital Payment";
					((activity_main) getActivity()).displaySelectedScreen(R.id.button_Pay, Para);

//					printTicket();
				}
			}
		}
	};

	class AsyncTask_Validation extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {

				EditText txtAmount = (EditText) getActivity().findViewById(R.id.editText_Amount);

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
				Amt = Double.parseDouble(txtAmount.getText().toString());
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

				String url = "http://" + getString(R.string.server_name) + "/smartpos/WebAPI.php?Type=TransactionCommit&TerminalId=1&DocumentType=DP&Amount=" + txtAmount.getText().toString() + "&AccountNo=";
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

								EditText txtAmount = (EditText) getActivity().findViewById(R.id.editText_Amount);
								TransactionAmount = txtAmount.getText().toString();

								double FinalAmt = Double.parseDouble(TransactionAmount);
								String ConvVal = "";
								ConvVal=String.format("%.2f", FinalAmt);

								String code = "da842c8e-1d08-4e43-bdb0-e2a25ec30dee " + ConvVal + " main " + TransactionId + "";//<id> <amount(10.00)> main <orderId>;

								DisplayMetrics displayMetrics = new DisplayMetrics();
								getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
								int width_px = Resources.getSystem().getDisplayMetrics().widthPixels;
								int height_px = Resources.getSystem().getDisplayMetrics().heightPixels;

								int pixeldpi = Resources.getSystem().getDisplayMetrics().densityDpi;
								float pixeldp = Resources.getSystem().getDisplayMetrics().density;

								int width_dp = (width_px / pixeldpi) * 160;
								int height_dp = (height_px / pixeldpi) * 160;

								Bitmap myBitmap = QRCode.from("" + code).withSize(width_px, width_px).bitmap();
								ImageView myImage = (ImageView) getActivity().findViewById(R.id.imageView_Qr);
								myImage.setImageBitmap(myBitmap);

								printTicket();

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
					hideKeyboard(getActivity());
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

	private int printTicket()
	{
		buffer = new byte[48];
		int i,state;
		int ret = -1;
		if (bLogo == true) {
			thermalPrinter.initBuffer();
			thermalPrinter.setGray(ilevel);
			Resources res= getResources();
			Bitmap bitmap1 =  BitmapFactory.decodeResource(res,R.drawable.justtide1);
			thermalPrinter.printLogo(0,0,bitmap1);
			thermalPrinter.setStep(200);
			thermalPrinter.printStart();
			state = thermalPrinter.waitForPrintFinish();
			bLogo = false;
			return state;
		}

		thermalPrinter.initBuffer();

		if (Build.BOARD.equals("msm8909")) {
			boolean paper = thermalPrinter.isPaperOut();
			if (paper) {
				thermalPrinter.feedPaper(52);
				thermalPrinter.waitForPrintFinish();
				paper = thermalPrinter.isPaperOut();
				if(paper){
					mThread = null;
					return thermalPrinter.NO_PAPER;
				}
			}
		}

		thermalPrinter.setGray(ilevel);
		thermalPrinter.setHeightAndLeft(0, 0);
		thermalPrinter.setLineSpacing(5);
		thermalPrinter.setDispMode(ThermalPrinter.UMODE);
		ret = thermalPrinter.setFont(ThermalPrinter.ASC8X16_DEF,ThermalPrinter.HZK24F);
		ret =thermalPrinter.getFontCH();
		Log.i("Print","setFontCH:"+ret);
		ticketString += "POS SALES SLIP\n";
		thermalPrinter.setFont(ThermalPrinter.ASC8X16_DEF,ThermalPrinter.HZK12);
		thermalPrinter.print("MERCHANT COPY\n");
		ticketString += "MERCHANT COPY\n";
		for(i=0;i<27;i++) {
			buffer[i]= (byte) 0xfc;
		}
		ticketString += "___________________________\n";
		thermalPrinter.printLine(buffer);
		thermalPrinter.printLine(buffer);
		thermalPrinter.setStep(4);
		//System.out.println("setStep:"+ret);
		thermalPrinter.setFont(ThermalPrinter.ASC12X24,ThermalPrinter.HZK12);
		thermalPrinter.print("Custom name：\n");
		//thermalPrinter.print("(MERCHANT NAME):\n");
		ticketString += "(MERCHANT NAME):\n";
		thermalPrinter.print("(MERCHANT NO):\n");
		ticketString += "(MERCHANT NO):\n";
		thermalPrinter.shiftRight(100);
		thermalPrinter.print("001420183990573\n");
		ticketString += "001420183990573\n";
		thermalPrinter.print("(TERMINAL NO):00026715\n");
		ticketString += "(TERMINAL NO):00026715\n";
		thermalPrinter.print("(CARD NO):\n");
		ticketString += "(CARD NO):\n";
		thermalPrinter.shiftRight(60);
		thermalPrinter.print("955880******9503920\n");
		ticketString += "955880******9503920\n";
		thermalPrinter.print("(ACQUIRER):03050011\n");
		ticketString += "(ACQUIRER):03050011\n";
		thermalPrinter.print("(TXN TYPE): SALE\n");
		ticketString += "(TXN TYPE): SALE\n";
		thermalPrinter.print("(BATCH NO)  :000023\n");
		ticketString += "(BATCH NO)  :000023\n";
		thermalPrinter.print("(VOUCHER NO):000018\n");
		ticketString += "(VOUCHER NO):000018\n";
		thermalPrinter.print("(AUTH NO)   :987654\n");
		ticketString += "(AUTH NO)   :987654\n";
		thermalPrinter.print("(DATE/TIME):\n");
		ticketString += "(DATE/TIME):\n";
		thermalPrinter.shiftRight(80);
		thermalPrinter.print("2012/02/10 10:14:39\n");
		ticketString += "2012/02/10 10:14:39\n";
		thermalPrinter.print("(REF  NO):201202100015\n");
		ticketString += "(REF  NO):201202100015\n";
		thermalPrinter.print("(AMOUNT)      RMB:2.55\n");
		ticketString += "(AMOUNT)      RMB:2.55\n";
		thermalPrinter.setStep(12);
		for(i=0;i<48;i++)
			buffer[i]=(byte) 0xfc;
		ticketString += "_____________________________________\n";
		thermalPrinter.printLine(buffer);
		thermalPrinter.printLine(buffer);
		thermalPrinter.setStep(12);
		thermalPrinter.print("REFERENCE\n");
		ticketString += "REFERENCE\n";
		thermalPrinter.setStep(12);
		ticketString += "_____________________________________\n";
		thermalPrinter.printLine(buffer);
		thermalPrinter.printLine(buffer);
		thermalPrinter.setStep(12);
		thermalPrinter.setFont(ThermalPrinter.ASC8X16B,ThermalPrinter.HZK12);
		thermalPrinter.print("SIGNATURE\n");
		ticketString += "(CARDHOLDER SIGNATURE)\n";
		thermalPrinter.setStep(30);
		Arrays.fill(buffer,(byte) 0);
		for(i=0;i<40;i++)
			buffer[i]=(byte) 0xfc;
		ret = thermalPrinter.printLine(buffer);
		thermalPrinter.printLine(buffer);
		ticketString += "_____________________________________\n";
		thermalPrinter.setStep(4);
		thermalPrinter.print("I ACKNOWLEDGE SATISFACTORY RECEIPT\n");
		ticketString += "I ACKNOWLEDGE SATISFACTORY RECEIPT\n";
		thermalPrinter.print("  OF RELATIVE GOODS/SERVICE\n");
		ticketString += "  OF RELATIVE GOODS/SERVICE\n";

		thermalPrinter.setStep(200);
		ret=thermalPrinter.printStart();
		state = thermalPrinter.waitForPrintFinish();
		return state;
	}

}
