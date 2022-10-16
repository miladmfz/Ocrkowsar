package com.kits.ocrkowsar.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.kits.ocrkowsar.R;
import com.kits.ocrkowsar.activity.ConfirmActivity;
import com.kits.ocrkowsar.activity.FactorActivity;
import com.kits.ocrkowsar.activity.LocalFactorListActivity;
import com.kits.ocrkowsar.application.CallMethod;
import com.kits.ocrkowsar.model.AppOcrFactor;
import com.kits.ocrkowsar.model.DatabaseHelper;
import com.kits.ocrkowsar.model.Factor;
import com.kits.ocrkowsar.model.NumberFunctions;
import com.kits.ocrkowsar.model.RetrofitResponse;
import com.kits.ocrkowsar.webService.APIClient;
import com.kits.ocrkowsar.webService.APIInterface;
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OcrFactorList_Adapter extends RecyclerView.Adapter<OcrFactorList_Adapter.facViewHolder> {
    APIInterface apiInterface ;

    private final Context mContext;
    Intent intent;
    ArrayList<Factor> factors ;

    Action action;
    String state ;
    String filter;
    String path;
    CallMethod callMethod;

    DatabaseHelper dbh;
    Dialog dialog;
    public OcrFactorList_Adapter(ArrayList<Factor> retrofitFactors,String State, Context context) {
        this.mContext = context;
        this.callMethod = new CallMethod(context);
        this.action=new Action(context);
        this.dbh = new DatabaseHelper(mContext, callMethod.ReadString("DatabaseName"));
        this.state = State;
        this.factors = retrofitFactors;
        apiInterface = APIClient.getCleint(callMethod.ReadString("ServerURLUse")).create(APIInterface.class);

    }

    @NonNull
    @Override
    public facViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.factor_list, parent, false);
        return new facViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final facViewHolder holder, final int position) {



        Factor factor =factors.get(position);

        holder.fac_customer.setText(NumberFunctions.PerisanNumber(factor.getCustName()));
        holder.fac_code.setText(NumberFunctions.PerisanNumber(factor.getFactorPrivateCode()));
        holder.fac_customercode.setText(NumberFunctions.PerisanNumber(factors.get(position).getCustomerCode()));

        if (factors.get(position).getExplain() != null&&factors.get(position).getExplain().length()>0){
            holder.fac_factor_explain_ll.setVisibility(View.VISIBLE);
            holder.fac_explain.setText(NumberFunctions.PerisanNumber(factors.get(position).getExplain()));
        }else {
            holder.fac_factor_explain_ll.setVisibility(View.GONE);
        }


        holder.fac_factor_state_ll.setVisibility(View.GONE);

        if(state.equals("0")){
            try {

                holder.fac_stackclass.setText(NumberFunctions.PerisanNumber(factors.get(position).getStackClass().substring(1)));
                if(factor.getIsEdited().equals("1")){
                    holder.fac_factor_state_ll.setVisibility(View.VISIBLE);
                    holder.fac_hasedite.setText("اصلاح شده");
                }else {
                    holder.fac_hasedite.setText(" ");
                }
                if(factor.getHasShortage().equals("1")){
                    holder.fac_factor_state_ll.setVisibility(View.VISIBLE);
                    holder.fac_hasshortage.setText("کسری موجودی");
                }else {
                    holder.fac_hasshortage.setText(" ");
                }
            }catch (Exception ignored){}
        }

        holder.fac_kowsardate.setText(NumberFunctions.PerisanNumber(factor.getFactorDate()));




        if(factors.get(position).getAppIsControled().equals("1")) {
            if (factors.get(position).getAppIsPacked().equals("1")) {
                if (factors.get(position).getAppIsDelivered().equals("1")) {
                    if (factors.get(position).getHasSignature().equals("1")) {
                        holder.fac_state.setText("تحویل شده");
                    }else {
                        holder.fac_state.setText("باربری");
                    }
                }else {
                    holder.fac_state.setText("آماده ارسال");
                }
            }else {
                holder.fac_state.setText("بسته بندی");
            }
        }else {
            holder.fac_state.setText("انبار");
        }




        if(callMethod.ReadString("Category").equals("4")) {
            holder.fac_factor_btn.setText("دریافت فاکتور");
        }

        if(state.equals("4")){
            holder.fac_stackclass.setText(NumberFunctions.PerisanNumber(factors.get(position).getStackClass().substring(1)));
            holder.fac_factor_btn.setVisibility(View.GONE);
        }else {
            holder.fac_factor_btn.setVisibility(View.VISIBLE);
        }



        if(callMethod.ReadString("Category").equals("5")){
            holder.fac_factor_btn.setText("اصلاح اطلاعات");
            holder.fac_factor_btn.setVisibility(View.VISIBLE);

        }



        holder.fac_factor_btn.setOnClickListener(v -> {

            if(factors.get(position).getStackClass().substring(1).length()>0){

                if(callMethod.ReadString("Category").equals("5")) {
                    Call<RetrofitResponse> call = apiInterface.GetOcrFactorDetail(
                            "GetOcrFactorDetail",
                            factors.get(position).getAppOCRFactorCode()
                            );
                    call.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<RetrofitResponse> call, @NonNull Response<RetrofitResponse> response) {
                            if(response.isSuccessful()) {
                                assert response.body() != null;
                                AppOcrFactor appOcrFactor=response.body().getAppOcrFactors().get(0);
                                action.factor_detail(appOcrFactor);
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<RetrofitResponse> call, @NonNull Throwable t) {}
                    });

                }else {
                    if (position < 5) {

                        if (callMethod.ReadString("Category").equals("4")) {
                            callMethod.EditString("LastTcPrint", factors.get(position).getAppTcPrintRef());

                            Call<RetrofitResponse> call = apiInterface.CheckState("OcrDeliverd", factor.getAppOCRFactorCode(), "1", callMethod.ReadString("Deliverer"));
                            call.enqueue(new Callback<>() {
                                @Override
                                public void onResponse(@NonNull Call<RetrofitResponse> call, @NonNull Response<RetrofitResponse> response) {
                                    if (response.isSuccessful()) {
                                        Log.e("test", "0");
                                        assert response.body() != null;
                                        if (response.body().getFactors().get(0).getErrCode().equals("0")) {
                                            intent = new Intent(mContext, FactorActivity.class);
                                            intent.putExtra("ScanResponse", factor.getAppTcPrintRef());
                                            intent.putExtra("FactorImage", "");
                                            mContext.startActivity(intent);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<RetrofitResponse> call, @NonNull Throwable t) {
                                    Log.e("test", "1");

                                    Log.e("test", t.getMessage());
                                }
                            });


                        } else {

                            callMethod.EditString("LastTcPrint", factors.get(position).getAppTcPrintRef());

                            intent = new Intent(mContext, ConfirmActivity.class);
                            intent.putExtra("ScanResponse", factor.getAppTcPrintRef());
                            intent.putExtra("State", state);
                            mContext.startActivity(intent);
                        }
                    } else {
                        Toast.makeText(mContext, "فاکتور های قبلی را تکمیل کنید", Toast.LENGTH_SHORT).show();
                    }

                }
            }else{
                Toast.makeText(mContext, "فاکتور خالی می باشد", Toast.LENGTH_SHORT).show();
            }


        });

    }

    @Override
    public int getItemCount() {
        return factors.size();
    }

    static class facViewHolder extends RecyclerView.ViewHolder {
        private final TextView fac_customer;
        private final TextView fac_customercode;
        private final TextView fac_code;
        private final TextView fac_hasedite;
        private final TextView fac_hasshortage;
        private final TextView fac_kowsardate;
        private final TextView fac_state;
        private final TextView fac_explain;
        private final TextView fac_stackclass;
        private final Button fac_factor_btn;
        private final LinearLayout fac_factor_explain_ll;
        private final LinearLayout fac_factor_state_ll;

        MaterialCardView fac_rltv;

        facViewHolder(View itemView) {
            super(itemView);

            fac_customer = itemView.findViewById(R.id.factor_list_customer);
            fac_customercode = itemView.findViewById(R.id.factor_list_customercode);
            fac_factor_explain_ll = itemView.findViewById(R.id.factor_list_ll_explain);
            fac_factor_state_ll = itemView.findViewById(R.id.factor_list_ll_state);
            fac_stackclass = itemView.findViewById(R.id.factor_list_stackclass);

            fac_code = itemView.findViewById(R.id.factor_list_privatecode);
            fac_hasedite = itemView.findViewById(R.id.factor_list_hasedited);
            fac_hasshortage = itemView.findViewById(R.id.factor_list_hasshortage);
            fac_kowsardate = itemView.findViewById(R.id.factor_list_kowsardate);
            fac_state = itemView.findViewById(R.id.factor_list_state);
            fac_factor_btn = itemView.findViewById(R.id.factor_list_btn);
            fac_explain = itemView.findViewById(R.id.factor_list_explain);

            fac_rltv = itemView.findViewById(R.id.factor_list);
        }
    }

//
//    public void Pack_detail(String FactorOcrCode){
//
//        dialog = new Dialog(mContext);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.pack_header);
//
//        ArrayList<String> arrayList1,arrayList2,arrayList3;
//        arrayList1=dbh.Packdetail("Reader");
//        arrayList2=dbh.Packdetail("Controler");
//        arrayList3=dbh.Packdetail("pack");
//        MaterialButton btn_pack_h_send =  dialog.findViewById(R.id.pack_header_send);
//        MaterialButton btn_pack_h_1 =  dialog.findViewById(R.id.pack_header_btn1);
//        MaterialButton btn_pack_h_2 =  dialog.findViewById(R.id.pack_header_btn2);
//        MaterialButton btn_pack_h_3 =  dialog.findViewById(R.id.pack_header_btn3);
//        MaterialButton btn_pack_h_5 =  dialog.findViewById(R.id.pack_header_btn5);
//        Spinner sp_pack_h_1 = dialog.findViewById(R.id.pack_header_spinner1);
//        Spinner sp_pack_h_2 = dialog.findViewById(R.id.pack_header_spinner2);
//        Spinner sp_pack_h_3 = dialog.findViewById(R.id.pack_header_spinner3);
//        ArrayAdapter<String> sp_adapter_1 = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, arrayList1);
//        ArrayAdapter<String> sp_adapter_2 = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item,arrayList2);
//        ArrayAdapter<String> sp_adapter_3 = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item,arrayList3);
//        sp_adapter_1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp_adapter_2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp_adapter_3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp_pack_h_1.setAdapter(sp_adapter_1);
//        sp_pack_h_2.setAdapter(sp_adapter_2);
//        sp_pack_h_3.setAdapter(sp_adapter_3);
//
//        LinearLayoutCompat ll_pack_h_new_1 = dialog.findViewById(R.id.pack_new_reader);
//        LinearLayoutCompat ll_pack_h_new_2 = dialog.findViewById(R.id.pack_new_control);
//        LinearLayoutCompat ll_pack_h_new_3 = dialog.findViewById(R.id.pack_new_pack);
//        MaterialButton btn_pack_h_new_1 = dialog.findViewById(R.id.pack_new_btn1);
//        MaterialButton btn_pack_h_new_2 = dialog.findViewById(R.id.pack_new_btn2);
//        MaterialButton btn_pack_h_new_3 = dialog.findViewById(R.id.pack_new_btn3);
//        EditText ed_pack_h_new_1 = dialog.findViewById(R.id.pack_new_ed1);
//        EditText ed_pack_h_new_2 = dialog.findViewById(R.id.pack_new_ed2);
//        EditText ed_pack_h_new_3 = dialog.findViewById(R.id.pack_new_ed3);
//        EditText ed_pack_h_amount = dialog.findViewById(R.id.pack_header_packamount);
//        TextView ed_pack_h_date = dialog.findViewById(R.id.pack_header_senddate);
//
//
//
//        PersianCalendar persianCalendar = new PersianCalendar();
//        String tmonthOfYear,tdayOfMonth;
//        tmonthOfYear="0"+ persianCalendar.getPersianMonth();
//        tdayOfMonth ="0"+ persianCalendar.getPersianDay();
//        String date = persianCalendar.getPersianYear()+"-"
//                + tmonthOfYear.substring(tmonthOfYear.length()-2)+"-"
//                + tdayOfMonth.substring(tdayOfMonth.length()-2);
//
//        ed_pack_h_date.setText(date);
//
//        final String[] reader_s = {""};
//        final String[] coltrol_s = {""};
//        final String[] pack_s = {""};
//        final String[] packCount = {""};
//
//        btn_pack_h_send.setOnClickListener(v -> {
//
//            int pack_r =sp_pack_h_1.getSelectedItemPosition();
//            int pack_c =sp_pack_h_2.getSelectedItemPosition();
//            int pack_d =sp_pack_h_3.getSelectedItemPosition();
//
//
//            reader_s[0] =arrayList1.get(pack_r);
//            coltrol_s[0] =arrayList2.get(pack_c);
//            pack_s[0] =arrayList3.get(pack_d);
//            packCount[0] =ed_pack_h_amount.getText().toString();
//
//            if(reader_s[0].length()<1){
//                reader_s[0] =" ";
//            }
//            if(coltrol_s[0].length()<1){
//                coltrol_s[0] =" ";
//            }
//            if(pack_s[0].length()<1){
//                pack_s[0] =" ";
//            }
//            if(packCount[0].length()<1){
//                packCount[0] ="1";
//            }
//
//
//            Call<RetrofitResponse> call2 =apiInterface.SetPackDetail("SetPackDetail",FactorOcrCode, reader_s[0], coltrol_s[0], pack_s[0],date, packCount[0]);
//            call2.enqueue(new Callback<>() {
//                @Override
//                public void onResponse(@NonNull Call<RetrofitResponse> call, @NonNull Response<RetrofitResponse> response) {
//                    dialog.dismiss();
//                    ((Activity) mContext).finish();
//                }
//                @Override
//                public void onFailure(@NonNull Call<RetrofitResponse> call, @NonNull Throwable t) {
//                    Log.e("",t.getMessage()); }
//            });
//
//        });
//        btn_pack_h_new_1.setOnClickListener(v -> {
//            dbh.Insert_Packdetail("Reader",ed_pack_h_new_1.getText().toString());
//            dialog.dismiss();
//            Pack_detail(FactorOcrCode);
//        });
//        btn_pack_h_new_2.setOnClickListener(v -> {
//            dbh.Insert_Packdetail("Controler",ed_pack_h_new_2.getText().toString());
//            dialog.dismiss();
//            Pack_detail(FactorOcrCode);
//        });
//        btn_pack_h_new_3.setOnClickListener(v -> {
//            dbh.Insert_Packdetail("pack",ed_pack_h_new_3.getText().toString());
//            dialog.dismiss();
//            Pack_detail(FactorOcrCode);
//        });
//
//
//        btn_pack_h_1.setOnClickListener(v -> ll_pack_h_new_1.setVisibility(View.VISIBLE));
//        btn_pack_h_2.setOnClickListener(v -> ll_pack_h_new_2.setVisibility(View.VISIBLE));
//        btn_pack_h_3.setOnClickListener(v -> ll_pack_h_new_3.setVisibility(View.VISIBLE));
//
//
//        btn_pack_h_5.setOnClickListener(v -> {
//
//            PersianCalendar persianCalendar1 = new PersianCalendar();
//            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
//                    (DatePickerDialog.OnDateSetListener) this,
//                    persianCalendar1.getPersianYear(),
//                    persianCalendar1.getPersianMonth(),
//                    persianCalendar1.getPersianDay()
//            );
//            datePickerDialog.show(datePickerDialog.getFragmentManager(), "Datepickerdialog");
//        });
//
//        dialog.show();
//
//    }
//


}
