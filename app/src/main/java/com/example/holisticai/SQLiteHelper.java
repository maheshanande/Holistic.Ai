package com.example.holisticai;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    static String DATABASE_NAME="UserDataBase";
    public static final String TABLE_NAME="UserTable";
    public static final String Table_Column_ID="id";
    public static final String Table_Column_1_FName="fname";
    public static final String Table_Column_2_LastName="lname";
    public static final String Table_Column_3_Password="password";
    public static final String Table_Column_4_Email="email";
    public static final String Table_Column_5_MobNo="mobileno";
    public static final String Table_Column_6_Age="age";
    public static final String Table_Column_7_Gender="gender";
    public static final String Table_Column_8_Goal="goal";
    public static final String Table_Column_9_DiseaseTarget="diseasetarget";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase database) {
        String CREATE_TABLE="CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("+Table_Column_ID+" INTEGER PRIMARY KEY , "+Table_Column_1_FName+" VARCHAR, "+Table_Column_2_LastName+" VARCHAR, "+Table_Column_3_Password+" VARCHAR,"+Table_Column_4_Email+" VARCHAR,"+Table_Column_5_MobNo+", VARCHAR,"+Table_Column_6_Age+",VARCHAR,"+Table_Column_7_Gender+",VARCHAR,"+Table_Column_8_Goal+" VARCHAR,"+Table_Column_9_DiseaseTarget+", VARCHAR)";
        database.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }
}
