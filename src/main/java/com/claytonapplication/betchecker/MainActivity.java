package com.claytonapplication.betchecker;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import javax.net.ssl.HandshakeCompletedListener;


public class MainActivity extends Activity {

    private EditText VBackOdds;
    private EditText VLayOdds;
    private EditText VBackStake;

    private TextView VLay;
    private TextView VLiability;
    private TextView VTotalStake;

    private Spinner VBackCommission;
    private Spinner VLayCommission;


    private TextView VBackReturns;
    private TextView VLayReturns;
    private TextView VBackProfit;
    private TextView VLayProfit;
    private TextView VGarunteedProfit;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VBackOdds = (EditText) findViewById(R.id.BackOdds);
        VLayOdds = (EditText) findViewById(R.id.LayOdds);
        VBackStake = (EditText) findViewById(R.id.BackStake);



        TextWatcher calc = new TextWatcher() {
            public void afterTextChanged(Editable s) {
               calculate();
            }
            public void beforeTextChanged(CharSequence s, int start,int count, int after) {}
            public void onTextChanged(CharSequence s, int start,int before, int count) {}
        };

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                calculate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                calculate();}
        };

        VBackOdds.addTextChangedListener(calc);
        VLayOdds.addTextChangedListener(calc);
        VBackStake.addTextChangedListener(calc);


        VLay = (TextView) findViewById(R.id.Lay);
        VLiability = (TextView) findViewById(R.id.Liability);
        VTotalStake = (TextView) findViewById(R.id.TotalStake);

        VBackCommission = (Spinner) findViewById(R.id.BackCommission);
        VLayCommission = (Spinner) findViewById(R.id.LayCommission);



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, commissionList(BigDecimal.ZERO, BigDecimal.TEN, new BigDecimal("0.1")));

        VBackCommission.setAdapter(adapter);
        VLayCommission.setAdapter(adapter);


        VBackCommission.setOnItemSelectedListener(spinnerListener);
        VLayCommission.setOnItemSelectedListener(spinnerListener);

        VBackReturns = (TextView) findViewById(R.id.BackReturns);
        VLayReturns = (TextView) findViewById(R.id.LayReturns);
        VBackProfit = (TextView) findViewById(R.id.BackProfit);
        VLayProfit = (TextView) findViewById(R.id.LayProfit);
        VGarunteedProfit = (TextView) findViewById(R.id.GarunteedProfit);

        calculate();
    }

    public ArrayList<String> commissionList(BigDecimal min, BigDecimal max, BigDecimal step){
        ArrayList<String> list = new ArrayList<String>();

        for (BigDecimal i = min; (i.compareTo(max) != 1); i = i.add(step)){
            list.add(i.toPlainString() + "%");
        }

        return list;
    }

    public String clean(String x){
        if (x.equals("")){
            return "0";
        }
        if (x.charAt(0) == '.'){
            x = "0" + x;
        }
        if (x.charAt(x.length()-1) == '.'){
            x = x + "0";
        }
        return x;

    }

    public void error(){
        String x = "";
        VLay.setText(x);
        VLiability.setText(x);
        VTotalStake.setText(x);
        VBackReturns.setText(x);
        VLayReturns.setText(x);
        formatSet(VBackProfit, BigDecimal.ZERO);
        formatSet(VLayProfit, BigDecimal.ZERO);
        formatSet(VGarunteedProfit, BigDecimal.ZERO);
    }


    public void calculate(){
        String BackOddsStr = clean(VBackOdds.getText().toString());
        String BackStakeStr = clean(VBackStake.getText().toString());
        String LayOddsStr = clean(VLayOdds.getText().toString());


        System.out.println(BackOddsStr + " " + BackStakeStr + " " + LayOddsStr);

        BigDecimal BackOdds = new BigDecimal(BackOddsStr);
        BigDecimal BackStake = new BigDecimal(BackStakeStr);
        BigDecimal LayOdds = new BigDecimal(LayOddsStr);

        String BackCommissionStr = VBackCommission.getSelectedItem().toString();
        String LayCommissionStr = VLayCommission.getSelectedItem().toString();
        BigDecimal BackCommission = new BigDecimal(BackCommissionStr.substring(0, BackCommissionStr.length() - 1)).divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP);
        BigDecimal LayCommission = new BigDecimal(LayCommissionStr.substring(0, LayCommissionStr.length() - 1)).divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP);

        System.out.println("Back Com: " + BackCommission.toPlainString());
        System.out.println("Lay Com: " + LayCommission.toPlainString());

        if ((LayOdds.compareTo(BigDecimal.ZERO) == 0) || (BackOdds.compareTo(BigDecimal.ZERO) == 0)) {
            error();
            return;
        }

        BigDecimal ONE = BigDecimal.ONE;

        BigDecimal top = BackStake.multiply(  BackOdds.subtract(ONE) .multiply (ONE.subtract(BackCommission)) .add(ONE)  );
        BigDecimal bottom = ONE.subtract(LayCommission) .divide (LayOdds.subtract(ONE), 20, RoundingMode.HALF_UP ) .add(ONE);

        BigDecimal Liability = top.divide(bottom, 2, RoundingMode.HALF_UP);
        BigDecimal Lay = Liability.divide( LayOdds.subtract(ONE), 2, RoundingMode.HALF_UP );

        BigDecimal TotalStake = BackStake.add(Liability);

        BigDecimal BackReturns = BackOdds.multiply(BackStake).setScale(2, RoundingMode.HALF_UP);
        BigDecimal LayReturns = Lay.add(Liability);

        BigDecimal PreBackProfit = BackReturns.subtract(BackStake);
        BigDecimal PreLayProfit = Lay;

        BigDecimal BackCommissionTotal = PreBackProfit.multiply(BackCommission);
        BigDecimal LayCommissionTotal = PreLayProfit.multiply(LayCommission);

        BigDecimal BackProfit = PreBackProfit.subtract(BackCommissionTotal).subtract(Liability).setScale(2, RoundingMode.HALF_UP);
        BigDecimal LayProfit = PreLayProfit.subtract(LayCommissionTotal).subtract(BackStake).setScale(2, RoundingMode.HALF_UP);


        BigDecimal GarunteedProfit = smallest(BackProfit, LayProfit);




        VLay.setText(Lay.toPlainString());
        VLiability.setText(Liability.toPlainString());
        VTotalStake.setText(TotalStake.toPlainString());
        VBackReturns.setText(BackReturns.toPlainString());
        VLayReturns.setText(LayReturns.toPlainString());

        formatSet(VBackProfit, BackProfit);
        formatSet(VLayProfit, LayProfit);
        formatSet(VGarunteedProfit, GarunteedProfit);


    }

    public void formatSet(TextView v, BigDecimal value){
        switch (value.compareTo(BigDecimal.ZERO)){
            case 1:
                v.setTextColor(getResources().getColor(R.color.profit_colour));
                break;
            case 0:
                v.setTextColor(getResources().getColor(R.color.main_text_colour));
                break;
            case -1:
                v.setTextColor(getResources().getColor(R.color.loss_colour));
                break;
        }

        v.setText(value.toPlainString());
    }


    public BigDecimal smallest(BigDecimal A, BigDecimal B){
        if (A.compareTo(B) == 1){
            return B;
        }
        else{
            return A;
        }
    }

}
