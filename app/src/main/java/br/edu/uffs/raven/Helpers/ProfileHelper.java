package br.edu.uffs.raven.Helpers;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileHelper {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static String getUserId(){ return "id0"; }

    public static String getUserName(){ return auth.getCurrentUser().getDisplayName(); }
}
