package com.example.abreak.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.example.abreak.Adapters.MessageAdapter;
import com.example.abreak.ModelClass.messageInbox;
import com.example.abreak.databinding.ActivityInboxViewBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class InboxView extends AppCompatActivity {

    private ActivityInboxViewBinding binding;
    private FirebaseDatabase database;
    private String sender, receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInboxViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra("name");
        String receiver_uid = getIntent().getStringExtra("uid");
        String sender_uid = FirebaseAuth.getInstance().getUid();

        database = FirebaseDatabase.getInstance();

        sender = sender_uid + receiver_uid;
        receiver = receiver_uid + sender_uid;

        ArrayList<messageInbox> messages = new ArrayList<>();
        MessageAdapter messageAdapter = new MessageAdapter(this, messages, sender, receiver);

        binding.inboxRecyclerview.setAdapter(messageAdapter);
        binding.inboxRecyclerview.setLayoutManager(new LinearLayoutManager(this));



        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chat = binding.messagebox.getText().toString();
                Date date = new Date();
                messageInbox message = new messageInbox(sender_uid, chat, date.getTime());
                binding.messagebox.setText("");

                database.getReference().child("chats")
                        .child(sender)
                        .child("messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            messageInbox newChat = snapshot1.getValue(messageInbox.class);
                            newChat.setMessageId(snapshot1.getKey());
                            messages.add(newChat);
                        }
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                String key = database.getReference().push().getKey();
                HashMap<String, Object> recentMsg = new HashMap<>();
                recentMsg.put("recentmsg", message.getMessage());
                recentMsg.put("recentmsgTime", date.getTime());

                database.getReference().child("chats").child(sender).updateChildren(recentMsg);
                database.getReference().child("chats").child(receiver).updateChildren(recentMsg);
                database.getReference().child("chats")
                        .child(sender)
                        .child("messages")
                        .child(key)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.getReference().child("chats")
                                        .child(receiver)
                                        .child("messages")
                                        .child(key)
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void bVoid) {

                                            }
                                        });

                            }
                        });
            }
        });

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}