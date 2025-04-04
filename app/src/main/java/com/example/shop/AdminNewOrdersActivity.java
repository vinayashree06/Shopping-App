package com.example.shop;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop.Model.AdminOrders;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminNewOrdersActivity extends AppCompatActivity {
    private RecyclerView ordersList;
    private DatabaseReference ordersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_new_orders);
        ordersRef= FirebaseDatabase.getInstance().getReference().child("Orders");

        ordersList=findViewById(R.id.orders_list);
        ordersList.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<AdminOrders> options=
                new FirebaseRecyclerOptions.Builder<AdminOrders>()
                        .setQuery(ordersRef,AdminOrders.class)
                        .build();
        FirebaseRecyclerAdapter<AdminOrders, AdminOrdersViewHolder>adapter =
                new FirebaseRecyclerAdapter<AdminOrders, AdminOrdersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AdminOrdersViewHolder holder, int position, @NonNull AdminOrders model) {
                      holder.userName.setText("Name:"+ model.getName());
                        holder.userPhoneNumber.setText("Phone:"+ model.getPhone());
                        holder.userTotalPrice.setText("Total Amount:"+ model.getTotalAmount());

                        holder.userDateTime.setText("Order at:"+ model.getDate()+""+model.getTime());

                        holder.userShippingAddress.setText("Shipping Address:"+ model.getAddress()+","+model.getCity());

                        holder.ShowOrdersBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int currentPosition = holder.getAdapterPosition();
                                if (currentPosition != RecyclerView.NO_POSITION) { // Ensure the position is valid
                                    String uID = getRef(currentPosition).getKey();
                                    Intent intent = new Intent(AdminNewOrdersActivity.this, AdminUserProductsActivity.class);
                                    intent.putExtra("uid", uID);
                                    startActivity(intent);
                                }
                            }
                        });
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{
                                        "Yes",
                                        "No"
                                };
                                AlertDialog.Builder builder = new AlertDialog.Builder(AdminNewOrdersActivity.this);
                                builder.setTitle("Have you shipped this order products?");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        int currentPosition = holder.getAdapterPosition();
                                        if (currentPosition != RecyclerView.NO_POSITION) { // Ensure the position is valid
                                            if (i == 0) {
                                                String uID = getRef(currentPosition).getKey();
                                                RemoverOrder(uID);
                                            } else {
                                                dialog.dismiss(); // Close the dialog on "No"
                                            }
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });





                    }

                    @NonNull
                    @Override
                    public AdminOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_layout,parent,false);
                        return new AdminOrdersViewHolder(view);
                    }
                };
        ordersList.setAdapter(adapter);
        adapter.startListening();
    }

    private void RemoverOrder(String uID) {
        ordersRef.child(uID).removeValue();
    }

    public static class AdminOrdersViewHolder extends RecyclerView.ViewHolder{
       public TextView userName, userPhoneNumber, userTotalPrice, userDateTime, userShippingAddress;
       public Button ShowOrdersBtn;
        public AdminOrdersViewHolder(@NonNull View itemView) {

            super(itemView);

            userName=itemView.findViewById(R.id.order_user_name);
            userPhoneNumber=itemView.findViewById(R.id.order_phone_number);
            userTotalPrice=itemView.findViewById(R.id.order_total_price);
            userDateTime=itemView.findViewById(R.id.order_date_time);
            userShippingAddress=itemView.findViewById(R.id.order_address_city);

            ShowOrdersBtn=itemView.findViewById(R.id.show_all_products_btn);


        }
    }
}