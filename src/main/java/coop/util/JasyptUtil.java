package coop.util;

import org.jasypt.util.text.AES256TextEncryptor;

public class JasyptUtil {

    public static String encrypt(String text) {
        AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        String jasyptEncryptorPassword = (String)SessionFactoryUtil.getInstance().getProperties().get("jasypt.encryptor.password");
        textEncryptor.setPassword(jasyptEncryptorPassword);
        return textEncryptor.encrypt(text);
    }

    public static String decrypt(String text) {
        AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        String jasyptEncryptorPassword = (String)SessionFactoryUtil.getInstance().getProperties().get("jasypt.encryptor.password");
        textEncryptor.setPassword(jasyptEncryptorPassword);
        return textEncryptor.decrypt(text);
    }
}
