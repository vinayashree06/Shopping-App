package com.example.shop;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop.Model.Cart;
import com.example.shop.Prevalent.Prevalent;
import com.example.shop.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button NextProcessBtn;
    private TextView txtTotalAmount,txtMsg1;
    private int overTotalPrice=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        NextProcessBtn = findViewById(R.id.next_btn);
        txtTotalAmount = findViewById(R.id.total_price);
        txtMsg1 = findViewById(R.id.msg1);


        NextProcessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtTotalAmount.setText("Total Price=₹"+String.valueOf(overTotalPrice));
                Intent intent=new Intent(CartActivity.this, ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price", String.valueOf(overTotalPrice));
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        CheckOrderState();
        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

        FirebaseRecyclerOptions<Cart> options = new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef.child("User View")
                        .child(Prevalent.currentOnlineUser.getPhone())
                        .child("Products"), Cart.class)
                .build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull Cart model) {
                holder.txtProductQuantity.setText("Quantity = " + model.getQuantity());
                holder.txtProductPrice.setText("Price ₹" + model.getPrice());
                holder.txtProductName.setText(model.getPname());

                int onetyprProductTPrice=((Integer.valueOf(model.getPrice()))) * (int)model.getQuantity();

                overTotalPrice=overTotalPrice+onetyprProductTPrice;

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options[]= new CharSequence[]
                                {
                                        "Edit",
                                        "Remove"
                                };
                        AlertDialog.Builder builder= new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options");
                         builder.setItems(options, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int i) {

                                 if (i == 0){
                                 Intent intent=new Intent(CartActivity.this, ProductDetailsActivity.class);
                                 intent.putExtra("pid", model.getPid());
                                 startActivity(intent);
                             }
                             if (i == 1){
                                 cartListRef.child("User View")
                                         .child(Prevalent.currentOnlineUser.getPhone())
                                         .child("Products")
                                         .child(model.getPid())
                                         .removeValue()
                                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                 if (task.isSuccessful()){
                                                     Toast.makeText(CartActivity.this,"Item removed successfully.", Toast.LENGTH_SHORT).show();
                                                     Intent intent=new Intent(CartActivity.this, HomeActivity.class);
                                                     startActivity(intent);
                                                 }
                                             }
                                         });

                             }
                             }
                         });
                         builder.show();
                    }
                });
            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                return new CartViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void CheckOrderState(){
        DatabaseReference ordersRef;
        ordersRef=FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()){
                String shippingState=snapshot.child("state").getValue().toString();
                String userName=snapshot.child("name").getValue().toString();
                
                if(shippingState.equals("shipped")){
                    txtTotalAmount.setText("Dear" +userName+"\n order is shipped successfully");
                    recyclerView.setVisibility(View.GONE);

                    txtMsg1.setVisibility(View.VISIBLE);
                    NextProcessBtn.setVisibility(View.GONE);

                    Toast.makeText(CartActivity.this,"You can purchase more products, once you received your first final order",Toast.LENGTH_SHORT).show();
                } else if (shippingState.equals("not shipped")) {
                    txtTotalAmount.setText("Shipping State= Not shipped" );
                    recyclerView.setVisibility(View.GONE);

                    txtMsg1.setVisibility(View.VISIBLE);
                    txtMsg1.setText("Congratulations your final order has been shipped successfully. Soon you will receive your order at your doorstep.");
                    NextProcessBtn.setVisibility(View.GONE);

                    Toast.makeText(CartActivity.this,"You can purchase more products, once you received your first final order",Toast.LENGTH_SHORT).show();
                }
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
