package com.example.selfdefence;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddRelative extends AppCompatActivity {

    EditText name,number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_relative);
    }

    public void display(View v) {
        Intent i_view=new Intent(AddRelative.this, DisplayContacts.class);
        startActivity(i_view);

    }

    public void instructions(View v) {
        Intent i_help=new Intent(AddRelative.this,HowToUse.class);
        startActivity(i_help);
    }


    public void storeInDB(View v) {

        name = (EditText) this.findViewById(R.id.editText1);
        number = (EditText) this.findViewById(R.id.editText2);

        String str_name=name.getText().toString();
        String str_number=number.getText().toString();

        SQLiteDatabase db;

        db=openOrCreateDatabase("NumDB", Context.MODE_PRIVATE, null);



        db.execSQL("CREATE TABLE IF NOT EXISTS details(name VARCHAR,number VARCHAR);");
        //Toast.makeText(getApplicationContext(), "table created",Toast.LENGTH_LONG).show();

        Cursor c=db.rawQuery("SELECT * FROM details", null);

        if (!TextUtils.isEmpty(str_name) && !TextUtils.isEmpty(str_number) ) {
            if (c.getCount() < 2) {
                db.execSQL("INSERT INTO details VALUES('" + str_name + "','" + str_number + "');");
                Toast.makeText(getApplicationContext(), "Successfully Saved", Toast.LENGTH_SHORT).show();
            } else {
                db.execSQL("INSERT INTO details VALUES('" + str_name + "','" + str_number + "');");


                Toast.makeText(getApplicationContext(), "Maximun Numbers limited reached.", Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(this, "Please enter name or number of your Guardiant", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }


    public void btnDelete(View v) {
       // Toast.makeText(getApplicationContext(), "delete started",Toast.LENGTH_LONG).show();

        name = (EditText) this.findViewById(R.id.editText1);
        number = (EditText) this.findViewById(R.id.editText2);

        String str_name=name.getText().toString();
        String str_number=number.getText().toString();
        //Toast.makeText(getApplicationContext(), "db created",Toast.LENGTH_LONG).show();

        SQLiteDatabase db;

        db=openOrCreateDatabase("NumDB", Context.MODE_PRIVATE, null);


        if (!TextUtils.isEmpty(str_name) ) {

            Cursor c = db.rawQuery("SELECT * FROM details", null);

            db.execSQL("DELETE FROM details WHERE name=\"" + str_name + "\";");
            // db.execSQL("DELETE FROM details");

            Toast.makeText(getApplicationContext(), "Your Contact is Deleted", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Please enter Name to delete from Contacts", Toast.LENGTH_SHORT).show();
        }

    }

}