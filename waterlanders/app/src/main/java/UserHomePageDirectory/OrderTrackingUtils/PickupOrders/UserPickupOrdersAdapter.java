package UserHomePageDirectory.OrderTrackingUtils.PickupOrders;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.waterlanders.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import AdminHomePageDirectory.Orders.Utils.OnDeliveryOrders.OnDeliveryOrdersConstructor;
import UserHomePageDirectory.OrderTrackingUtils.DeliveryOrders.UserDeliveryOrdersAdapter;
import UserHomePageDirectory.OrderTrackingUtils.DeliveryOrders.UserDeliveryOrdersCurrentOrderDetails;

public class UserPickupOrdersAdapter extends RecyclerView.Adapter<UserPickupOrdersAdapter.UserPickupOrdersAdapterViewHolder> {
    Context context;
    List<OnDeliveryOrdersConstructor> onDeliveryOrdersConstructorList;

    public UserPickupOrdersAdapter(Context context, List<OnDeliveryOrdersConstructor> onDeliveryOrdersConstructorList) {
        this.context = context;
        this.onDeliveryOrdersConstructorList = onDeliveryOrdersConstructorList;
    }

    public static class UserPickupOrdersAdapterViewHolder extends RecyclerView.ViewHolder {
        ImageView orderIMG;
        TextView orderID;
        TextView userID;
        TextView orderPrice;

        public UserPickupOrdersAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIMG = itemView.findViewById(R.id.order_img);
            orderID = itemView.findViewById(R.id.order_id);
            userID = itemView.findViewById(R.id.user_id);
            orderPrice = itemView.findViewById(R.id.order_price);
        }
    }

    @NonNull
    @Override
    public UserPickupOrdersAdapter.UserPickupOrdersAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_pending_orders_card, parent, false);
        return new UserPickupOrdersAdapter.UserPickupOrdersAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return onDeliveryOrdersConstructorList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull UserPickupOrdersAdapter.UserPickupOrdersAdapterViewHolder holder, int position) {
        OnDeliveryOrdersConstructor orderCard = onDeliveryOrdersConstructorList.get(position);

        // display the icon
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(orderCard.getOrder_icon());
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load image using Glide with the download URL
            Glide.with(context).load(uri).into(holder.orderIMG);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
        });

        // set text values.
        holder.orderID.setText(String.format("ORDER ID: " + orderCard.getOrder_id()));
//        holder.userID.setText(String.format("USER ID: " + orderCard.getUser_id()));
        holder.orderPrice.setText(String.format("₱" + orderCard.getTotal_amount()));
        holder.userID.setText(String.format("USERNAME: " + orderCard.getSearch_term()));

        holder.itemView.setOnClickListener(v -> {
            Intent showCurrentOrderDetailsIntent = new Intent(context, UserPickupOrdersCurrentOrderDetails.class);
            showCurrentOrderDetailsIntent.putExtra("current_order", orderCard);
            context.startActivity(showCurrentOrderDetailsIntent);
        });
    }
}
