package ru.boringbar.dropboxtest.easy_drobox;

import java.io.Serializable;

public class DropboxUser implements Serializable {
    public String id;
    public String mail;
    public String name;
    public String token;
    public byte[] photo;
}
