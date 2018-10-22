package br.edu.uffs.raven;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import br.edu.uffs.raven.Helpers.ProfileHelper;
import br.edu.uffs.raven.InsideChat.ChatActivity;
import br.edu.uffs.raven.Models.Chat;
import br.edu.uffs.raven.Models.Message;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder>{
    private Context context;
    private ArrayList<Chat> chats;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatsAdapter(Context context, ArrayList<Chat> chats) {
        this.context = context;
        this.chats = chats;
    }

    @NonNull
    @Override
    public ChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat, parent, false);
        return new ChatsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsAdapter.ViewHolder holder, int position) {
        final Chat chat = chats.get(position);

        final String username;

        // Username
        if(chat.getUsersIds().get(0).equals(ProfileHelper.getUserId())) {
            holder.txtUsername.setText(chat.getUsersName().get(1));
            username = chat.getUsersName().get(1);
        }
        else {
            holder.txtUsername.setText(chat.getUsersName().get(0));
            username = chat.getUsersName().get(0);
        }

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chatId", chat.getId());
                intent.putExtra("username", username);
                context.startActivity(intent);
            }
        });

        // Last message text and time
        getLastMessage(chat.getId(), holder.txtLastMessage, holder.txtTime);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        CircleImageView civProfilePicture;
        TextView txtUsername, txtLastMessage, txtTime;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.rootView);
            civProfilePicture = itemView.findViewById(R.id.civProfilePicture);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }

    private void getLastMessage(final String chatId, final TextView txtLastMessage, final TextView txtTime){
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()){
                            Message message = snap.toObject(Message.class);
                            txtLastMessage.setText(message.getText());
                            txtTime.setText(sdf.format(message.getCreatedAt()));
                        }
                    }
                });
    }
}
