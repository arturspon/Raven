package br.edu.uffs.raven.Helpers;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileHelper {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static String getUserId(){ return auth.getUid(); }

    public static String getUserName(){ return auth.getCurrentUser().getDisplayName(); }
}
