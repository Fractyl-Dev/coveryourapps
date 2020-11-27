package com.example.coveryourapps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.squareup.picasso.Picasso;

import static android.media.ExifInterface.ORIENTATION_NORMAL;
import static android.media.ExifInterface.TAG_ORIENTATION;

public class LendItemFragment extends Fragment implements View.OnClickListener {
    CoverCreatorActvity thisActivity;//
    private EditText itemNameEditText, memoEditText;
    private Button uploadImageButton, returnDateButton, continueButton, takeImageButton;

    private ArrayList<Uri> imageURLs;
    private RecyclerView imagesRecyclerView;

    private ImageView testImage;

    private boolean sentAlready;

    public static final int PICK_IMAGE = 1;

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lend_item, container, false);
        thisActivity = (CoverCreatorActvity) getActivity();

        itemNameEditText = view.findViewById(R.id.itemNameEditText);
        memoEditText = view.findViewById(R.id.memoEditText);
        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        takeImageButton = view.findViewById(R.id.takeImageButton);
        returnDateButton = view.findViewById(R.id.returnDateButton);
        continueButton = view.findViewById(R.id.continueButton);
        takeImageButton.setOnClickListener((this));
        uploadImageButton.setOnClickListener(this);
        returnDateButton.setOnClickListener(this);
        continueButton.setOnClickListener(this);

        imageURLs = new ArrayList<>();
        LinearLayoutManager horizontalLayout = new LinearLayoutManager(thisActivity, LinearLayoutManager.HORIZONTAL, false);
        imagesRecyclerView = view.findViewById(R.id.imagesRecyclerView);
        imagesRecyclerView.setLayoutManager(horizontalLayout);
        imagesRecyclerView.setAdapter(new LendItemFragment.ImagesAdapter(imageURLs));

        sentAlready = false;

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uploadImageButton:
                uploadImage();
                break;
            case R.id.takeImageButton:
                takeImage();
                break;
            case R.id.returnDateButton:
                getReturnDate();
                break;
            case R.id.continueButton:
                if (!sentAlready) {
                    if (!itemNameEditText.getText().toString().equals("") || !memoEditText.getText().toString().equals("")) {
                        createAndUploadCover(itemNameEditText.getText().toString(), memoEditText.getText().toString());
                        sentAlready = true;
                    } else {
                        Toast.makeText(thisActivity, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //Bs because you can't change things that aren't final in DB on success, but you can do it like this
    int recipientIteration;
    private void createAndUploadCover(String itemName, String memoEditText) {
        recipientIteration = 0;
        for (final User recipient : thisActivity.getSelectedRecipients()) {
            // You're supposed to use Map to put data in a Firestore DB
            final Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("content", memoEditText);
            updateMap.put("coverType", "lending");
            updateMap.put("createdTime", new Timestamp(System.currentTimeMillis()));
            updateMap.put("id", "Useless ID for android");
            updateMap.put("memo", itemName);
            updateMap.put("recipientID", recipient.getUid());
            updateMap.put("senderID", DBHandler.getCurrentFirebaseUser().getUid());
            updateMap.put("status", "pending");

            DBHandler.getDB().collection("covers")
                    .add(updateMap)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("**Write A Contract |", "Cover added to DB");

                            //Add Recipient to friends list
                            if (Settings.isAutoAddFriends()) {
                                if (!DBHandler.getAllUserFriends().contains(recipient)) {
                                    DBHandler.getDB().collection("users").document(DBHandler.getCurrentFirebaseUser().getUid())
                                            .update("friends", FieldValue.arrayUnion(recipient.getUid()));
                                }
                            }

                            for (Uri uri : imageURLs) {
                                DBHandler.getDB().collection("covers").document(documentReference.getId())
                                        .update("pictures", FieldValue.arrayUnion(uri.toString()));
                            }
                            recipientIteration ++;
                            if (recipientIteration == thisActivity.getSelectedRecipients().size()) {
                                refresh();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("**Contract Template Argument |", "Cover failed to be added to DB");
                            Toast.makeText(thisActivity, "Failed to send", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    public void refresh() {
        DBHandler.refreshUser(true);
        onRefreshFinished();
    }
    private void onRefreshFinished() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DBHandler.checkIfDoneThinking()) {
                    Log.d("**LendItemFragment |", "Uploaded cover to database and DBHandler has updated, sending to home fragment");
                    Intent nextIntent = new Intent(thisActivity, MainActivity.class);
                    thisActivity.startActivity(nextIntent);
                    Toast.makeText(thisActivity, "Sent successfully", Toast.LENGTH_SHORT).show();
                } else {
                    onRefreshFinished();
                }
            }
        }, DBHandler.getRefreshDelay());
    }

    public void uploadImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    public void takeImage(){
        askCameraPermissions();
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(thisActivity, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
                checkCamera();
        }else{
            openCamera();
        }
    }

    private void checkCamera(){
        if(ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(thisActivity, "Must Allow Camera to Take Photos", Toast.LENGTH_LONG).show();
        }else{
            openCamera();
        }
    }

    private void openCamera(){
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera,CAMERA_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
   /*    if(requestCode == CAMERA_REQUEST_CODE){
           Bitmap image = (Bitmap) data.getExtras().get("data");
       }
*/
        if (requestCode == PICK_IMAGE) {
            if (data == null) {
                Toast.makeText(thisActivity, "Image not able to be processed, please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {//I don't think the try/catch is needed but android gets pissy without it
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                final StorageReference imageRef = storageRef.child("images/" + DBHandler.getCurrentFirebaseUser().getUid() + "/" + System.currentTimeMillis());


                InputStream inputStream = thisActivity.getContentResolver().openInputStream(data.getData());
                UploadTask uploadTask = imageRef.putStream(inputStream);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();

                            //Picasso big good
                            imageURLs.add(downloadUri);
                            imagesRecyclerView.setAdapter(new LendItemFragment.ImagesAdapter(imageURLs));
                            Log.d("**Lend Item Fragment | ", downloadUri.toString());
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });

//                Toast.makeText(thisActivity, "UrlTask url is |   " + urlTask, Toast.LENGTH_LONG).show();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    public void getReturnDate() {

    }

    class ImagesAdapter extends RecyclerView.Adapter<LendItemFragment.ImageViewHolder> {
        private ArrayList<Uri> urls;

        public ImagesAdapter(ArrayList<Uri> urls) {
            super();
            this.urls = urls;
        }

        @NonNull
        @Override
        public LendItemFragment.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new LendItemFragment.ImageViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull LendItemFragment.ImageViewHolder holder, int position) {
            holder.bind(this.urls.get(position));
        }

        @Override
        public int getItemCount() {
            return this.urls.size();
        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        private ImageButton deleteButton;
        private Uri uri;

        public ImageViewHolder(ViewGroup container) {
            super(LayoutInflater.from(getContext()).inflate(R.layout.lend_image_item, container, false));
            image = itemView.findViewById(R.id.image);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Uri theUrl) {
            this.uri = theUrl;
            deleteButton.setOnClickListener(this);


            //Reference the place where the image is stored in firestore
            StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(theUrl.toString());

            httpsReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    //Exif stuff is to rotate images that are incorrectly oriented
                    ExifInterface exifInterface = null;
                    try {
                        exifInterface = new ExifInterface(new ByteArrayInputStream(bytes));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    Matrix matrix = new Matrix(); // Rotate images that are bad
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.setRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.setRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.setRotate(270);
                            break;
                    }
                    //Make new bitmap that is properly oriented
                    Bitmap bmRotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

                    image.setImageBitmap((Bitmap.createBitmap(bmRotated)));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("**Review Cover Fragment", "Image not able to be loaded");
                    Toast.makeText(thisActivity, "Image not able to be loaded", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.deleteButton) {
                Log.d("**LendItemFragment |", "Delete Pressed");
                for (Uri u : imageURLs) {
                    if (u.equals(uri)) {
                        imageURLs.remove(u);
                        imagesRecyclerView.setAdapter(new LendItemFragment.ImagesAdapter(imageURLs));
                        Log.d("**LendItemFragment |", "Found and deleted");
                        break;
                    }
                }
            }
        }
    }
}