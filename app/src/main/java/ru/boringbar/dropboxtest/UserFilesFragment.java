package ru.boringbar.dropboxtest;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.util.ArrayList;
import java.util.List;

import ru.boringbar.dropboxtest.adapters.FilesRecycleAdapter;
import ru.boringbar.dropboxtest.easy_drobox.DropboxHelper;
import ru.boringbar.dropboxtest.easy_drobox.DropboxUser;

/**
 * Фрагмент для удобства отображения файлов пользователя
 */
public class UserFilesFragment extends Fragment {

    private static final String DBX_REQUEST_CONFIG_TAG = "sdfdvdfvs";

//Поля===================================================================================================

    /**
     * Слушатель событий фрагмента
     */
    private USerFilesFragmentListener mListener;

    private FilesRecycleAdapter userFilesAdapter;

    private List<Metadata> files;

    private DropboxUser selectedUser;

    private DropboxHelper selectedHelper;

    private String selectedFolder;

//View's=================================================================================================



//Пустой конструктор=====================================================================================
    public UserFilesFragment() {}
//Жизненный цикл=========================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.files = new ArrayList<>();
        userFilesAdapter = new FilesRecycleAdapter(getContext(), files, new FilesRecycleAdapter.ItemClickListener() {
            @Override
            public void OnItemClick(Metadata metadata) {

                if(metadata instanceof FileMetadata)
                    mListener.OnFileClick(selectedUser, metadata);

                else if(metadata instanceof FolderMetadata)
                    mListener.OnDirectoryClick(selectedUser, metadata);

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_user_files, container, false);
        RecyclerView recyclerView = ((RecyclerView)mainView.findViewById(R.id.user_files_list));
        recyclerView.setAdapter(userFilesAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        return mainView;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof USerFilesFragmentListener) {
            mListener = (USerFilesFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement USerFilesFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

//Публичные методы=======================================================================================

    /**
     * Получение фрагмента
     */
    public static UserFilesFragment newInstance() {
        UserFilesFragment fragment = new UserFilesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void OpenFolder(DropboxHelper helper, DropboxUser user, String folder){

        Log.i("UserFilesFragment","Folder: "+folder);

        selectedUser = user;
        selectedHelper = helper;
        selectedFolder = folder;

        helper.getUserFileAsync(user, folder, new DropboxHelper.ReadFolderListener() {
            @Override
            public void OnFolderLoad(ListFolderResult metadata) {
                if(files == null) files = new ArrayList<>(); else files.clear();
                files.addAll(metadata.getEntries());
                userFilesAdapter.notifyDataSetChanged();
                UserFilesFragment.this.selectedFolder = folder;
            }
        });
    }

    public boolean FolderBack(){
        if(selectedFolder==null||selectedUser==null||selectedHelper==null){
            mListener.OnUserFilesFragmentError(ErrorType.NOT_INITIALIZE);
            return false;
        }
        else {
            if(selectedFolder.contains("/")){
                OpenFolder(selectedHelper,selectedUser, selectedFolder.substring(0, selectedFolder.lastIndexOf("/")));
                return true;
            }
            else {
                return false;
            }
        }
    }

//Интерфейсы=============================================================================================

    /**
     * Интерфейс слушателя фрагмента
     */
    public interface USerFilesFragmentListener {
        void OnDirectoryClick(DropboxUser user, Metadata directory);
        void OnFileClick(DropboxUser user, Metadata file);
        void OnUserFilesFragmentError(ErrorType errorType);
    }

    public enum ErrorType{NOT_INITIALIZE}
}
