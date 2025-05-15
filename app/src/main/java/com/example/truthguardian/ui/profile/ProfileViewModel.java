package com.example.truthguardian.ui.profile;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.truthguardian.model.User;
import com.example.truthguardian.model.UserUpdateRequest;
import com.example.truthguardian.repository.UserRepository;

import static android.content.Context.MODE_PRIVATE;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends AndroidViewModel {
    private static final String TAG = "ProfileViewModel";
    private static final String PREFS_NAME = "AuthPrefs";

    private final UserRepository userRepository;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application.getApplicationContext());
        loadUserProfile();
    }

    public void loadUserProfile() {
        isLoadingLiveData.setValue(true);
        userRepository.getCurrentUser(new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                userLiveData.postValue(user);
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue(error);
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void updateProfile(String name, String username, String phone, String bio, List<String> interests, UserRepository.UserCallback externalCallback) {
        isLoadingLiveData.setValue(true);
        UserUpdateRequest request = new UserUpdateRequest(name, username, phone, null, bio);
        request.setInterests(interests);

        userRepository.updateUser(request, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "用户资料更新成功：" + user);
                userLiveData.postValue(user);
                isLoadingLiveData.postValue(false);
                if (externalCallback != null) {
                    externalCallback.onSuccess(user);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "更新用户资料失败: " + error);
                errorLiveData.postValue(error);
                isLoadingLiveData.postValue(false);
                if (externalCallback != null) {
                    externalCallback.onError(error);
                }
            }
        });
    }

    public void updateProfile(String name, String username, String phone, String bio, List<String> interests) {
        updateProfile(name, username, phone, bio, interests, null);
    }

    public void updateAvatar(Uri avatarUri) {
        isLoadingLiveData.setValue(true);
        Log.d(TAG, "开始上传头像: " + avatarUri);
        
        userRepository.uploadAvatar(avatarUri, new UserRepository.UploadCallback() {
            @Override
            public void onSuccess(String avatarUrl) {
                Log.d(TAG, "头像上传成功，返回的URL: " + avatarUrl);
                User currentUser = userLiveData.getValue();
                if (currentUser != null) {
                    UserUpdateRequest request = new UserUpdateRequest(
                        currentUser.getName(),
                        currentUser.getUserName(),
                        currentUser.getPhone(),
                        avatarUrl,
                        currentUser.getBio()
                    );
                    if (currentUser.getInterests() != null) {
                        request.setInterests(List.of(currentUser.getInterests()));
                    }
                    
                    Log.d(TAG, "准备更新用户信息，包含新的头像URL: " + avatarUrl);
                    userRepository.updateUser(request, new UserRepository.UserCallback() {
                        @Override
                        public void onSuccess(User user) {
                            Log.d(TAG, "用户信息更新成功，新的头像URL: " + user.getAvatarUrl());
                            userLiveData.postValue(user);
                            isLoadingLiveData.postValue(false);
                            errorLiveData.postValue("头像更新成功");
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "更新用户信息失败: " + error);
                            errorLiveData.postValue("更新用户信息失败: " + error);
                            isLoadingLiveData.postValue(false);
                        }
                    });
                } else {
                    Log.e(TAG, "当前用户信息不存在");
                    errorLiveData.postValue("当前用户信息不存在");
                    isLoadingLiveData.postValue(false);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "上传头像失败: " + error);
                errorLiveData.postValue("上传头像失败: " + error);
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void logout() {
        userRepository.logout();
        userLiveData.setValue(null);
    }

    // LiveData getters
    public LiveData<User> getUserProfile() {
        return userLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
} 