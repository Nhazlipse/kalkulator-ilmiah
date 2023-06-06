package net.practicalcoding.scientificcalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.Gravity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.mariuszgromada.math.mxparser.*;

public class MainActivity extends AppCompatActivity {
    private DBhelper dbHelper;
    private SQLiteDatabase database;
    private TextView previousCalculation;
    private EditText display;
    private boolean isButtonEnabled = true;
    private Handler handler;
    private int cooldownTime = 1800; // Waktu cooldown dalam milidetik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize DBHelper and SQLiteDatabase
        dbHelper = new DBhelper(this);
        database = dbHelper.getWritableDatabase();

        previousCalculation = findViewById(R.id.previousCalculationView);
        display = findViewById(R.id.displayEditText);

        display.setShowSoftInputOnFocus(false); // agar tidak menampikam keyboard HP saat bar operasi di tekan

        handler = new Handler();
    }

    private void updateText(String strToAdd) {
        String oldStr = display.getText().toString();
        int cursorPos = display.getSelectionStart();
        String leftStr = oldStr.substring(0, cursorPos);
        String rightStr = oldStr.substring(cursorPos);

        display.setText(String.format("%s%s%s", leftStr, strToAdd, rightStr));
        display.setSelection(cursorPos + strToAdd.length());
    }

    public void doubleZeroBTNPush(View view){
            updateText(getResources().getString(R.string.doubleZeroText));
    }

    public void zeroBTNPush(View view){
        updateText(getResources().getString(R.string.zeroText));
    }

    public void oneBTNPush(View view){
        updateText(getResources().getString(R.string.oneText));
    }

    public void twoBTNPush(View view){
        updateText(getResources().getString(R.string.twoText));
    }

    public void threeBTNPush(View view){
        updateText(getResources().getString(R.string.threeText));
    }

    public void fourBTNPush(View view){
        updateText(getResources().getString(R.string.fourText));
    }

    public void fiveBTNPush(View view){
        updateText(getResources().getString(R.string.fiveText));
    }

    public void sixBTNPush(View view){
        updateText(getResources().getString(R.string.sixText));
    }

    public void sevenBTNPush(View view){
        updateText(getResources().getString(R.string.sevenText));
    }

    public void eightBTNPush(View view){
        updateText(getResources().getString(R.string.eightText));
    }

    public void nineBTNPush(View view){
        updateText(getResources().getString(R.string.nineText));
    }

    public void multiplyBTNPush(View view){
        if (isButtonEnabled) { //eksekusi cooldown
            updateText(getResources().getString(R.string.multiplyText));
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void divideBTNPush(View view) {
        if (isButtonEnabled) {
            updateText(getResources().getString(R.string.divideText));
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void subtractBTNPush(View view) {
        if (isButtonEnabled) {
            updateText(getResources().getString(R.string.subtractText));
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void addBTNPush(View view){
        if (isButtonEnabled) {
            updateText(getResources().getString(R.string.addText));
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void clearBTNPush(View view){
        display.setText("");
        previousCalculation.setText("");
    }

    public void parOpenBTNPush(View view) {
        if (isButtonEnabled) {
            updateText(getResources().getString(R.string.parenthesesOpenText));
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void parCloseBTNPush(View view) {
        if (isButtonEnabled) {
            updateText(getResources().getString(R.string.parenthesesCloseText));
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void decimalBTNPush(View view) {
        if (isButtonEnabled) {
            updateText(getResources().getString(R.string.decimalText));
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void equalBTNPush(View view){
        String userExp = display.getText().toString();

        previousCalculation.setText(userExp);

        userExp = userExp.replaceAll(getResources().getString(R.string.divideText), "/");
        userExp = userExp.replaceAll(getResources().getString(R.string.multiplyText), "*");

        Expression exp = new Expression(userExp);
        double result = exp.calculate();

        if(Double.isNaN(result)) {
            display.setText("Kesalahan");
        } else {
            display.setText(String.valueOf(result));

            // Save the calculation history to the database
            saveCalculationToHistory(userExp, result);
        }
        display.setSelection(display.getText().length());
        }

    private void saveCalculationToHistory(String expression, double result) {
        ContentValues values = new ContentValues();
        values.put(DBhelper.COLUMN_EXPRESSION, expression);
        values.put(DBhelper.COLUMN_RESULT, result);

        database.insert(DBhelper.TABLE_NAME, null, values);
    }

    public void showHistory(View view) {
        // Query the database to retrieve calculation history
        Cursor cursor = database.query(DBhelper.TABLE_NAME, null, null, null, null, null, null);

        StringBuilder historyBuilder = new StringBuilder();

        if (cursor.moveToFirst()) {
            do {
                String expression = cursor.getString(cursor.getColumnIndex(DBhelper.COLUMN_EXPRESSION));
                double result = cursor.getDouble(cursor.getColumnIndex(DBhelper.COLUMN_RESULT));

                String historyItem = String.format("%s = %s\n", expression, result);
                historyBuilder.append(historyItem);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Display the calculation history
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("History Perhitungan");
        builder.setMessage(historyBuilder.toString());
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Clear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Clear the calculation history
                database.delete(DBhelper.TABLE_NAME, null, null);
            }
        });

        // Reorder the buttons
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                layoutParams.gravity = Gravity.END;
                positiveButton.setLayoutParams(layoutParams);
                negativeButton.setLayoutParams(layoutParams);
            }
        });
        dialog.show();
    }

    public void SinBTN(View view) {
        if (isButtonEnabled) {
            updateText("sin(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void CosBTN(View view){
        if (isButtonEnabled) {
            updateText("cos(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void TanBTN(View view){
        if (isButtonEnabled) {
            updateText("tan(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void ArcSinBTN(View view){
        if (isButtonEnabled) {
            updateText("arcsin(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void ArcCosBTN(View view){
        if (isButtonEnabled) {
            updateText("arccos(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void ArcTanBTN(View view){
        if (isButtonEnabled) {
            updateText("arctan(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void naturalLogBTN(View view) {
        if (isButtonEnabled) {
            updateText("ln(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void logBTN(View view){
        if (isButtonEnabled) {
            updateText("log10(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void sqrtBTN(View view){
        if (isButtonEnabled) {
            updateText("sqrt(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void absBTN(View view){
        if (isButtonEnabled) {
            updateText("abs(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void piBTN(View view){
        if (isButtonEnabled) {
            updateText("pi");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void eBTN(View view){
        if (isButtonEnabled) {
            updateText("e");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void xSquaredBTN(View view){
        if (isButtonEnabled) {
            updateText("^(2)");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void xPowerYBTN(View view){
        if (isButtonEnabled) {
            updateText("^(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }

    public void primeBTN(View view){
        if (isButtonEnabled) {
            updateText("ispr(");
            isButtonEnabled = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonEnabled = true;
                }
            }, cooldownTime);
        }
    }
}