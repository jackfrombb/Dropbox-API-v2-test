package ru.boringbar.dropboxtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import ru.boringbar.dropboxtest.easy_drobox.DropboxHelper;
import ru.boringbar.dropboxtest.easy_drobox.DropboxUser;

//TODO ОБЯЗАТЕЛЬНО ЗАМЕНИТЕ app_key в ресурсах на свой, полученный в https://www.dropbox.com/developers/apps

/**
 * Тест работы с Dropbox Api v2
 */
public class MainActivity extends AppCompatActivity {

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

//Жизненный цикл=========================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        users = new ArrayList<>();
        dropboxHelper = new DropboxHelper(this, users);

        //Инициализация View
        {
            usersAdapter = new UsersRecyclerAdapter(this, users);
            ((RecyclerView)findViewById(R.id.main_users_recycler)).setAdapter(usersAdapter);

            findViewById(R.id.main_add_new_user).setOnClickListener(this::OnNewUserAddClick);
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

    private void OnNewUserAddClick(View view){
        dropboxHelper.StartAuthActivity();
    }
}
