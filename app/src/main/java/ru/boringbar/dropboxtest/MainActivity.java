package ru.boringbar.dropboxtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dropbox.core.util.StringUtil;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import ru.boringbar.dropboxtest.adapters.UsersRecyclerAdapter;
import ru.boringbar.dropboxtest.easy_drobox.DropboxHelper;
import ru.boringbar.dropboxtest.easy_drobox.DropboxUser;

//TODO ОБЯЗАТЕЛЬНО ЗАМЕНИТЕ app_key в ресурсах на свой, полученный в https://www.dropbox.com/developers/apps

/**
 * Тест работы с Dropbox Api v2
 */
public class MainActivity extends AppCompatActivity
        implements UserFilesFragment.USerFilesFragmentListener {

    public static final String FILES_FRAGMENT_TAG = "skw9mdks";

//Поля===================================================================================================

    /**
     * Помощник по работе с Dropbox
     */
    DropboxHelper dropboxHelper;

    /**
     * Адаптер для отображения добавленных пользователей
     */
    UsersRecyclerAdapter usersAdapter;

    /**
     * Для хранения инормации о добавленных пользователях
     */
    ArrayList<DropboxUser> users;

    /**
     * Для управления фрагментами
     */
    FragmentManager fragmentManager;

    /**
     * Просмотр содержимого облака
     */
    UserFilesFragment filesFragment;

    /**
     * Кнопка для добавления пользователей, или возвращения из директорий
     */
    FloatingActionButton universalButton;
//Жизненный цикл=========================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        users = new ArrayList<>();
        dropboxHelper = new DropboxHelper(this, users);
        fragmentManager = getSupportFragmentManager();

        //Инициализация View
        {
            //Настройка и установка адаптера для отображения пользователей
            usersAdapter = new UsersRecyclerAdapter(this, users, new UsersRecyclerAdapter.ClickListener() {
                @Override
                public void OnOpenFolderClick(DropboxUser user) {
                    MainActivity.this.OnOpenFolderClick(user);
                }

                @Override
                public void OnRemoveUserClick(DropboxUser user) {
                    dropboxHelper.removeUser(user);
                    usersAdapter.notifyDataSetChanged();
                }
            });
            ((RecyclerView)findViewById(R.id.main_users_recycler)).setAdapter(usersAdapter);

            universalButton = findViewById(R.id.main_add_new_user);
            //Нажатие Добавить пользователя
            SetUniButtonForUser();

            //Подготовка фрагмента показа содержимого облака
            filesFragment = UserFilesFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(R.id.main_files_view_layout, filesFragment, FILES_FRAGMENT_TAG)
                    .hide(filesFragment)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Проверяем есть сведения о новом пользователе
        dropboxHelper.CheckAuth((userInfo)->{
            dropboxHelper.addUser(userInfo);
            usersAdapter.notifyDataSetChanged();
            Log.i(this.toString(), "Добавлен");
        });

    }

//Методы и события=======================================================================================

    /**
     * Событие Добавить пользователя
     */
    private void OnNewUserAddClick(View view){
        dropboxHelper.StartAuthActivity();
    }

    /**
     * Событие начала просмотра содержимого облака
     */
    private void OnOpenFolderClick(DropboxUser user){
        SetUniButtonForFolder("");
        fragmentManager.beginTransaction().show(filesFragment).commit();
        filesFragment.OpenFolder(dropboxHelper, user, "");
    }

    /**
     * Закрыть фрагмент с облаком
     */
    private void OnCloseFolderClick(){
        if(filesFragment.isVisible()){
            fragmentManager.beginTransaction().hide(filesFragment).commit();
        }
        SetUniButtonForUser();
    }

    /**
     * Плавающая кнопка отвечает за возврат в просмотре файлов
     */
    private void SetUniButtonForFolder(String folder) {
        universalButton.setImageDrawable(getDrawable(R.drawable.ic_back));
        universalButton.setOnClickListener((X) -> {
            if (!filesFragment.FolderBack()) {
                OnCloseFolderClick();
            }
        });
    }

    /**
     * Плавающая кнопка отвечает за пользователей
     */
    private void SetUniButtonForUser(){
        universalButton.setImageDrawable(getDrawable(R.drawable.ic_plus));
        universalButton.setOnClickListener(this::OnNewUserAddClick);
    }

    @Override
    public void OnDirectoryClick(DropboxUser user, Metadata directory) {
        filesFragment.OpenFolder(dropboxHelper,user, directory.getPathLower());
        SetUniButtonForFolder(directory.getPathDisplay());
    }

    @Override
    public void OnFileClick(DropboxUser user, Metadata file) {
        DbxClientV2 clientV2 = new DbxClientV2(dropboxHelper.getRequestConfig(), user.token);
        Thread thread = new Thread(()->{
            try {
               Log.i("URL", clientV2.files().getTemporaryLink(file.getPathDisplay()).getLink());
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        });
        thread.start();
    }

    @Override
    public void OnUserFilesFragmentError(UserFilesFragment.ErrorType errorType) {
        switch (errorType){
            case NOT_INITIALIZE:
                OnCloseFolderClick();
                Log.e("UserFragmenError","NOT INITIALIZE");
                break;
        }
    }

}
