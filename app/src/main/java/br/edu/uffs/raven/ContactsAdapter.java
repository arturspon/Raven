package br.edu.uffs.raven;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import br.edu.uffs.raven.Helpers.ProfileHelper;
import br.edu.uffs.raven.InsideChat.ChatActivity;
import br.edu.uffs.raven.Models.Chat;
import br.edu.uffs.raven.Models.Message;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder>{
    private Context context;
    private List<String> usersIds;
    private Map<String, String> usersNameMap = new HashMap<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ContactsAdapter(Context context, List<String> usersIds) {
        this.context = context;
        this.usersIds = usersIds;
    }

    @NonNull
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_contact, parent, false);
        return new ContactsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ViewHolder holder, int position) {
        final String user = usersIds.get(position);

        getUser(user, holder.tvUsername);

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("chats")
                        .whereArrayContains("usersIds", ProfileHelper.getUserId())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()) {
                                    List<Chat> chats = new ArrayList<>();
                                    for (DocumentSnapshot snap : task.getResult()) {
                                        chats.add(snap.toObject(Chat.class));
                                    }
                                    boolean chatFound = false;
                                    for(Chat chat : chats){
                                        if(chat.getGroupId() == null
                                                && chat.getUsersIds().contains(user)){
                                            Intent chatIntent = new Intent(context, ChatActivity.class);
                                            chatIntent.putExtra("chatId", chat.getId());
                                            context.startActivity(chatIntent);
                                            chatFound = true;
                                            break;
                                        }
                                    }
                                    if(!chatFound){
                                        final DocumentReference newChatRef = db.collection("chats").document();
                                        final Chat chatToBeCreated = new Chat();
                                        chatToBeCreated.setId(newChatRef.getId());

                                        db.collection("users")
                                                .document(ProfileHelper.getUserId())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        final List<String> usersName = new ArrayList<>();
                                                        usersName.add(ProfileHelper.getUserName());
                                                        usersName.add(usersNameMap.get(user));

                                                        List<String> usersId = new ArrayList<>();
                                                        usersId.add(ProfileHelper.getUserId());
                                                        usersId.add(user);

                                                        chatToBeCreated.setUsersName(usersName);
                                                        chatToBeCreated.setUsersIds(usersId);

                                                        newChatRef.set(chatToBeCreated)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            Intent chatIntent = new Intent(context, ChatActivity.class);
                                                                            chatIntent.putExtra("chatId", newChatRef.getId());
                                                                            chatIntent.putExtra("username", usersName.get(1));
                                                                            context.startActivity(chatIntent);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });

                                    }
                                }
                            }
                        });
            }
        });
    }

    private void getUser(final String userId, final TextView tvUsername){
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        tvUsername.setText(documentSnapshot.getString("name"));
                        usersNameMap.put(userId, documentSnapshot.getString("name"));
                    }
                });
    }

    @Override
    public int getItemCount() {
        return usersIds.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        CircleImageView civUser;
        TextView tvUsername;

        public ViewHolder(View itemView) {
            super(itemView);
            civUser = itemView.findViewById(R.id.civUser);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            rootView = itemView.findViewById(R.id.rootView);
        }
    }
}
