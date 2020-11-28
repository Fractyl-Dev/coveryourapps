package com.example.coveryourapps;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class User {
    private String uid, name, displayName, birthdate;
    private ArrayList<String> notificationTokens = new ArrayList<>();//Fuck you Stephen -Miles
    private ArrayList<String> friends = new ArrayList<>();//Empty upon user creation
    private ArrayList<String> covers = new ArrayList<>();//Empty upon user creation
    private ArrayList<String> displayNames = new ArrayList<>();//Used to check if a display name is taken when generating display name



    public User() {
        // Default empty constructor required for pulling users from FireStore usersDB
    }
    public User(String name, String birthdate) {
        this.name = name;
        this.birthdate = birthdate;

        // Make Display Name
        FirebaseFirestore displayNamesdb = FirebaseFirestore.getInstance();
        displayNamesdb.collection("displayNames")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                String newDisplayName = document.getData().toString();
                                newDisplayName = newDisplayName.replace("{name=", "");
                                newDisplayName = newDisplayName.replace("}", "");
                                displayNames.add(newDisplayName);
                                //Log.d("User | Display Name", "Pulled Display Name: " + newDisplayName);
                            }

                            displayName = generateDisplayName(getName());
                            Log.d("**User | Display Name", "Generated Display Name: " + displayName);
                        }
                    }
                });
    }

    // Generate display name by getting rid of spaces
    private String generateDisplayName(String name, int increment) {
        String suggestedName = name;

        // If they put a space at the end of their name
        if (name.lastIndexOf(" ") == name.length() - 1) {
            suggestedName = suggestedName.substring(0, suggestedName.length() - 1);
        }
        suggestedName = suggestedName.replace(" ", "-").toLowerCase();
        String savedSuggestion = suggestedName;

        //If not first name, add dash before number
        if (increment > 0) {
            suggestedName = suggestedName.concat("-" + increment);
        }
        if (displayNames.contains(suggestedName)) {
            return generateDisplayName(savedSuggestion, increment + 1);
        } else {
            return suggestedName;
        }
    }

    private String generateDisplayName(String name) {
        return generateDisplayName(name, 0);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<String> friendUIDs) {
        this.friends = friendUIDs;
    }

    public ArrayList<String> getCovers() {
        return covers;
    }

    public void setCovers(ArrayList<String> covers) {
        this.covers = covers;
    }

    public void addCover(String cover){
        covers.add(cover);
    }

    public ArrayList<String> getNotificationTokens() {
        return notificationTokens;
    }

    public void setNotificationTokens(ArrayList<String> notificationTokens) {
        this.notificationTokens = notificationTokens;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", birthdate='" + birthdate + '\'' +
                ", notificationTokens=" + notificationTokens +
                ", friends=" + friends +
                ", covers=" + covers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uid, user.uid) &&
                Objects.equals(name, user.name) &&
                Objects.equals(displayName, user.displayName) &&
                Objects.equals(birthdate, user.birthdate) &&
                Objects.equals(notificationTokens, user.notificationTokens) &&
                Objects.equals(friends, user.friends) &&
                Objects.equals(covers, user.covers) &&
                Objects.equals(displayNames, user.displayNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, name, displayName);
    }
}
