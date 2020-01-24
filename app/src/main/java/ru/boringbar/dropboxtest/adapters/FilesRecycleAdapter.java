package ru.boringbar.dropboxtest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.v2.files.Metadata;

import java.util.List;

import ru.boringbar.dropboxtest.R;

public class FilesRecycleAdapter extends RecyclerView.Adapter<FilesRecycleAdapter.ItemHolder> {

    LayoutInflater inflater;
    List<Metadata> files;
    ItemClickListener listener;

    public FilesRecycleAdapter(Context context, List<Metadata>  files, ItemClickListener listener){
        inflater = LayoutInflater.from(context);
        this.files = files;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_dropbox_file, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.itemName.setText(files.get(position).getName());
        holder.itemView.setOnClickListener((x)->{
            listener.OnItemClick(files.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder{

        ImageView typeImg;
        TextView itemName;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            typeImg = itemView.findViewById(R.id.item_dropbox_file_img);
            itemName = itemView.findViewById(R.id.item_dropbox_file_name);
        }
    }

    public interface ItemClickListener{
        void OnItemClick(Metadata metadata);
    }
}
