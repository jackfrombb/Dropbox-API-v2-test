package ru.boringbar.dropboxtest.easy_drobox;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.boringbar.dropboxtest.R;

/**
 * Помощник при использовании API Dropbox
 */
public class DropboxHelper implements Serializable {

//Теги===================================================================================================

    /**
     * Тег для чтения пользователей
     */
    private static final String USERS_ARRAY_TAG = "aoa,wefsdc028d";

//Поля===================================================================================================

    /**
     * Содержин информацию о приложении
     */
    private String app_key;

    /**
     * Конфигурация приложения для Dropbox
     */
    private DbxRequestConfig requestConfig;

    /**
     * Контекст приложения
     */
    private Context context;

    /**
     * Хранилище токенов
     */
    private ArrayList<DropboxUser> dropboxUsers;

//Конструкторы===========================================================================================

    public DropboxHelper(Context context, ArrayList<DropboxUser> dropboxUsers) {

        this.context = context;

        //Считавыем доступных пользователей
        this.dropboxUsers = dropboxUsers;
        dropboxUsers.addAll(getUsers(context));

        //Получаем ключ из ресурсов
        app_key = context.getString(R.string.app_key);

        //Получаем имя приложения
        String appName = context.getString(R.string.app_name);
        //Получаем версию приложения
        String appVer = "1.0";
        try {
            appVer = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) { e.printStackTrace(); }

        //Инициализируем информацией о приложении
        requestConfig = DbxRequestConfig
                .newBuilder(appName + "/" + appVer)
                .withAutoRetryEnabled()
                .build();

        //Инициализируем информацией о ключах

    }

//Публичные методы=======================================================================================

    /**
     * Проверка на наличие нового токена, если найден то добавляем токена
     */
    public  boolean  CheckAuth(ReadUserInfoListener callback) {

        //Если доступен токен и пользователя с таким токеном ещё нет, то добавляем
        if (Auth.getOAuth2Token() != null) {

            String token = Auth.getOAuth2Token();
            DbxClientV2 clientV2 = new DbxClientV2(requestConfig, token);

            ReadUserInfoAsync readUserInfoAsync = new ReadUserInfoAsync(clientV2, new ReadUserInfoListener() {
                @Override
                public void OnLoadOver(DropboxUser user) {
                    user.token = token;
                    callback.OnLoadOver(user);
                }
            });
            readUserInfoAsync.execute();
            return true;
        }

        else return false;
    }

    /**
     * Запустить авторизацию пользователя
     */
    public void StartAuthActivity(){
        Auth.startOAuth2Authentication(context, app_key);
    }

    /**
     * Добавить нового пользователя, или обновить существующего
     */
    public void addUser(DropboxUser user){

        //Удаляю дубли
        ArrayList<DropboxUser> toRemove = new ArrayList<>();
        for(DropboxUser u : dropboxUsers){
            if(u.id.compareTo(user.id)==0){
                toRemove.add(u);
            }
        }
        dropboxUsers.removeAll(toRemove);

        //Добавляю обновлённые данные
        dropboxUsers.add(user);

        saveUsers(context, dropboxUsers);
    }

    /**
     * Удалить пользователя
     */
    public void removeUser(DropboxUser user){
        dropboxUsers.remove(user);
        saveUsers(context, dropboxUsers);
    }

    /**
     * Считать файлы для пользователя
     */
    public static ListFolderResult getListFolder(DbxClientV2 client, String folder) throws DbxException {

        ListFolderResult folderResult = client.files().listFolder(folder);
        while (folderResult.getHasMore()) {
            folderResult = client.files().listFolderContinue(folderResult.getCursor());
        }

        return folderResult;

    }

    /**
     * Считать эллементы из папки пользователя ассинхронно
     */
    public void getUserFileAsync(DropboxUser user, String folder, ReadFolderListener callback) {
        DbxClientV2 clientV2 = new DbxClientV2(requestConfig, user.token);
        ReadFolderAsync reader = new ReadFolderAsync(clientV2, folder, callback);
        reader.execute();
    }

    //Приватные методы=======================================================================================

    /**
     * Десериализация токенов
     */
    private static ArrayList<DropboxUser> getUsers(Context context){
        try {

            ArrayList<DropboxUser> toReturn = new ArrayList<>();

            FileInputStream inputStream = new FileInputStream(context.getNoBackupFilesDir()+"/"+ USERS_ARRAY_TAG);
            ObjectInputStream ser = new ObjectInputStream(inputStream);
            Object serTokens = ser.readObject();
            if(serTokens!=null){
                toReturn.addAll((ArrayList<DropboxUser>)serTokens);
            }
            ser.close();

            return toReturn;
        }
        catch (Exception ex){
            return new ArrayList<>();
        }
    }

    /**
     * Сериализация токенов
     */
    private static void saveUsers(Context context, ArrayList<DropboxUser> tokens){
        try {
            FileOutputStream outputStream = new FileOutputStream(context.getNoBackupFilesDir() + "/" + USERS_ARRAY_TAG);
            ObjectOutputStream ser = new ObjectOutputStream(outputStream);
            ser.writeObject(tokens);
            ser.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //Вспомогательные классы=================================================================================


    /**
     * Прочитать сведения о пользователе асинхронно
     */
    private static class ReadUserInfoAsync extends AsyncTask<Void, Void, DropboxUser>{

        private ReadUserInfoListener callback;
        private DbxClientV2 clientV2;
        private DropboxUser user;

        ReadUserInfoAsync(DbxClientV2 client, ReadUserInfoListener callback){
            this.callback = callback;
            this.clientV2 = client;
            this.user = new DropboxUser();
        }

        @Override
        protected DropboxUser doInBackground(Void... voids) {
            try {

                //Считываем информацию о аккаунте пользователя
                FullAccount userInfo = clientV2.users().getCurrentAccount();

                //Считываем  фото
                try {
                    URL imgUrl = new URL(userInfo.getProfilePhotoUrl());
                    byte[] buffer = new byte[16384];
                    InputStream reader = imgUrl.openStream();
                    reader.read(buffer);
                    user.photo = buffer;
                    reader.close();

                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
                //Имя
                user.name = userInfo.getName().getDisplayName();
                //ИД аккаунта
                user.id = userInfo.getAccountId();
                //Почту
                user.mail = userInfo.getEmail();

                return user;
            }
            catch (Exception ex){
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(DropboxUser dropboxUser) {
            super.onPostExecute(dropboxUser);
            callback.OnLoadOver(dropboxUser);
        }
    }

    /**
     * Получить эллементы в папках
     */
    private static class ReadFolderAsync extends  AsyncTask<Void, Void, ListFolderResult>{

        DbxClientV2 client;
        ReadFolderListener listener;
        String folder;

        public ReadFolderAsync(DbxClientV2 client, String folder, ReadFolderListener listener){
            this.client = client;
            this.listener = listener;
            this.folder = folder;
        }

        @Override
        protected ListFolderResult doInBackground(Void... voids) {
            try{
                return DropboxHelper.getListFolder(client, folder);
            }catch (Exception ex){
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ListFolderResult metadata) {
            super.onPostExecute(metadata);
            listener.OnFolderLoad(metadata);
        }
    }

    /**
     * Вернуть ссылку на настройки
     */
    public DbxRequestConfig getRequestConfig() {
        return requestConfig;
    }

    //Вспомогательные интерфейсы=============================================================================

    /**
     * Слушатель получения информации о пользователе
     */
    public interface ReadUserInfoListener {
        void OnLoadOver(DropboxUser user);
    }

    /**
     * Сдушатель получения содержимого папки
     */
    public interface ReadFolderListener{
        void OnFolderLoad(ListFolderResult metadata);
    }

}
