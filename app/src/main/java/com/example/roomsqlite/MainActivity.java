package com.example.roomsqlite;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roomsqlite.database.UserDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 10;
    private EditText editUsername;
    private EditText editAddress;
    private EditText editEmail;
    private EditText editContent;
    private Button btnAddUser;
    private RecyclerView rcvUser;
    private TextView tvDeleteAll;
    private EditText edtSearch;

    private UserAdapter userAdapter;
    private List<User> mListUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUi();
        userAdapter = new UserAdapter(new UserAdapter.IClickItemUser() {
            @Override
            public void updateUser(User user) {
                clickUpdateUser(user);
            }

            @Override
            public void deleteUser(User user) {
                clickDeleteUser(user);
            }
        });
        mListUser= new ArrayList<>();
        userAdapter.setData(mListUser);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvUser.setLayoutManager(linearLayoutManager);

        rcvUser.setAdapter(userAdapter);

        btnAddUser.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                addUser();
            }
        });
        tvDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickDeleteAllUser();
            }
        });

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEARCH){
                    handleSearchUser();

                }
                return false;
            }
        });

        loadData();
    }

    private void initUi(){
        editUsername=findViewById(R.id.edit_username);
        editAddress=findViewById(R.id.edit_address);
        editEmail=findViewById(R.id.edit_email);
        editContent=findViewById(R.id.edit_content);
        btnAddUser=findViewById(R.id.btn_add_user);
        rcvUser=findViewById(R.id.rcv_user);
        tvDeleteAll=findViewById(R.id.tv_delete_all);
        edtSearch=findViewById(R.id.edt_search);

    }

    private void addUser() {
        String strUsername = editUsername.getText().toString().trim();
        String strAddress = editAddress.getText().toString().trim();

        if(TextUtils.isEmpty(strUsername) || TextUtils.isEmpty(strAddress)){
            return;
        }
        User user = new User(strUsername, strAddress);

        if(isUserExist(user)){
            Toast.makeText(this, "User Exist", Toast.LENGTH_SHORT).show();
            return;
        }

        UserDatabase.getInstance(this).userDAO().insertUser(user);
        //thong bao add success
        Toast.makeText(this, "Add user successfully", Toast.LENGTH_SHORT).show();

        editUsername.setText("");
        editAddress.setText("");
        editContent.setText("");
        editEmail.setText("");
        hideSoftKeyboard();

        loadData();

    }
    //function ẩn keyboard
    public void hideSoftKeyboard(){
        try{
            InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        }catch(NullPointerException ex){
            ex.printStackTrace();

        }
    }

    private void loadData(){
        mListUser = UserDatabase.getInstance(this).userDAO().getListUser();
        userAdapter.setData(mListUser);
    }

    private boolean isUserExist(User user){
        List<User> list = UserDatabase.getInstance(this).userDAO().checkUser(user.getUsername());
        return list != null && !list.isEmpty();
    }

    private void clickUpdateUser(User user){//function click button update chuyen qua list update
        Intent intent = new Intent(MainActivity.this, UpdateActivity.class);//duong chuyen tu main sang update
        Bundle bundle = new Bundle();
        bundle.putSerializable("object_user", user);//to use putSerialzable wwe must implements class with Serialzable
        intent.putExtras(bundle);

        startActivityForResult(intent, MY_REQUEST_CODE);//need khai bao bien  MY_REQUEST_CODE owr tren

    }

    @Override//override this function to hứng listener of UpdateActivity
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            loadData();
        }

    }
    public void startActivityForResult(@SuppressLint("UnknownNullness") Intent intent,
                                       int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }
//================================================
//delete
   private void clickDeleteUser(User user){
        new AlertDialog.Builder(this)
                .setTitle("Confirm delete user")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        UserDatabase.getInstance(MainActivity.this).userDAO().deleteUser(user);
                        Toast.makeText(MainActivity.this, "Delete user successfully",
                                Toast.LENGTH_SHORT).show();
                        loadData();

                    }
                }).setNegativeButton("No", null)
                .show();
    }
//===================================delete All
    private void clickDeleteAllUser(){
        new AlertDialog.Builder(this)
                .setTitle("Confirm delete all user")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        UserDatabase.getInstance(MainActivity.this).userDAO().deleteAllUser();
                        Toast.makeText(MainActivity.this, "Delete all user successfully",
                                Toast.LENGTH_SHORT).show();
                        loadData();

                    }
                }).setNegativeButton("No", null)
                .show();
    }
//==================search
    private void handleSearchUser(){
        String strKeyword = edtSearch.getText().toString().trim();
        mListUser = new ArrayList<>();
        mListUser = UserDatabase.getInstance(this).userDAO().searchUser(strKeyword);
        userAdapter.setData(mListUser);
        hideSoftKeyboard();
    }

}