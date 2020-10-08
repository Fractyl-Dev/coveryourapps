package com.example.coveryourapps;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.util.Listener;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class DBHandler extends Application {
    private static DBHandler thisHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        thisHandler = this;
        refreshUser(true);
    }

    public static DBHandler getInstance() {
        return thisHandler;
    }

    private static ArrayList<Cover> allUserCovers;
    private static ArrayList<User> allUserFriends;
    private static ArrayList<ContractTemplate> allContractTemplates;

    private static FirebaseFirestore DB;
    private static User currentUser;
    private static FirebaseAuth mAuth;
    private static FirebaseUser currentFirebaseUser;

    private static int refreshDelay = 100;

    private static boolean currentUserFound, sentCoversChecked, receivedCoversChecked, friendsChecked, contractTemplatesChecked;
    private static boolean googleQuitDuringAccountCreation;

    public static void refreshUser(final boolean refreshAllToo) {
        //Setting up database and current user
        mAuth = FirebaseAuth.getInstance();
        currentFirebaseUser = mAuth.getCurrentUser();
        googleQuitDuringAccountCreation = false;

        if (currentFirebaseUser != null) {
            currentUserFound = false;
            DB = FirebaseFirestore.getInstance();
            DB.collection("users")
                    .whereEqualTo("uid", currentFirebaseUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                boolean isSizeGreaterThan0 = false;
                                for (DocumentSnapshot userSnapshot : task.getResult()) {
                                    currentUser = userSnapshot.toObject(User.class);

                                    //Friends list needs to refresh user in order to be accurate, so it calls refreshUser
                                    if (refreshAllToo) {
                                        refreshCovers();
                                        refreshContractTemplates();
                                    }

                                    //Refresh notification token
                                    if (!currentUser.getNotificationTokens().contains(FirebaseInstanceId.getInstance().getToken())) {
                                        DBHandler.getDB().collection("users").document(currentFirebaseUser.getUid())
                                                .update("notificationTokens", FieldValue.arrayUnion(FirebaseInstanceId.getInstance().getToken()));
                                        currentUser.getNotificationTokens().add(FirebaseInstanceId.getInstance().getToken());

                                    }

                                    refreshFriendsList();
//                                    Log.e("**DB Handler", "Length is bigger than 0");
                                    isSizeGreaterThan0 = true;
                                }
                                if (isSizeGreaterThan0) {
                                    Log.d("**DB Handler", "Current User is Found");
                                    currentUserFound = true;
                                } else {
                                    // No user found in the database, but the user is authenticated. This can only happen when the app is quit after
                                    // signing up with google and no birthday is entered. Send to birthday screen.
                                    Log.e("**DB Handler", "User quit app during google authentication, send to login");
                                    googleQuitDuringAccountCreation = true;
                                }
                            } else {
                                declareError();
                            }
                        }
                    });
        }
    }

    private static int nonFinalSenderIteration = 0;
    private static int nonFinalRecipientIteration = 0;
    private static int nonFinalSenderResultSize;
    private static int nonFinalRecipientResultSize;
    private static ArrayList<Cover> newAllUserCovers;



    public static void refreshCovers() {
        if (allUserCovers == null) {
            allUserCovers = new ArrayList<>();
        }
        newAllUserCovers = new ArrayList<>();
        sentCoversChecked = false;
        receivedCoversChecked = false;
        nonFinalSenderIteration = 0;
        nonFinalRecipientIteration = 0;
        //Populate all covers sent by you
        DB.collection("covers")
                .whereEqualTo("senderID", currentFirebaseUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (task.getResult().size() != 0) {
                                for (DocumentSnapshot coversSnapshot : task.getResult()) {
                                    final Cover cover = coversSnapshot.toObject(Cover.class);
                                    assert cover != null;//Needed so android studio doesn't think it's wrong
                                    cover.setDocID(coversSnapshot.getId());
                                    cover.setSender(currentUser);

                                    nonFinalSenderResultSize = task.getResult().size();

                                    DB.collection("users")
                                            .document(cover.getRecipientID())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful() && task.getResult() != null) {
                                                        cover.setRecipient(task.getResult().toObject(User.class));
//                                                        addCoverToAllUserCovers(cover);
//                                                        allUserCovers.add(cover);
                                                        newAllUserCovers.add(cover);
                                                        nonFinalSenderIteration++;
                                                        if (nonFinalSenderIteration == nonFinalSenderResultSize) {
                                                            Log.d("**DBHandler |", "Sent Covers Checked to true");
                                                            sentCoversChecked = true;
                                                        }
                                                    } else {
                                                        declareError();
                                                    }
                                                }
                                            });
                                }
                            } else {
                                Log.d("**DBHandler |", "No Sent Covers, sent covers checked to true");
                                sentCoversChecked = true;
                            }
                        } else {
                            declareError();
                        }
                    }
                });

        //Populate all covers you've received
        DB.collection("covers")
                .whereEqualTo("recipientID", currentFirebaseUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (task.getResult().size() != 0) {
                                for (DocumentSnapshot coversSnapshot : task.getResult()) {
                                    final Cover cover = coversSnapshot.toObject(Cover.class);
                                    assert cover != null;//Needed so android studio doesn't think it's wrong
                                    cover.setDocID(coversSnapshot.getId());
                                    cover.setRecipient(currentUser);
                                    nonFinalRecipientResultSize = task.getResult().size();

                                    DB.collection("users")
                                            .document(cover.getSenderID())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful() && task.getResult() != null) {
                                                        cover.setSender(task.getResult().toObject(User.class));
//                                                        addCoverToAllUserCovers(cover);
//                                                        allUserCovers.add(cover);
                                                        newAllUserCovers.add(cover);
                                                        nonFinalRecipientIteration++;
                                                        if (nonFinalRecipientIteration == nonFinalRecipientResultSize) {
                                                            Log.d("**DBHandler |", "Received Covers Checked to true");
                                                            receivedCoversChecked = true;
                                                        }
                                                    } else {
                                                        declareError();
                                                    }
                                                }
                                            });
                                }
                            } else {
                                Log.d("**DBHandler |", "No Received Covers, Received covers checked to true");
                                receivedCoversChecked = true;
                            }
                        } else {
                            declareError();
                        }
                    }
                });
    }

    public static void refreshFriendsList() {
        friendsChecked = false;
        allUserFriends = new ArrayList<>();
        if (currentUser.getFriends().size() != 0) {
            for (String friendUID : currentUser.getFriends()) {
                DB.collection("users")
                        .whereEqualTo("uid", friendUID)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    for (DocumentSnapshot usersSnapshot : task.getResult()) {
                                        User user = usersSnapshot.toObject(User.class);
//                                        addFriendToAllUserFriends(user);
                                        allUserFriends.add(user);
                                        if (currentUser.getFriends().size() == allUserFriends.size()) {
                                            Log.d("**DBHandler |", "FriendsChecked to true");
                                            friendsChecked = true;
                                        }
                                    }
                                } else {
                                    declareError();
                                }
                            }
                        });
            }
        } else {
            Log.d("DBHandler |", "No Friends Found, FriendsChecked to true");
            friendsChecked = true;
        }


    }

    public static void refreshContractTemplates() {
        contractTemplatesChecked = false;
        allContractTemplates = new ArrayList<>();
        DB.collection("contractTemplates")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DocumentSnapshot contractTemplateSnapshot : task.getResult()) {
                                ContractTemplate contractTemplate = contractTemplateSnapshot.toObject(ContractTemplate.class);
//                                addContractTemplateToAllContractTemplates(contractTemplate);
                                allContractTemplates.add(contractTemplate);
                            }
                            Log.d("**DBHandler |", "ContractTemplatesChecked to true");
                            contractTemplatesChecked = true;
                        } else {
                            declareError();
                        }
                    }
                });
    }

//    private static void addCoverToAllUserCovers(Cover cover) {
//        allUserCovers.add(cover);
//        Log.d("**DBHandler |", "Cover " + cover.toString() + " added to allUserCovers");
//    }
//
//    private static void addFriendToAllUserFriends(User friend) {
//        allUserFriends.add(friend);
//        Log.d("**DBHandler |", "User " + friend.toString() + " added to allUserFriends");
//    }
//    private static void addContractTemplateToAllContractTemplates(ContractTemplate cT) {
//        allContractTemplates.add(cT);
//        Log.d("**DBHandler |", "Contract Template " + cT.toString() + " added to allUserContractTemplates");
//    }

    private static void declareError() {
        Toast.makeText(getInstance(), "We are currently unable to reach the database, please try again later", Toast.LENGTH_SHORT).show();
        Log.e("**DBHandler | ", "Unable to find current user in DB");
    }

    private static boolean topDown = true;

    private static void setTopDown(boolean value) {
        topDown = value;
    }

    public static boolean checkIfDoneThinking() {
        if (currentUserFound && sentCoversChecked && receivedCoversChecked && friendsChecked && contractTemplatesChecked) {
            //Check what covers were already there, record their dropdown value
            for (Cover oldCover : allUserCovers) {
                for (Cover newCover : newAllUserCovers) {
                    //Calling toString because even after overriding .equals, it didn't want to work
                    if (oldCover.toString().equals(newCover.toString())) {
                        newCover.setDroppedDown(oldCover.isDroppedDown());
                    }
                }
            }
            allUserCovers.clear();
            allUserCovers.addAll(newAllUserCovers);
            //Sort the array before declaring that it's done thinking
            Collections.sort(allUserCovers, new Comparator<Cover>() {
                public int compare(Cover o1, Cover o2) {
                    if (topDown) {
                        return o2.getCreatedTime().compareTo(o1.getCreatedTime());
                    } else {
                        return o1.getCreatedTime().compareTo(o2.getCreatedTime());

                    }
                }
            });
            Log.d("**DBHandler |", "Done thinking");
            return true;
        }
        return false;
    }

    //Getters and setters

    public static ArrayList<Cover> getAllUserCovers() {
        return allUserCovers;
    }

    public static void setAllUserCovers(ArrayList<Cover> allUserCovers) {
        DBHandler.allUserCovers = allUserCovers;
    }

    public static ArrayList<User> getAllUserFriends() {
        return allUserFriends;
    }

    public static void setAllUserFriends(ArrayList<User> allUserFriends) {
        DBHandler.allUserFriends = allUserFriends;
    }

    public static ArrayList<ContractTemplate> getAllContractTemplates() {
        return allContractTemplates;
    }

    public static void setAllContractTemplates(ArrayList<ContractTemplate> allContractTemplates) {
        DBHandler.allContractTemplates = allContractTemplates;
    }

    public static FirebaseFirestore getDB() {
        return DB;
    }

    public static void setDB(FirebaseFirestore DB) {
        DBHandler.DB = DB;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        DBHandler.currentUser = currentUser;
    }

    public static FirebaseAuth getmAuth() {
        return mAuth;
    }

    public static void setmAuth(FirebaseAuth mAuth) {
        DBHandler.mAuth = mAuth;
    }

    public static FirebaseUser getCurrentFirebaseUser() {
        return currentFirebaseUser;
    }

    public static void setCurrentFirebaseUser(FirebaseUser currentFirebaseUser) {
        DBHandler.currentFirebaseUser = currentFirebaseUser;
    }

    public static int getRefreshDelay() {
        return refreshDelay;
    }

    public static boolean isGoogleQuitDuringAccountCreation() {
        return googleQuitDuringAccountCreation;
    }

    public static void setGoogleQuitDuringAccountCreation(boolean googleQuitDuringAccountCreation) {
        DBHandler.googleQuitDuringAccountCreation = googleQuitDuringAccountCreation;
    }
}