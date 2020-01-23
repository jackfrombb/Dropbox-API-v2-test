package ru.boringbar.dropboxtest;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.boringbar.dropboxtest.easy_drobox.DropboxUser;

public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.ItemHolder> {

    LayoutInflater inflater;
    List<DropboxUser> users;

    public UsersRecyclerAdapter(Context context, List<DropboxUser> users){
        inflater = LayoutInflater.from(context);
        this.users = users;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_dropbox_user, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        DropboxUser user = users.get(position);
        if(user.photo!=null&&user.photo.length>0)
            holder.userAva.setImageBitmap(BitmapFactory.decodeByteArray(user.photo, 0 , user.photo.length));
        else
            holder.userAva.setImageDrawable(inflater.getContext().getDrawable(R.drawable.ic_user));

        holder.userName.setText(user.name);
        holder.userMail.setText(user.mail);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder{

        ImageView userAva;
        TextView userName, userMail;
        Button selectUser, removeUser;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            userAva = itemView.findViewById(R.id.item_dropbox_user_ava);
            userName = itemView.findViewById(R.id.item_dropbox_user_name);
            userMail = itemView.findViewById(R.id.item_dropbox_user_mail);

            selectUser = itemView.findViewById(R.id.item_dropbox_user_select_btn);
            removeUser = itemView.findViewById(R.id.item_dropbox_user_remove_btn);
        }
    }
}
