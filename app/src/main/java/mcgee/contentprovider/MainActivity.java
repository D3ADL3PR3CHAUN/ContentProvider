package mcgee.contentprovider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void addBirthday(View view) {
        //add new birthday record
        ContentValues values = new ContentValues();
        values.put(BirthdayProvider.NAME, ((EditText)findViewById(R.id.nameET)).getText().toString());
        values.put(BirthdayProvider.BIRTHDAY, ((EditText)findViewById(R.id.birthdayET)).getText().toString());
        Uri uri = getContentResolver().insert(BirthdayProvider.CONTEXT_URI, values);

        Toast.makeText(getBaseContext(), "McGee: " + uri.toString() + " inserted!", Toast.LENGTH_LONG).show();
    }

    public void showAllBirthdays(View view) {
        //show all birthdays sorted by friends name
        String URL = "content://mcgee.contentprovider.BirthdayProvider/friends";
        Uri friends = Uri.parse(URL);
        Cursor c = getContentResolver().query(friends, null, null, null, "name");
        String result = "McGee Results:";

        if(!c.moveToFirst()) {
            Toast.makeText(this, result + " no content yet!", Toast.LENGTH_LONG).show();
        } else {
            do {
                result = result + "\n" + c.getString(c.getColumnIndex(BirthdayProvider.NAME)) + " with id " + c.getString(c.getColumnIndex(BirthdayProvider.ID)) + " has birthday: " + c.getString(c.getColumnIndex(BirthdayProvider.BIRTHDAY));
            } while(c.moveToNext());

            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }
    }

    public void deleteAllBirthdays(View view) {
        String URL = "content://mcgee.contentprovider.BirthdayProvider/friends";
        Uri friends = Uri.parse(URL);
        int count = getContentResolver().delete(friends, null, null);
        String countNum = "McGee: " + count + " records are deleted.";
        Toast.makeText(getBaseContext(), countNum, Toast.LENGTH_LONG).show();
    }
}
