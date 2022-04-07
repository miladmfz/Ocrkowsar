package com.kits.ocrkowsar.activity;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.kits.ocrkowsar.R;
import com.kits.ocrkowsar.adapter.Factor_List_adapter;
import com.kits.ocrkowsar.application.CallMethod;
import com.kits.ocrkowsar.model.DatabaseHelper;
import com.kits.ocrkowsar.model.Factor;
import com.kits.ocrkowsar.model.NumberFunctions;
import com.kits.ocrkowsar.model.RetrofitResponse;
import com.kits.ocrkowsar.webService.APIClient;
import com.kits.ocrkowsar.webService.APIInterface;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FactorListActivity extends AppCompatActivity {

    APIInterface apiInterface;
    Factor_List_adapter adapter;
    GridLayoutManager gridLayoutManager;
    RecyclerView factor_list_recycler;
    AppCompatEditText edtsearch;
    Handler handler;
    ArrayList<Factor> factors=new ArrayList<>();
    ArrayList<Factor> selectfactors=new ArrayList<>();
    String srch="";
    TextView textView_Count;
    String state="0",StateEdited="0",StateShortage="0";


    SwitchMaterial RadioEdited;
    SwitchMaterial RadioShortage;
    Spinner spinnerPath;
    String filter="0";
    String path="همه";
    ArrayList<String> customerpath=new ArrayList<>();
    Intent intent;
    NotificationManager notificationManager;
    String channel_id = "Kowsarmobile";
    String channel_name = "home";
    CallMethod callMethod;
    DatabaseHelper dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factor_list);
        Dialog dialog1 = new Dialog(this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(dialog1.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog1.setContentView(R.layout.rep_prog);
        TextView repw = dialog1.findViewById(R.id.rep_prog_text);
        repw.setText("در حال خواندن اطلاعات");
        dialog1.show();
        intent();
        Config();
        try {
            Handler handler = new Handler();
            handler.postDelayed(this::init, 100);
            handler.postDelayed(dialog1::dismiss, 1000);
        }catch (Exception e){
            callMethod.ErrorLog(e.getMessage());
        }



    }
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public  void intent(){
        Bundle bundle =getIntent().getExtras();
        assert bundle != null;
        state = bundle.getString("State");
        StateEdited ="0";
        StateShortage ="0";
        if(state.equals("5"))
        {
            state = "0";
            StateEdited = bundle.getString("StateEdited");
            StateShortage = bundle.getString("StateShortage");
        }
    }

    public void Config() {

        callMethod = new CallMethod(this);
        dbh = new DatabaseHelper(this, callMethod.ReadString("UseSQLiteURL"));
        apiInterface = APIClient.getCleint(callMethod.ReadString("ServerURLUse")).create(APIInterface.class);
        handler=new Handler();

        Toolbar toolbar = findViewById(R.id.factor_listActivity_toolbar);
        setSupportActionBar(toolbar);

        factor_list_recycler=findViewById(R.id.factor_listActivity_recyclerView);
        textView_Count=findViewById(R.id.factorlistActivity_count);
        edtsearch = findViewById(R.id.factorlistActivity_edtsearch);
        RadioEdited= findViewById(R.id.factorlistActivity_edited);
        RadioShortage= findViewById(R.id.factorlistActivity_shortage);
        spinnerPath= findViewById(R.id.factorlistActivity_path);

    }

    public void init(){


        customerpath.add("همه");

        if(!state.equals("0")){
            RadioEdited.setVisibility(View.GONE);
            RadioShortage.setVisibility(View.GONE);
        }

        srch=callMethod.ReadString("Last_search");

        edtsearch.setText(srch);
        edtsearch.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged( Editable editable) {
                        handler.removeCallbacksAndMessages(null);
                        handler.postDelayed(() -> {
                            srch = NumberFunctions.EnglishNumber(editable.toString());
                            retrofitrequset();
                        }, 1000);

                    }
                });


        RadioEdited.setChecked(StateEdited.equals("1"));
        RadioShortage.setChecked(StateShortage.equals("1"));





        spinnerPath.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                path=customerpath.get(position);
                callrecycle();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });







        RadioShortage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) StateShortage="1"; else  StateShortage="0";
            if(StateEdited.equals("0")){
                if(StateShortage.equals("0")) filter="0"; else  filter="1";
            }else {
                if(StateShortage.equals("0")) filter="2"; else  filter="3";
            }
            callrecycle();
        });


        RadioEdited.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) StateEdited="1"; else  StateEdited="0";
            if(StateEdited.equals("0")){
                if(StateShortage.equals("0")) filter="0"; else  filter="1";
            }else {
                if(StateShortage.equals("0")) filter="2"; else  filter="3";
            }
            callrecycle();
        });



        if(StateEdited.equals("0")){
            if(StateShortage.equals("0")) {
                filter="0";
            }else {
                filter="1";
            }
        }else {
            if(StateShortage.equals("0")) {
                filter="2";
            }else {
                filter="3";
            }
        }



        retrofitrequset();
    }




    public void callrecycle() {
        adapter = new Factor_List_adapter(factors,state, filter,path,FactorListActivity.this);
        if (adapter.getItemCount()==0){
             callMethod.showToast("فاکتوری یافت نشد");
        }
        textView_Count.setText(NumberFunctions.PerisanNumber(String.valueOf(adapter.getItemCount())));
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);//grid
        factor_list_recycler.setLayoutManager(gridLayoutManager);
        factor_list_recycler.setAdapter(adapter);
        factor_list_recycler.setItemAnimator(new DefaultItemAnimator());


    }

    public void retrofitpath() {
        Call<RetrofitResponse> call =apiInterface.GetCustomerPath("GetCustomerPath");
        call.enqueue(new Callback<RetrofitResponse>() {
            @Override
            public void onResponse(Call<RetrofitResponse> call, Response<RetrofitResponse> response) {

                if(response.isSuccessful()) {
                    assert response.body() != null;
                    for ( Factor factor : response.body().getFactors()) {
                        customerpath.add(factor.getCustomerPath());
                    }
                    ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(FactorListActivity.this,
                            android.R.layout.simple_spinner_item,customerpath);
                    spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPath.setAdapter(spinner_adapter);
                    spinnerPath.setSelection(0);
                }
            }
            @Override
            public void onFailure(Call<RetrofitResponse> call, Throwable t) {
                finish();
                callMethod.showToast("فاکتوری موجود نمی باشد");
                Log.e("",t.getMessage()); }
        });
    }

    public void retrofitrequset() {
        if(!state.equals("2")){
            Call<RetrofitResponse> call =apiInterface.GetOcrFactorList("GetFactorList",state,srch);
            call.enqueue(new Callback<RetrofitResponse>() {
                @Override
                public void onResponse(Call<RetrofitResponse> call, Response<RetrofitResponse> response) {

                }

                @Override
                public void onFailure(Call<RetrofitResponse> call, Throwable t) {

                }
            });
        }
        if(state.equals("2")){

            Call<RetrofitResponse> call =apiInterface.GetOcrFactorList("GetFactorList",state,srch);
            call.enqueue(new Callback<RetrofitResponse>() {
                @Override
                public void onResponse(Call<RetrofitResponse> call, Response<RetrofitResponse> response) {

                    if(response.isSuccessful()) {
                        assert response.body() != null;
                        factors= response.body().getFactors();
                        if(factors.size()>0){
                            callrecycle();
                            retrofitpath();
                            if(state.equals("0")){
                                for(Factor factor:factors){
                                    if(factor.getHasShortage().equals("1")){
                                        noti_Messaging("کسری", "فکتور دارای کسری موجود است","0");
                                    }
                                    if(factor.getIsEdited().equals("1")){
                                        noti_Messaging("اصلاحی", "فکتور اصلاحی موجود است", "1");
                                    }
                                }
                            }

                        }else {
                            finish();
                            callMethod.showToast("فاکتوری موجود نمی باشد");
                        }

                    }
                }
                @Override
                public void onFailure(Call<RetrofitResponse> call, Throwable t) {
                    finish();
                    callMethod.showToast("فاکتوری موجود نمی باشد");
                    Log.e("",t.getMessage()); }
            });
        }else {

            Call<RetrofitResponse> call =apiInterface.GetOcrFactorList("GetFactorList",state,srch);
            call.enqueue(new Callback<RetrofitResponse>() {
                @Override
                public void onResponse(Call<RetrofitResponse> call, Response<RetrofitResponse> response) {

                    if(response.isSuccessful()) {
                        assert response.body() != null;
                        factors= response.body().getFactors();
                        if(factors.size()>0){
                            callrecycle();
                            retrofitpath();
                        }else {
                            finish();
                            callMethod.showToast("فاکتوری موجود نمی باشد");
                        }

                    }
                }
                @Override
                public void onFailure(Call<RetrofitResponse> call, Throwable t) {
                    finish();
                    callMethod.showToast("فاکتوری موجود نمی باشد");
                    Log.e("",t.getMessage()); }
            });


        }


    }





    @Override
    protected void onRestart() {
        super.onRestart();
        intent = new Intent(FactorListActivity.this, FactorListActivity.class);
        intent.putExtra("State", state);
        startActivity(intent);
        finish();

    }


    public void noti_Messaging(String title, String message,String flag) {

        notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel Channel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(Channel);
        }
        Intent notificationIntent = new Intent(this, FactorListActivity.class);
        notificationIntent.putExtra("State", "5");
        if(flag.equals("0")){
            notificationIntent.putExtra("StateEdited", "0");
            notificationIntent.putExtra("StateShortage", "1");
        }else {
            notificationIntent.putExtra("StateEdited", "1");
            notificationIntent.putExtra("StateShortage", "0");

        }


        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notcompat = new NotificationCompat.Builder(FactorListActivity.this, channel_id)
                .setContentTitle(title)
                .setContentText(message)
                .setOnlyAlertOnce(false)
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(contentIntent);

        notificationManager.notify(1, notcompat.build());
    }

}