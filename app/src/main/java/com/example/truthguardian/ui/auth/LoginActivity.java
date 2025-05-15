package com.example.truthguardian.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.truthguardian.MainActivity;
import com.example.truthguardian.R;
import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.api.AuthService;
import com.example.truthguardian.model.LoginRequest;
import com.example.truthguardian.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化API服务
        authService = ApiClient.getClient().create(AuthService.class);

        // 初始化视图
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // 设置登录按钮点击事件
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // 设置注册文本点击事件
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String username = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(username)) {
            etEmail.setError("请输入用户名");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            return;
        }

        // 创建登录请求
        LoginRequest loginRequest = new LoginRequest(username, password);

        // 调用登录API
        Call<LoginResponse> call = authService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body());
                } else {
                    // 登录失败
                    Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Login failed: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "登录失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Login request failed", t);
            }
        });
    }

    private void handleLoginSuccess(LoginResponse response) {
        try {
            if (response != null && response.isSuccess()) {
                String token = response.getAccessToken();
                
                // 移除Bearer前缀，确保保存的是原始token
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                
                // 保存登录状态和token（不带Bearer前缀）
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean(KEY_IS_LOGGED_IN, true);
                editor.putString(KEY_TOKEN, token);
                editor.apply();

                Log.d(TAG, "Token saved successfully: " + token.substring(0, Math.min(token.length(), 20)) + "...");

                // 显示成功提示
                String successMessage = "登录成功";
                Toast.makeText(LoginActivity.this, successMessage, Toast.LENGTH_SHORT).show();

                // 跳转到主页面
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                // 登录失败
                Toast.makeText(LoginActivity.this, "登录失败，请重试", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Login failed: response=" + (response != null ? "not null" : "null"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in handleLoginSuccess", e);
            Toast.makeText(LoginActivity.this, "登录过程中出现错误", Toast.LENGTH_SHORT).show();
        }
    }

    // 检查用户是否已登录
    public static boolean isLoggedIn(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // 获取保存的访问令牌
    public static String getAccessToken(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }
} 