package com.example.sriram.customcontacts;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sriram.customcontacts.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends ActionBarActivity {

    private ListView listView;
    private List<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        this.listView = (ListView) findViewById(R.id.listView2);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateContact(position);
            }
        });
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            contacts = new ArrayList<>();
            SQLiteDatabase database = openOrCreateDatabase("contacts.db", Context.MODE_PRIVATE, null);
            Cursor cursor = database.query("Contact",null,"first_name like '%"+query+"%' or last_name like '%"+query+"%' or phone like '"+query+"%'",null,null,null,null);
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                Contact contact = new Contact();
                contact.setFirstName(cursor.getString(0));
                contact.setLastName(cursor.getString(1));
                contact.setPhone(cursor.getString(2));
                contact.setEmail(cursor.getString(3));
                contacts.add(contact);
                cursor.moveToNext();
            }
            cursor.close();
            database.close();
            ContactAdapter adapter = new ContactAdapter(this, contacts);
            this.listView.setAdapter(adapter);
        }
    }

    private void updateContact(int index) {
        Contact contact = contacts.get(index);
        Intent intent = new Intent(this, ViewActivity.class);
        intent.putExtra("CONTACT", contact);
        startActivity(intent);
    }

    private class ContactAdapter extends ArrayAdapter<Contact> {
        public ContactAdapter(Context context, List<Contact> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_layout, parent, false);
            }
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView2);
            TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
            TextView txtLName = (TextView) convertView.findViewById(R.id.txtLName);
            TextView txtPhone = (TextView) convertView.findViewById(R.id.txtPhone);
            Contact contact = contacts.get(position);
            String etPhone = contact.getPhone();
            SQLiteDatabase database = openOrCreateDatabase("contacts.db", Context.MODE_PRIVATE, null);
            Cursor cursor = database.query("Contact", null, "phone = "+etPhone,null,null,null,null);
            byte[] img = null;
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
            txtName.setText(contact.getFirstName());
            txtLName.setText(contact.getLastName());
            txtPhone.setText(contact.getPhone());
            return convertView;
        }
    }
}