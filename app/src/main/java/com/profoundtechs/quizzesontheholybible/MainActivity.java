package com.profoundtechs.quizzesontheholybible;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DatabaseHelper databaseHelper;
    Content content;
    List<String> tableName;
    Button btnPrev,btnCenter,btnNext;
    TextView tvQuestionType,tvQuestionNo,tvQuestion;
    int qNumber;
    int tableNameIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Displaying the navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Initializing the views on the main content
        btnPrev = (Button)findViewById(R.id.btnPrev);
        btnCenter = (Button)findViewById(R.id.btnCenter);
        btnNext = (Button)findViewById(R.id.btnNext);
        tvQuestionType = (TextView)findViewById(R.id.tvQuestionType);
        tvQuestionNo = (TextView)findViewById(R.id.tvQuestionNo);
        tvQuestion = (TextView)findViewById(R.id.tvQuestion);

        //Code for copying the database to the device
        databaseHelper=new DatabaseHelper(this);
        File database=getApplicationContext().getDatabasePath(DatabaseHelper.DB_NAME);
        if (!database.exists()){
            databaseHelper.getReadableDatabase();
            if (copyDatabase(this)){
            }else {
                Toast.makeText(this, "Error occurred during copying database", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //Prepare a list of all question categories using a method (to make code readable)
        prepareQTypeList();

        //Code for displaying content when the application launches for the first time
        SharedPreferences sharedPreferences = getSharedPreferences("readerPosition", Context.MODE_PRIVATE);
        qNumber=sharedPreferences.getInt("qNumber",1);
        tableNameIndex=sharedPreferences.getInt("tableNameIndex",0);
        showQuestion(tableNameIndex,qNumber);

        //Make btnPrev blank if current question is the first question of the first table
        if (tableNameIndex==0&&qNumber==1){
            btnPrev.setText("");
        }

        //Make btnNext blank if current question is the last question of the last table
        if (tableNameIndex+1==tableName.size()&&qNumber==databaseHelper.getNumberOfRows(tableName.get(tableNameIndex))){
            btnNext.setText("");
        }

        //Event when the previous arrow is clicked
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tableNameIndex>0){
                    if (qNumber-2>=0){
                        qNumber--;
                        btnCenter.setText("Answer");
                        btnNext.setText("Next");
                        showQuestion(tableNameIndex,qNumber);
                    } else if (qNumber-2<0){
                        tableNameIndex--;
                        qNumber=databaseHelper.getNumberOfRows(tableName.get(tableNameIndex));
                        btnCenter.setText("Answer");
                        btnNext.setText("Next");
                        showQuestion(tableNameIndex,qNumber);
                    }
                } else if (tableNameIndex==0){
                    if (qNumber-2>0){
                        qNumber--;
                        btnCenter.setText("Answer");
                        btnNext.setText("Next");
                        showQuestion(tableNameIndex,qNumber);
                    } else if (qNumber-2==0){
                        qNumber--;
                        btnCenter.setText("Answer");
                        btnNext.setText("Next");
                        showQuestion(tableNameIndex,qNumber);
                        btnPrev.setText("");
                    }
                }
            }
        });

        //Event when the center arrow is clicked
        btnCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnCenter.getText().equals("Answer")){
                    tvQuestionNo.setText("A"+content.getId()+".");
                    tvQuestion.setText(content.getAnswer());
                    btnCenter.setText("Question");
                } else if (btnCenter.getText().equals("Question")){
                    tvQuestionNo.setText("Q"+content.getId()+".");
                    tvQuestion.setText(content.getQuestion());
                    btnCenter.setText("Answer");
                }
            }
        });

        //Event when the next arrow is clicked
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**This code will display the questions one by one when the next button is pressed.
                 * It will pass to the next table in the database if the questions in a current database
                 * are over. It will show a button with empty text when the last question of the last
                 * table in the database is reached.
                 */
                if (tableNameIndex+1<tableName.size()){
                    if (qNumber+1<=databaseHelper.getNumberOfRows(tableName.get(tableNameIndex))){
                        qNumber++;
                        btnCenter.setText("Answer");
                        btnPrev.setText("Previous");
                        showQuestion(tableNameIndex,qNumber);
                    } else if (qNumber+1>databaseHelper.getNumberOfRows(tableName.get(tableNameIndex))){
                        qNumber=1;
                        tableNameIndex++;
                        btnCenter.setText("Answer");
                        btnPrev.setText("Previous");
                        showQuestion(tableNameIndex,qNumber);
                    }
                } else if (tableNameIndex+1==tableName.size()){
                    if (qNumber+1<databaseHelper.getNumberOfRows(tableName.get(tableNameIndex))){
                        qNumber++;
                        btnCenter.setText("Answer");
                        btnPrev.setText("Previous");
                        showQuestion(tableNameIndex,qNumber);
                    } else if (qNumber+1==databaseHelper.getNumberOfRows(tableName.get(tableNameIndex))){
                        qNumber++;
                        btnCenter.setText("Answer");
                        btnPrev.setText("Previous");
                        showQuestion(tableNameIndex,qNumber);
                        btnNext.setText("");
                    }
                }
            }
        });
    }

    //Method for copying database (Used above)
    public boolean copyDatabase(Context context){
        try{
            InputStream inputStream=context.getAssets().open(DatabaseHelper.DB_NAME);
            String outFileName=DatabaseHelper.DB_LOCATION+DatabaseHelper.DB_NAME;
            OutputStream outputStream=new FileOutputStream(outFileName);
            byte[] buff=new byte[1024];
            int length=0;
            while ((length=inputStream.read(buff))>0){
                outputStream.write(buff,0,length);
            }
            outputStream.flush();
            outputStream.close();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (isFirstRun) {

            //show start activity
            startActivity(new Intent(MainActivity.this, StartActivity.class));
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Code for saving the reader position to display it when a user opens the app next time
        SharedPreferences sharedPreferences = getSharedPreferences("readerPosition",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("qNumber",qNumber);
        editor.putInt("tableNameIndex",tableNameIndex);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getItemId()==R.id.start){
            startActivity(new Intent(MainActivity.this,StartActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id==R.id.preface){
            startActivity(new Intent(MainActivity.this,PrefaceActivity.class));
        }else if (id == R.id.table_01) {
            tableNameIndex = 0;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("");
        } else if (id == R.id.table_02) {
            tableNameIndex = 1;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_03) {
            tableNameIndex = 2;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_04) {
            tableNameIndex = 3;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_05) {
            tableNameIndex = 4;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_06) {
            tableNameIndex = 5;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_07) {
            tableNameIndex = 6;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_08) {
            tableNameIndex = 7;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_09) {
            tableNameIndex = 8;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_10) {
            tableNameIndex = 9;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_11) {
            tableNameIndex = 10;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_12) {
            tableNameIndex = 11;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_13) {
            tableNameIndex = 12;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_14) {
            tableNameIndex = 13;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_15) {
            tableNameIndex = 14;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_16) {
            tableNameIndex = 15;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_17) {
            tableNameIndex = 16;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_18) {
            tableNameIndex = 17;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_19) {
            tableNameIndex = 18;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_20) {
            tableNameIndex = 19;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_21) {
            tableNameIndex = 20;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_22) {
            tableNameIndex = 21;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        } else if (id == R.id.table_23) {
            tableNameIndex = 22;
            qNumber = 1;
            showQuestion(tableNameIndex,qNumber);
            btnPrev.setText("Previous");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void prepareQTypeList() {
        tableName = new ArrayList<>();
        tableName.add("First");
        tableName.add("Who?");
        tableName.add("The Apostles");
        tableName.add("St. Pauls Assistants and Disciples");
        tableName.add("The Holy Spirit");
        tableName.add("Fasting");
        tableName.add("Ways in Which God Spoke");
        tableName.add("Gods Promise");
        tableName.add("After the Resurrection");
        tableName.add("Women");
        tableName.add("Mothers");
        tableName.add("The Holy Virgin");
        tableName.add("Children");
        tableName.add("Kings and Prophets");
        tableName.add("Church Sacraments");
        tableName.add("Worship in the Old Testament");
        tableName.add("From the Parables of the Lord");
        tableName.add("Work");
        tableName.add("Giving");
        tableName.add("Personalities");
        tableName.add("About Lord Jesus");
        tableName.add("Miscellaneous");
        tableName.add("Specialised Chapters");
    }

    private void showQuestion(int tableIndex, int qNo){
        content = databaseHelper.getContent(tableName.get(tableIndex),qNo);
        tvQuestionType.setText(tableName.get(tableIndex));
        tvQuestionNo.setText("Q"+content.getId()+".");
        tvQuestion.setText(content.getQuestion());
    }
}
