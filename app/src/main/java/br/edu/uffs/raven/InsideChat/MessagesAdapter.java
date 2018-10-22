package br.edu.uffs.raven.InsideChat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.edu.uffs.raven.Helpers.ProfileHelper;
import br.edu.uffs.raven.Models.Message;
import br.edu.uffs.raven.R;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private Context context;
    private List<Message> messages;
    private static final int LAYOUT_NORMAL = 0, LAYOUT_CURRENT_USER = 1;

    public MessagesAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    public void addMoreMessages(List<Message> moreMessages){ this.messages.addAll(moreMessages); }

    public void addSingleMessage(Message message){ this.messages.add(0, message); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;

        if(viewType == LAYOUT_NORMAL)
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message, parent, false);
        else if(viewType == LAYOUT_CURRENT_USER)
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message_current_user, parent, false);

        return new MessagesAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Message message = messages.get(position);
        holder.tvMessage.setText(message.getText());
    }

    @Override
    public int getItemViewType(int position) {
        Message currentMsg = messages.get(position);

        if(currentMsg.getUserId().equals(ProfileHelper.getUserId()))
            return LAYOUT_CURRENT_USER;
        else
            return LAYOUT_NORMAL;

        //return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        public ViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}
