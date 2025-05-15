package com.example.truthguardian.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.truthguardian.R;
import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.api.AuthService;
import com.example.truthguardian.model.RegisterRequest;
import com.example.truthguardian.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText etUsername;
    private EditText etName;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化API服务
        authService = ApiClient.getClient().create(AuthService.class);

        // 初始化视图
        etUsername = findViewById(R.id.etUsername);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // 设置注册按钮点击事件
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // 设置返回登录页面点击事件
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一个活动（登录页面）
            }
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("请输入用户名");
            return;
        }

        if (TextUtils.isEmpty(name)) {
            etName.setError("请输入称呼");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("请输入手机号码");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("请确认密码");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("两次输入的密码不一致");
            return;
        }

        // 创建注册请求
        RegisterRequest registerRequest = new RegisterRequest(
                username,
                password,
                name,
                phone
        );

        // 调用注册API
        Call<RegisterResponse> call = authService.register(registerRequest);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 注册成功
                    Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                    
                    // 返回登录页面
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // 注册失败
                    String errorMsg = "注册失败，请重试";
                    if (response.code() == 409) {
                        errorMsg = "用户名已存在";
                    }
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Registration failed: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "注册失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Registration request failed", t);
            }
        });
    }
} 