package UserHomePageDirectory.OrderTrackingUtils.PendingOrders;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import AdminHomePageDirectory.Orders.Utils.GCashPaymentDetails;
import AdminHomePageDirectory.Orders.Utils.OrderedItemsConstructor;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersCurrentOrderDetailsAdapter;
import LoginDirectory.Login;
import UserHomePageDirectory.HomeFragmentUtils.OrderReceipt;
import UserHomePageDirectory.MainDashboardUser;

public class UserPendingOrdersCurrentOrderDetails extends AppCompatActivity {

    private ImageView backButton;

    private TextView customerName;
    private TextView customerContactNumber;
    private TextView customerID;
    private TextView customerDeliveryAddress;

    private TextView dateOrdered;

    private TextView orderID;

    private RecyclerView recyclerViewHolder;
    private TextView totalOrderAmount;
    private ImageView editOrder;

    private LinearLayout modeOfPaymentContainer;
    private TextView modeOfPayment;

    private TextView orderStatus;

    private TextView additionalMessage;

    private Button cancelOrderButton;
    private Button backButton2;

    private PendingOrdersConstructor pendingOrdersConstructor;
    private List<OrderedItemsConstructor> orderedItemsConstructorList;
    private PendingOrdersCurrentOrderDetailsAdapter pendingOrdersCurrentOrderDetailsAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pending_orders_current_order_details);
        initializeObjects();
        getIntentData();
//        setTextValues();
//        populateOrderList();

        // other buttons
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("refreshNeeded", true);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        // to check the payment details
        if (String.valueOf(modeOfPayment.getText()).equals("GCash")){
            modeOfPaymentContainer.setOnClickListener(view -> {
                Map<String, Object> gcash_payment_details = pendingOrdersConstructor.getGcash_payment_details();
                Intent showGCashPaymentIntent = new Intent(this, GCashPaymentDetails.class);
                showGCashPaymentIntent.putExtra("gcash_payment_details", (Serializable) gcash_payment_details);
                startActivity(showGCashPaymentIntent);
            });
        }

        cancelOrderButton.setOnClickListener(v ->{
            showCancelOrderDialog();
        });

        backButton2.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("refreshNeeded", true);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        editOrder.setOnClickListener(v -> {
            Intent editOrdersIntent = new Intent(this, UserEditPendingOrders.class);
            editOrdersIntent.putExtra("order_id", pendingOrdersConstructor.getOrder_id());
            startActivityForResult(editOrdersIntent, 1);
        });
    }

    private void initializeObjects(){
        backButton = findViewById(R.id.back_button);

        customerName = findViewById(R.id.customer_name);
        customerContactNumber = findViewById(R.id.customer_contact_number);
        customerID = findViewById(R.id.customer_id);
        customerDeliveryAddress = findViewById(R.id.customer_delivery_address);

        dateOrdered = findViewById(R.id.date_ordered);
        orderID = findViewById(R.id.order_id);

        recyclerViewHolder = findViewById(R.id.recycle_view_holder);
        totalOrderAmount = findViewById(R.id.total_order_amount);
        editOrder = findViewById(R.id.edit_order);

        modeOfPaymentContainer = findViewById(R.id.mode_of_payment_container);
        modeOfPayment = findViewById(R.id.mode_of_payment);

        orderStatus = findViewById(R.id.order_status);

        additionalMessage = findViewById(R.id.additional_message);

        backButton2 = findViewById(R.id.back_button_2);
        cancelOrderButton = findViewById(R.id.cancel_order_button);

        // for populating the recycler view item list
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(this));
        orderedItemsConstructorList = new ArrayList<>();
        pendingOrdersCurrentOrderDetailsAdapter = new PendingOrdersCurrentOrderDetailsAdapter(this, orderedItemsConstructorList);
        recyclerViewHolder.setAdapter(pendingOrdersCurrentOrderDetailsAdapter);

        db = FirebaseFirestore.getInstance();
    }

    private void getIntentData(){
        Intent intent = getIntent();
        //pendingOrdersConstructor = intent.getParcelableExtra("current_order");
        String documentReference = intent.getStringExtra("document_id");

        if (documentReference != null) {
            db.collection("pendingOrders")
                    .document(documentReference)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                // Successfully retrieved the document
                                pendingOrdersConstructor = documentSnapshot.toObject(PendingOrdersConstructor.class);
                                // Do something with pendingOrdersConstructor
                                setTextValues();
                                populateOrderList();
                            } else {
                                Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Document reference is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void setTextValues(){
        // address
        Map<String, Object> deliveryAddress = pendingOrdersConstructor.getDelivery_address();
        customerName.setText(String.valueOf(deliveryAddress.get("fullName")));
        customerContactNumber.setText(String.valueOf(deliveryAddress.get("phoneNumber")));
        customerDeliveryAddress.setText(String.valueOf(deliveryAddress.get("deliveryAddress")));

        customerID.setText(String.valueOf(pendingOrdersConstructor.getUser_id()));

        // date ordered
        Timestamp timestamp = pendingOrdersConstructor.getDate_ordered();
        long dateOrderedMillis = timestamp.toDate().getTime();
        Date formatedDateOrdered = new Date(dateOrderedMillis);
        dateOrdered.setText(String.valueOf(formatedDateOrdered));

        // order id
        orderID.setText(String.valueOf(pendingOrdersConstructor.getOrder_id()));

        // total amount
        totalOrderAmount.setText(String.format("Total Amount: ₱" + pendingOrdersConstructor.getTotal_amount()));

        // mode of payment
        modeOfPayment.setText(String.valueOf(pendingOrdersConstructor.getMode_of_payment()));

        // order status
        orderStatus.setText(String.valueOf(pendingOrdersConstructor.getOrder_status()));

        // additional message
        String customerMessage = String.valueOf(pendingOrdersConstructor.getAdditional_message());
        if (customerMessage.isEmpty()){
            additionalMessage.setText(String.format("NONE"));
        } else {
            additionalMessage.setText(customerMessage);
        }

    }

    private void populateOrderList(){
        List<Map<String, Object>> orderItems = pendingOrdersConstructor.getOrder_items();
        for (Map<String, Object> item : orderItems){
            // Extract the item details from the map
            String itemId = (String) item.get("item_id");
            String itemImg = (String) item.get("item_img");
            String itemName = (String) item.get("item_name");
            int itemOrderQuantity = ((Number) item.get("item_order_quantity")).intValue();
            int itemPrice = ((Number) item.get("item_price")).intValue();
            int itemTotalPrice = ((Number) item.get("item_total_price")).intValue();

            // Create a new OrderedItemsConstructor object
            OrderedItemsConstructor orderedItem = new OrderedItemsConstructor(
                    itemId, itemImg, itemName, itemOrderQuantity, itemPrice, itemTotalPrice
            );

            // Add the object to the list
            orderedItemsConstructorList.add(orderedItem);
        }
        pendingOrdersCurrentOrderDetailsAdapter.notifyDataSetChanged();
    }

    private void showCancelOrderDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_cancel_order);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_bg);

        MaterialButton btnCancel = dialog.findViewById(R.id.button_cancel);
        MaterialButton btnOk = dialog.findViewById(R.id.button_ok);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnOk.setOnClickListener(v -> {
            db.collection("pendingOrders")
                .document(pendingOrdersConstructor.getOrder_id())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Intent intent = new Intent(this, MainDashboardUser.class);
                    intent.putExtra("open_fragment", "history");
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "An error occurred cancelling order.", Toast.LENGTH_SHORT).show();
                    Log.d("delete pending order", e.toString());
                });
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            boolean refreshNeeded = data.getBooleanExtra("refreshNeeded", false);
            if (refreshNeeded) {
                // Refresh logic here
                initializeObjects();
                getIntentData();

                // other buttons
                backButton.setOnClickListener(v -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("refreshNeeded", true);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                });

                // to check the payment details
                if (String.valueOf(modeOfPayment.getText()).equals("GCash")){
                    modeOfPaymentContainer.setOnClickListener(view -> {
                        Map<String, Object> gcash_payment_details = pendingOrdersConstructor.getGcash_payment_details();
                        Intent showGCashPaymentIntent = new Intent(this, GCashPaymentDetails.class);
                        showGCashPaymentIntent.putExtra("gcash_payment_details", (Serializable) gcash_payment_details);
                        startActivity(showGCashPaymentIntent);
                    });
                }

                cancelOrderButton.setOnClickListener(v ->{
                    showCancelOrderDialog();
                });

                backButton2.setOnClickListener(v -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("refreshNeeded", true);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                    finish();
                });

                editOrder.setOnClickListener(v -> {
                    Intent editOrdersIntent = new Intent(this, UserEditPendingOrders.class);
                    editOrdersIntent.putExtra("order_id", pendingOrdersConstructor.getOrder_id());
                    startActivityForResult(editOrdersIntent, 1);
                });
            }
        }
    }
}