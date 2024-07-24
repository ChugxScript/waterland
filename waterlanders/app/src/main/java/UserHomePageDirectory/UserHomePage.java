package UserHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.example.waterlanders.R;

import LoginDirectory.Login;

public class UserHomePage extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_home_page);

        Button logout_button = findViewById(R.id.button);

        logout_button.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(UserHomePage.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserHomePage.this, Login.class);
            startActivity(intent);
            finish();
        });

    }    // init obj
}
