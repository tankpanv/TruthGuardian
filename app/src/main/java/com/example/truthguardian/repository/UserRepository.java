package com.example.truthguardian.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.api.AuthService;
import com.example.truthguardian.model.User;
import com.example.truthguardian.model.UserResponse;
import com.example.truthguardian.model.UserUpdateRequest;
import com.example.truthguardian.model.UploadResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_BIO = "bio";
    private static final String KEY_AVATAR_URL = "avatar_url";
    private static final String KEY_INTERESTS = "interests";

    private final SharedPreferences sharedPreferences;
    private final AuthService authService;
    private final Context context;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserRepository(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.authService = ApiClient.getClient().create(AuthService.class);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface UploadCallback {
        void onSuccess(String url);
        void onError(String error);
    }

    public void getCurrentUser(UserCallback callback) {
        // 首先检查登录状态
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        String token = sharedPreferences.getString(KEY_TOKEN, null);

        if (!isLoggedIn || token == null) {
            callback.onError("未登录");
            return;
        }

        // 确保 token 格式正确
        if (!token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }

        // 从服务器获取最新的用户信息
        Call<UserResponse> call = authService.getUserInfo(token);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = response.body().getData();
                    if (user != null && user.getId() > 0) {
                        saveUserToPrefs(user);
                        callback.onSuccess(user);
                    } else {
                        callback.onError("获取用户信息失败：用户数据无效");
                    }
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "获取用户信息失败";
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "获取用户信息失败", t);
                // 如果网络请求失败，尝试从本地获取缓存的用户信息
                User cachedUser = getUserFromPrefs();
                if (cachedUser != null) {
                    callback.onSuccess(cachedUser);
                } else {
                    callback.onError("网络错误：" + t.getMessage());
                }
            }
        });
    }

    private User getUserFromPrefs() {
        int userId = sharedPreferences.getInt(KEY_USER_ID, -1);
        if (userId == -1) {
            return null;
        }

        User user = new User();
        user.setId(userId);
        user.setUserName(sharedPreferences.getString(KEY_USERNAME, ""));
        user.setName(sharedPreferences.getString(KEY_NAME, ""));
        user.setPhone(sharedPreferences.getString(KEY_PHONE, ""));
        user.setBio(sharedPreferences.getString(KEY_BIO, ""));
        user.setAvatarUrl(sharedPreferences.getString(KEY_AVATAR_URL, ""));

        Set<String> interests = sharedPreferences.getStringSet(KEY_INTERESTS, null);
        if (interests != null) {
            user.setInterests(interests.toArray(new String[0]));
        }

        return user;
    }

    private void saveUserToPrefs(User user) {
        if (user == null) {
            Log.e(TAG, "尝试保存空用户信息");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUserName());
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_BIO, user.getBio());
        editor.putString(KEY_AVATAR_URL, user.getAvatarUrl());

        if (user.getInterests() != null) {
            editor.putStringSet(KEY_INTERESTS, new HashSet<>(Arrays.asList(user.getInterests())));
        }

        editor.apply();
        Log.d(TAG, "用户信息已保存到本地");
    }

    public void updateUser(UserUpdateRequest request, UserCallback callback) {
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        if (!sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) || token == null) {
            callback.onError("未登录");
            return;
        }

        if (!token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }

        Call<UserResponse> call = authService.updateUser(token, request);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "用户信息更新成功，获取最新数据");
                    // 无论响应中是否包含用户数据，都重新获取一次最新数据
                    getCurrentUser(callback);
                } else {
                    callback.onError(response.body() != null ? response.body().getMessage() : "更新失败");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "更新用户信息失败", t);
                callback.onError("网络错误：" + t.getMessage());
            }
        });
    }

    public void uploadAvatar(Uri imageUri, UploadCallback callback) {
        try {
            Log.d(TAG, "开始处理头像上传: " + imageUri);
            // Convert Uri to File
            File file = uriToFile(imageUri);
            if (file == null) {
                Log.e(TAG, "图片文件处理失败");
                callback.onError("Failed to process image file");
                return;
            }
            Log.d(TAG, "图片文件处理成功: " + file.getAbsolutePath());

            // Create MultipartBody.Part from File
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            // Get token
            String token = sharedPreferences.getString(KEY_TOKEN, null);
            if (token == null) {
                Log.e(TAG, "用户未登录");
                callback.onError("Not logged in");
                return;
            }
            if (!token.startsWith("Bearer ")) {
                token = "Bearer " + token;
            }
            Log.d(TAG, "准备发送头像上传请求");

            // Make API call
            authService.uploadAvatar(token, body).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String filePath = response.body().getFilePath();
                        Log.d(TAG, "头像上传成功，服务器返回的文件路径: " + filePath);
                        callback.onSuccess(filePath);
                    } else {
                        Log.e(TAG, "头像上传失败，服务器响应: " + response.code() + 
                            (response.body() != null ? ", " + response.body().toString() : ""));
                        callback.onError("Failed to upload avatar");
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    Log.e(TAG, "头像上传网络请求失败: " + t.getMessage());
                    callback.onError("Network error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "头像上传过程发生异常: " + e.getMessage());
            callback.onError("Error: " + e.getMessage());
        }
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile("avatar", ".jpg", context.getCacheDir());
            copyInputStreamToFile(inputStream, tempFile);
            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "Error converting uri to file: " + e.getMessage());
            return null;
        }
    }

    private void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4 * 1024]; // 4KB buffer
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 只清除登录相关的数据，而不是所有数据
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putString(KEY_TOKEN, null);
        // 用户ID也需要清除，防止缓存冲突
        editor.putInt(KEY_USER_ID, -1);
        editor.apply();
        Log.d(TAG, "用户已登出，登录状态已清除");
    }

    public void getUserProfile(OnSuccessListener<User> successListener, OnErrorListener errorListener) {
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        if (token == null) {
            errorListener.onError(new Exception("未登录"));
            return;
        }

        String authHeader = "Bearer " + token;
        authService.getUserInfo(authHeader).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    successListener.onSuccess(response.body().getData());
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "获取用户信息失败";
                    errorListener.onError(new Exception(errorMsg));
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "获取用户信息失败", t);
                errorListener.onError(new Exception("网络错误，请检查网络连接", t));
            }
        });
    }

    public void updateUserProfile(UserUpdateRequest request, OnSuccessListener<User> successListener, OnErrorListener errorListener) {
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        if (token == null) {
            errorListener.onError(new Exception("未登录"));
            return;
        }

        String authHeader = "Bearer " + token;
        authService.updateUser(authHeader, request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    successListener.onSuccess(response.body().getData());
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "更新用户信息失败";
                    errorListener.onError(new Exception(errorMsg));
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "更新用户信息失败", t);
                errorListener.onError(new Exception("网络错误，请检查网络连接", t));
            }
        });
    }

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    public interface OnErrorListener {
        void onError(Exception e);
    }
} 