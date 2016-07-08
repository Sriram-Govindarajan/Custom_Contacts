package com.example.sriram.customcontacts;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sriram.customcontacts.model.Contact;

import java.io.ByteArrayOutputStream;

public class ViewActivity extends AppCompatActivity {
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etPhone;
    private EditText etEmail;
    private Button btnSave;
    private Button btnDelete;
    private Contact contact;
    private byte[] img = null;
    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        // Find the GUI components
        findViews();

        checkIntentForContact();

        this.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contact == null) {
                    insertContact();
                } else {
                    updateContact();
                }
            }
        });

        this.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContact();
            }
        });
    }

    private void updateContact() {
        SQLiteDatabase database = openOrCreateDatabase("contacts.db", Context.MODE_PRIVATE, null);
        ContentValues values = new ContentValues();
        values.put("first_name", etFirstName.getText().toString());
        values.put("last_name", etLastName.getText().toString());
        values.put("phone", etPhone.getText().toString());
        values.put("email", etEmail.getText().toString());
        values.put("image", img);
        database.update("Contact", values, "phone = ?", new String[]{contact.getPhone()});
        database.close();
        this.finish();
    }

    private void insertContact() {
        SQLiteDatabase database = openOrCreateDatabase("contacts.db", Context.MODE_PRIVATE, null);
        ContentValues values = new ContentValues();
        values.put("first_name", etFirstName.getText().toString());
        values.put("last_name", etLastName.getText().toString());
        values.put("phone", etPhone.getText().toString());
        values.put("email", etEmail.getText().toString());
        values.put("image", img);
        database.insert("Contact", null, values);
        database.close();
        this.finish();
    }

    private void deleteContact() {
        SQLiteDatabase database = openOrCreateDatabase("contacts.db", Context.MODE_PRIVATE, null);
        database.delete("Contact", "phone = ?", new String[]{contact.getPhone()});
        database.close();
        this.finish();
    }

    private void findViews() {
        this.etFirstName = (EditText) findViewById(R.id.etFirstName);
        this.etLastName = (EditText) findViewById(R.id.etLastName);
        this.etPhone = (EditText) findViewById(R.id.etPhone);
        this.etEmail = (EditText) findViewById(R.id.etEmail);
        this.btnSave = (Button) findViewById(R.id.btnSave);
        this.btnDelete = (Button) findViewById(R.id.btnDelete);
    }

    private void checkIntentForContact() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        btnDelete.setEnabled(false);
        if (bundle != null) {
            contact = (Contact) bundle.get("CONTACT");
            if (contact != null) {
                this.etFirstName.setText(contact.getFirstName());
                this.etLastName.setText(contact.getLastName());
                this.etPhone.setText(contact.getPhone());
                this.etEmail.setText(contact.getEmail());
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                SQLiteDatabase database = openOrCreateDatabase("contacts.db", Context.MODE_PRIVATE, null);
                Cursor cursor = database.query("Contact", null, "phone = "+etPhone.getText().toString(),null,null,null,null);
                if (cursor.moveToFirst()) {
                    img = cursor.getBlob(4);
                    cursor.close();
                }
                if(img!=null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                    imageView.setImageBitmap(bitmap);
                }
                else{
                    imageView.setImageResource(R.mipmap.ic_launcher);
                }
                btnDelete.setEnabled(true);
            }
        }
    }

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                ImageView imgView = (ImageView) findViewById(R.id.imageView);
                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));
                BitmapDrawable drawable = (BitmapDrawable) imgView.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                img = outputStream.toByteArray();

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }
}