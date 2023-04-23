package com.example.holisticai;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.airbnb.lottie.L;

public class Register_form extends AppCompatActivity {
    EditText Email, Password, FirstName ,lastName,confirm_password,Age,Phone_no;
    AutoCompleteTextView Gender;
    Button registerbtn;
    String FirstNameHolder,LastNameHolder, EmailHolder, PasswordHolder,AgeHolder,PhnoHolder,GoalHolder,GenderHolder,DiseaseHolder;
    Boolean EditTextEmptyHolder;
    SQLiteDatabase sqLiteDatabaseObj;
    String SQLiteDataBaseQueryHolder ;
    SQLiteHelper sqLiteHelper;
    Cursor cursor;
    String F_Result = "Not_Found";
    Spinner goalDropdown,diseaseDropdown;
    EditText t1;

    public static  String selectedTypeResult,selectedTypeResult1;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_form);
        goalDropdown = (Spinner) findViewById((R.id.goalDropDownSpinner));
        diseaseDropdown = findViewById(R.id.DiseaseDropDownSpinner);
        FirstName = findViewById(R.id.fNameEditText);
        lastName = findViewById(R.id.lNameEditText);
        Email = findViewById(R.id.EmailEditText);
        Phone_no = findViewById(R.id.MobileNoEditText);
        Age = findViewById(R.id.AgeEditText);
        confirm_password = findViewById(R.id.confirmPassEditText);
        Password = findViewById(R.id.passEditText);
        registerbtn = findViewById(R.id.Register_btn);
        sqLiteHelper = new SQLiteHelper(this);

        Gender = findViewById(R.id.GenderEdittext);
        String[] suggestions = {"Male","Female","Others"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        Gender.setAdapter(adapter);








        goalDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GoalHolder = parent.getItemAtPosition(position).toString();
                if (GoalHolder.equals("Target Disease")) {
                    diseaseDropdown.setVisibility(View.VISIBLE);
                } else {
                    diseaseDropdown.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                display();
                SQLiteDataBaseBuild();
                // Creating SQLite table if dose n't exists.
                SQLiteTableBuild();
                // Checking EditText is empty or Not.
                CheckEditTextStatus();
                // Method to check Email is already exists or not.
                CheckingEmailAlreadyExistsOrNot();
                // Empty EditText After done inserting process.
                EmptyEditTextAfterDataInsert();

            }
        });

    }
    public void display(){
        GoalHolder = goalDropdown.getSelectedItem().toString();
        DiseaseHolder = diseaseDropdown.getSelectedItem().toString();
        Toast.makeText(this, "Mahesh"+GoalHolder, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "ANande"+DiseaseHolder, Toast.LENGTH_SHORT).show();



    }

    private void EmptyEditTextAfterDataInsert() {
        FirstName.getText().clear();
        Email.getText().clear();
        Password.getText().clear();
        Phone_no.getText().clear();
        Age.getText().clear();
        confirm_password.getText().clear();
        lastName.getText().clear();
        Gender.getText().clear();


    }

    private void CheckingEmailAlreadyExistsOrNot() {
        sqLiteDatabaseObj = sqLiteHelper.getWritableDatabase();
        // Adding search email query to cursor.
        cursor = sqLiteDatabaseObj.query(SQLiteHelper.TABLE_NAME, null, " " + SQLiteHelper.Table_Column_4_Email + "=?", new String[]{EmailHolder}, null, null, null);
        while (cursor.moveToNext()) {
            if (cursor.isFirst()) {
                cursor.moveToFirst();
                // If Email is already exists then Result variable value set as Email Found.
                F_Result = "Email Found";
                // Closing cursor.
                cursor.close();
            }
        }
        // Calling method to check final result and insert data into SQLite database.
        CheckFinalResult();
    }

    private void CheckFinalResult() {
        if(F_Result.equalsIgnoreCase("Email Found"))
        {
            // If email is exists then toast msg will display.
            Toast.makeText(Register_form.this,"Email Already Exists",Toast.LENGTH_LONG).show();
        }
        else {
            // If email already dose n't exists then user registration details will entered to SQLite database.
            InsertDataIntoSQLiteDatabase();
        }
        F_Result = "Not_Found" ;
    
    }

    private void InsertDataIntoSQLiteDatabase() {
        GoalHolder = goalDropdown.getSelectedItem().toString();
        DiseaseHolder = diseaseDropdown.getSelectedItem().toString();
        if(EditTextEmptyHolder == true)
        {
            // SQLite query to insert data into table.
            SQLiteDataBaseQueryHolder = "INSERT INTO "+SQLiteHelper.TABLE_NAME+" (fname,lname,password,email,mobileno,age,gender,goal,diseasetarget) VALUES('"+FirstNameHolder+"','"+LastNameHolder +"', '"+PasswordHolder+"', '"+EmailHolder+"', '"+PhnoHolder+"','"+AgeHolder+"','"+GenderHolder+"','"+GoalHolder+"','"+DiseaseHolder+"');";
            // Executing query.
            sqLiteDatabaseObj.execSQL(SQLiteDataBaseQueryHolder);
            // Closing SQLite database object.
            sqLiteDatabaseObj.close();
            // Printing toast message after done inserting.
            Toast.makeText(Register_form.this,"User Registered Successfully", Toast.LENGTH_LONG).show();
            Intent intent2 = new Intent(Register_form.this , Login.class);
            startActivity(intent2);
        }
        // This block will execute if any of the registration EditText is empty.
        else {
            // Printing toast message if any of EditText is empty.
            Toast.makeText(Register_form.this,"Please Fill All The Required Fields.", Toast.LENGTH_LONG).show();
        }
    }

    private void CheckEditTextStatus() {
        FirstNameHolder = FirstName.getText().toString();
        PasswordHolder = Password.getText().toString();
        EmailHolder = Email.getText().toString();
        AgeHolder = Age.getText().toString();
        PhnoHolder = Phone_no.getText().toString();
        LastNameHolder = lastName.getText().toString();

        if(TextUtils.isEmpty(FirstNameHolder) || TextUtils.isEmpty(EmailHolder) || TextUtils.isEmpty(PasswordHolder)||TextUtils.isEmpty(AgeHolder)||TextUtils.isEmpty(PhnoHolder)||TextUtils.isEmpty(LastNameHolder)){
            EditTextEmptyHolder = false ;
        }
        else {
            EditTextEmptyHolder = true ;
        }
    }

    private void SQLiteTableBuild() {
        sqLiteDatabaseObj.execSQL("CREATE TABLE IF NOT EXISTS " + SQLiteHelper.TABLE_NAME + "(" + SQLiteHelper.Table_Column_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " + SQLiteHelper.Table_Column_1_FName + " VARCHAR, " + SQLiteHelper.Table_Column_2_LastName + " VARCHAR," + SQLiteHelper.Table_Column_3_Password + " VARCHAR," + SQLiteHelper.Table_Column_4_Email + " VARCHAR," + SQLiteHelper.Table_Column_5_MobNo + " VARCHAR," + SQLiteHelper.Table_Column_6_Age + " VARCHAR," + SQLiteHelper.Table_Column_7_Gender + " VARCHAR," + SQLiteHelper.Table_Column_8_Goal + " VARCHAR," + SQLiteHelper.Table_Column_9_DiseaseTarget + ", VARCHAR);");
    }

    private void SQLiteDataBaseBuild() {
        sqLiteDatabaseObj = openOrCreateDatabase(SQLiteHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);

    }


}