package com.example.truthguardian;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.api.AuthService;
import com.example.truthguardian.model.User;
import com.example.truthguardian.model.UserResponse;
import com.example.truthguardian.ui.ai.AIChatActivity;
import com.example.truthguardian.ui.auth.LoginActivity;
import com.example.truthguardian.ui.message.Message;
import com.example.truthguardian.ui.message.MessageToastManager;
import com.example.truthguardian.websocket.WebSocketManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.truthguardian.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NAME = "name";
    private static final String KEY_AVATAR_URL = "avatar_url";
    
    private AuthService authService;
    private FloatingActionButton fabAIChat;
    private MessageToastManager messageToastManager;
    private WebSocketManager webSocketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "MainActivity onCreate started");

            // 初始化视图绑定
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            if (binding == null) {
                Log.e(TAG, "View binding failed to initialize");
                throw new IllegalStateException("View binding is null");
            }
            setContentView(binding.getRoot());
            
            // 获取保存的token（如果有的话）
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String token = sharedPreferences.getString(KEY_TOKEN, null);
            
            // 无论是否登录，直接初始化各个组件
            initializeComponents(token);
            
            Log.d(TAG, "MainActivity onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            // 出错时仍然尝试初始化组件
            try {
                initializeComponents(null);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to initialize components after error", ex);
            }
        }
    }

    private void initializeComponents(String token) {
        try {
            Log.d(TAG, "Initializing components");
            
            // 设置Toolbar
            Toolbar toolbar = binding.toolbar;
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }
            
            // 初始化API服务
            authService = ApiClient.getClient().create(AuthService.class);
            
            // 设置底部导航
            setupNavigation();
            
            // 设置AI浮动按钮
            setupAIButton();
            
            // 初始化消息提示管理器
            messageToastManager = new MessageToastManager(this);

            // 只有在token不为null时才初始化需要token的组件
            if (token != null && !token.isEmpty()) {
                // 初始化WebSocket连接
                initializeWebSocket(token);
                
                // 获取最新的用户信息
                fetchUserInfo(token);
            } else {
                Log.d(TAG, "Token is null or empty, skipping token-dependent initializations");
            }
            
            Log.d(TAG, "Components initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing components", e);
            throw e;
        }
    }

    private void setupNavigation() {
        try {
            BottomNavigationView navView = binding.navView;
            if (navView == null) {
                Log.e(TAG, "Navigation view is null");
                return;
            }

            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_visualization,
                    R.id.navigation_message, R.id.navigation_profile)
                    .build();

            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation", e);
            throw e;
        }
    }

    private void setupAIButton() {
        try {
            fabAIChat = findViewById(R.id.fabAIChat);
            if (fabAIChat == null) {
                Log.e(TAG, "AI chat button is null");
                return;
            }

            fabAIChat.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, AIChatActivity.class);
                startActivity(intent);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up AI button", e);
            throw e;
        }
    }

    /**
     * 仅在用户明确选择登出时调用此方法
     */
    private void redirectToLogin() {
        try {
            Log.d(TAG, "Redirecting to login page (explicit logout)");
            // 清除所有保存的数据
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            // 跳转到登录页面
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error redirecting to login", e);
            // 如果连跳转都失败了，直接结束activity
            finish();
        }
    }

    private void fetchUserInfo(String token) {
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is null or empty in fetchUserInfo");
            return;
        }

        try {
            // 确保token格式正确
            String formattedToken = token;
            if (!formattedToken.startsWith("Bearer ")) {
                formattedToken = "Bearer " + formattedToken;
            }
            
            Log.d(TAG, "Fetching user info with token format: " + (formattedToken.startsWith("Bearer ") ? "Bearer + token" : "token without Bearer"));

            Call<UserResponse> call = authService.getUserInfo(formattedToken);
            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    try {
                        Log.d(TAG, "User info response received - code: " + response.code());
                        
                        if (response.isSuccessful() && response.body() != null) {
                            UserResponse userResponse = response.body();
                            if (userResponse.getData() != null && userResponse.getData().getId() != null) {
                                // 用户已登录，保存用户信息
                                saveUserInfo(userResponse);
                            } else {
                                Log.e(TAG, "Invalid user data received");
                            }
                        } else {
                            Log.e(TAG, "Failed to fetch user info: " + response.code());
                            if (response.errorBody() != null) {
                                Log.e(TAG, "Error body: " + response.errorBody().string());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing user info response", e);
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    Log.e(TAG, "Network error when fetching user info", t);
                    // 网络错误时不立即登出，让用户可以继续使用缓存的数据
                    Toast.makeText(MainActivity.this, "网络连接失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in fetchUserInfo", e);
        }
    }

    private void saveUserInfo(UserResponse userResponse) {
        try {
            if (userResponse == null || userResponse.getData() == null || userResponse.getData().getId() == null) {
                Log.e(TAG, "Invalid user data received");
                Toast.makeText(this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_USER_ID, String.valueOf(userResponse.getData().getId()));
            editor.putString(KEY_USERNAME, userResponse.getData().getUserName());
            editor.putString(KEY_NAME, userResponse.getData().getName());
            editor.putString("phone", userResponse.getData().getPhone());
            editor.putString("bio", userResponse.getData().getBio());
            editor.putString(KEY_AVATAR_URL, userResponse.getData().getAvatarUrl());
            editor.apply();

            Log.d(TAG, "User info saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving user info: " + e.getMessage());
            Toast.makeText(this, "保存用户信息时出错", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 退出登录
     */
    private void logout() {
        try {
            Log.d(TAG, "Logging out...");
            
            // 清除SharedPreferences中的登录信息
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            // 断开WebSocket连接
            if (webSocketManager != null) {
                try {
                    webSocketManager.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Error disconnecting WebSocket", e);
                }
                webSocketManager = null;
            }

            // 清除消息管理器
            if (messageToastManager != null) {
                messageToastManager = null;
            }

            // 跳转到登录页面
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            
            Log.d(TAG, "Logout completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            // 即使发生错误，也要尝试跳转到登录页面
            try {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } catch (Exception e2) {
                Log.e(TAG, "Failed to start LoginActivity", e2);
            }
        }
    }

    private void initializeWebSocket(String token) {
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Cannot initialize WebSocket: token is null or empty");
            return;
        }

        // 确保token格式正确
        String formattedToken = token;
        if (!formattedToken.startsWith("Bearer ")) {
            formattedToken = "Bearer " + formattedToken;
        }
        Log.d(TAG, "Initializing WebSocket with token format: " + (formattedToken.startsWith("Bearer ") ? "Bearer + token" : "token without Bearer"));
        
        webSocketManager = WebSocketManager.getInstance(formattedToken, new WebSocketManager.WebSocketListener() {
            @Override
            public void onConnected(int userId) {
                Log.d(TAG, "WebSocket connected, userId: " + userId);
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "WebSocket disconnected");
            }

            @Override
            public void onNewMessage(WebSocketManager.Message message) {
                Log.d(TAG, "New message received: " + message.getTitle());
                runOnUiThread(() -> {
                    messageToastManager.showMessage(message);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "WebSocket error: " + error);
            }
        });

        webSocketManager.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }

    public MessageToastManager getMessageToastManager() {
        return messageToastManager;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}