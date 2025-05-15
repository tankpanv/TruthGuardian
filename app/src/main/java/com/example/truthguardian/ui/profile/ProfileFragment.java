package com.example.truthguardian.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.activity.OnBackPressedCallback;

import com.bumptech.glide.Glide;
import com.example.truthguardian.R;
import com.example.truthguardian.databinding.FragmentProfileBinding;
import com.example.truthguardian.databinding.FragmentProfileDisplayBinding;
import com.example.truthguardian.model.User;
import com.example.truthguardian.repository.UserRepository;
import com.example.truthguardian.ui.auth.LoginActivity;
import com.example.truthguardian.ui.debunk.MyDebunkArticleListActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import static android.content.Context.MODE_PRIVATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "access_token";
    // 添加基础URL常量
    private static final String BASE_URL = "http://47.106.74.86:5005";
    
    // 预设的标签颜色组合 [背景色, 文字色]
    private static final int[][] TAG_COLORS = {
        {0xFFE8F5E9, 0xFF2E7D32}, // 浅绿背景，深绿文字
        {0xFFE3F2FD, 0xFF1976D2}, // 浅蓝背景，深蓝文字
        {0xFFF3E5F5, 0xFF7B1FA2}, // 浅紫背景，深紫文字
        {0xFFFFF3E0, 0xFFF57C00}, // 浅橙背景，深橙文字
        {0xFFFFEBEE, 0xFFC62828}, // 浅红背景，深红文字
        {0xFFE0F2F1, 0xFF00695C}, // 浅青背景，深青文字
        {0xFFF5F5F5, 0xFF424242}  // 浅灰背景，深灰文字
    };

    private FragmentProfileDisplayBinding displayBinding;
    private FragmentProfileBinding editBinding;
    private ProfileViewModel viewModel;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean isEditMode = false;
    private View currentView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置返回导航
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isEditMode) {
                    handleCancel();
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
        
        // 初始化图片选择器
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // 更新头像
                        updateAvatar(selectedImageUri.toString());
                    }
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            // 使用 ViewBinding 初始化视图
            displayBinding = FragmentProfileDisplayBinding.inflate(inflater, container, false);
            editBinding = FragmentProfileBinding.inflate(inflater, container, false);
            
            // 设置有返回按钮的标题栏
            setHasOptionsMenu(true);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("个人资料");
            
            // 初始化根视图容器
            ViewGroup rootContainer = new FrameLayout(requireContext());
            rootContainer.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ));
            
            // 添加显示模式的视图
            rootContainer.addView(displayBinding.getRoot());
            currentView = displayBinding.getRoot();

            // 初始化 ViewModel
            ProfileViewModelFactory factory = new ProfileViewModelFactory(requireActivity().getApplication());
            viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);

            // 设置观察者和点击事件
            setupObservers();
            setupClickListeners();

            // 加载用户资料
            viewModel.loadUserProfile();

            return rootContainer;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            Toast.makeText(requireContext(), "初始化界面失败", Toast.LENGTH_SHORT).show();
            return new View(requireContext());
        }
    }

    private void setupObservers() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // 有用户数据，显示已登录界面
                displayBinding.layoutNotLoggedIn.setVisibility(View.GONE);
                // ScrollView没有ID，需要通过索引获取
                ((ScrollView) displayBinding.getRoot().getChildAt(0)).setVisibility(View.VISIBLE);
                displayBinding.btnEditProfile.setVisibility(View.VISIBLE);
                displayBinding.btnLogout.setVisibility(View.VISIBLE);
                
                if (isEditMode) {
                    updateEditUI(user);
                } else {
                    updateDisplayUI(user);
                }
                
                // 打印用户信息，包括头像URL
                Log.d(TAG, "用户信息更新: " + 
                    "\nName: " + user.getName() +
                    "\nUsername: " + user.getUserName() +
                    "\nAvatar URL: " + user.getAvatarUrl());
            } else {
                // 无用户数据，显示未登录界面
                displayBinding.layoutNotLoggedIn.setVisibility(View.VISIBLE);
                // ScrollView没有ID，需要通过索引获取
                ((ScrollView) displayBinding.getRoot().getChildAt(0)).setVisibility(View.GONE);
                displayBinding.btnEditProfile.setVisibility(View.GONE);
                displayBinding.btnLogout.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                if (error.equals("未登录")) {
                    // 未登录错误，显示未登录界面
                    displayBinding.layoutNotLoggedIn.setVisibility(View.VISIBLE);
                    // ScrollView没有ID，需要通过索引获取
                    ((ScrollView) displayBinding.getRoot().getChildAt(0)).setVisibility(View.GONE);
                    displayBinding.btnEditProfile.setVisibility(View.GONE);
                    displayBinding.btnLogout.setVisibility(View.GONE);
                } else {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    // 如果是头像更新成功的消息，重新加载用户资料
                    if (error.contains("头像更新成功")) {
                        viewModel.loadUserProfile();
                    }
                }
            }
        });
    }

    private void setupClickListeners() {
        // 显示模式下的点击事件
        displayBinding.btnEditProfile.setOnClickListener(v -> switchToEditMode());
        displayBinding.btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
        
        // 未登录状态下的登录按钮
        displayBinding.btnLogin.setOnClickListener(v -> navigateToLogin());
        
        // 设置返回按钮点击事件
        View backButton = displayBinding.getRoot().findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> requireActivity().onBackPressed());
            Log.d(TAG, "Display mode back button setup successfully");
        }

        // 编辑模式下的点击事件
        editBinding.btnChangeAvatar.setOnClickListener(v -> openImagePicker());
        editBinding.btnSave.setOnClickListener(v -> saveProfile());
        editBinding.btnAddInterest.setOnClickListener(v -> showAddInterestDialog());
    }

    private void switchToEditMode() {
        try {
            isEditMode = true;
            
            // 获取父容器
            ViewGroup rootContainer = (ViewGroup) currentView.getParent();
            if (rootContainer == null) {
                Log.e(TAG, "Root container is null");
                return;
            }

            // 准备编辑模式视图
            User user = viewModel.getUserProfile().getValue();
            if (user != null) {
                updateEditUI(user);
            }
            
            // 更新标题栏
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("编辑资料");

            // 安全切换视图
            rootContainer.removeAllViews();
            rootContainer.addView(editBinding.getRoot());
            currentView = editBinding.getRoot();
            
            Log.d(TAG, "Successfully switched to edit mode");
        } catch (Exception e) {
            Log.e(TAG, "Error switching to edit mode", e);
            Toast.makeText(requireContext(), "切换编辑模式失败", Toast.LENGTH_SHORT).show();
            // 恢复到显示模式
            isEditMode = false;
        }
    }

    private void switchToDisplayMode() {
        try {
            isEditMode = false;
            
            // 获取父容器
            ViewGroup rootContainer = (ViewGroup) currentView.getParent();
            if (rootContainer == null) {
                Log.e(TAG, "Root container is null");
                return;
            }
            
            // 更新标题栏
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("个人资料");

            // 安全切换视图
            rootContainer.removeAllViews();
            rootContainer.addView(displayBinding.getRoot());
            currentView = displayBinding.getRoot();
            
            Log.d(TAG, "Successfully switched to display mode");
        } catch (Exception e) {
            Log.e(TAG, "Error switching to display mode", e);
            Toast.makeText(requireContext(), "切换显示模式失败", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFullAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return null;
        }
        // 如果已经是完整URL，直接返回
        if (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://")) {
            return avatarUrl;
        }
        // 确保路径以/开头
        if (!avatarUrl.startsWith("/")) {
            avatarUrl = "/" + avatarUrl;
        }
        return BASE_URL + avatarUrl;
    }

    private void updateDisplayUI(User user) {
        try {
            // 更新头像
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                String fullAvatarUrl = getFullAvatarUrl(user.getAvatarUrl());
                Log.d(TAG, "加载头像URL: " + fullAvatarUrl);
                Glide.with(this)
                        .load(fullAvatarUrl)
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .circleCrop()
                        .into(displayBinding.ivAvatar);
            } else {
                // 如果没有头像URL，直接使用默认头像
                displayBinding.ivAvatar.setImageResource(R.drawable.avatar);
            }
            
            // 更新基本信息
            displayBinding.tvName.setText(user.getName() != null ? user.getName() : "");
            displayBinding.tvUsername.setText(user.getUserName() != null ? user.getUserName() : "");
            displayBinding.tvPhone.setText(user.getPhone() != null ? user.getPhone() : "未设置");
            displayBinding.tvBio.setText(user.getBio() != null && !user.getBio().isEmpty() ? user.getBio() : "这个人很懒，什么都没留下");
            
            // 更新兴趣标签
            displayBinding.chipGroupInterests.removeAllViews();
            String[] interestsArray = user.getInterests();
            List<String> interests = interestsArray != null ? Arrays.asList(interestsArray) : new ArrayList<>();
            if (interests != null && !interests.isEmpty()) {
                for (String interest : interests) {
                    addDisplayInterestChip(interest);
                }
                displayBinding.chipGroupInterests.setVisibility(View.VISIBLE);
            } else {
                displayBinding.chipGroupInterests.setVisibility(View.GONE);
            }
            
            // 添加"我的辟谣"卡片点击事件
            displayBinding.cardMyDebunks.setOnClickListener(v -> navigateToMyDebunkList());
            
            Log.d(TAG, "Display UI updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating display UI", e);
            Toast.makeText(requireContext(), "更新界面失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEditUI(User user) {
        if (user == null) return;

        try {
            // 设置基本信息
            editBinding.etName.setText(user.getName());
            editBinding.etUsername.setText(user.getUserName());
            editBinding.etPhone.setText(user.getPhone());
            editBinding.etBio.setText(user.getBio());

            // 显示头像
            editBinding.ivAvatar.setVisibility(View.VISIBLE);
            editBinding.btnChangeAvatar.setVisibility(View.VISIBLE);
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                String fullAvatarUrl = getFullAvatarUrl(user.getAvatarUrl());
                Log.d(TAG, "加载编辑模式头像URL: " + fullAvatarUrl);
                // 使用用户自己的头像
                Glide.with(this)
                    .load(fullAvatarUrl)
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .circleCrop()
                    .into(editBinding.ivAvatar);
            } else {
                // 使用默认头像
                editBinding.ivAvatar.setImageResource(R.drawable.avatar);
            }

            // 启用兴趣标签编辑
            editBinding.chipGroupInterests.removeAllViews();
            editBinding.chipGroupInterests.setVisibility(View.VISIBLE);
            editBinding.btnAddInterest.setVisibility(View.VISIBLE);
            
            // 添加已有的兴趣标签
            String[] interestsArray = user.getInterests();
            if (interestsArray != null && interestsArray.length > 0) {
                for (String interest : interestsArray) {
                    if (interest != null && !interest.isEmpty()) {
                        addEditInterestChip(interest);
                    }
                }
            }

            // 设置添加兴趣标签按钮点击事件
            editBinding.btnAddInterest.setOnClickListener(v -> showAddInterestDialog());

            // 打印日志以验证数据
            Log.d(TAG, "Updating Edit UI with user data: " + 
                "\nName: " + user.getName() +
                "\nUsername: " + user.getUserName() +
                "\nPhone: " + user.getPhone() +
                "\nBio: " + user.getBio());
        } catch (Exception e) {
            Log.e(TAG, "Error updating edit UI", e);
            Toast.makeText(requireContext(), "更新编辑界面时出错", Toast.LENGTH_SHORT).show();
        }
    }

    private void addDisplayInterestChip(String interest) {
        Chip chip = new Chip(requireContext());
        chip.setText(interest);
        chip.setClickable(false);
        
        // 设置标签样式 - 为每个标签随机分配预设的颜色组合
        int colorIndex = Math.abs(interest.hashCode()) % TAG_COLORS.length;
        int backgroundColor = TAG_COLORS[colorIndex][0];
        int textColor = TAG_COLORS[colorIndex][1];
        
        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(backgroundColor));
        chip.setTextColor(textColor);
        chip.setEnsureMinTouchTargetSize(false);
        chip.setChipStrokeWidth(0);
        
        displayBinding.chipGroupInterests.addView(chip);
    }

    private void addEditInterestChip(String interest) {
        Chip chip = new Chip(requireContext());
        chip.setText(interest);
        chip.setCloseIconVisible(true);
        
        // 设置标签样式 - 为每个标签随机分配预设的颜色组合
        int colorIndex = Math.abs(interest.hashCode()) % TAG_COLORS.length;
        int backgroundColor = TAG_COLORS[colorIndex][0];
        int textColor = TAG_COLORS[colorIndex][1];
        
        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(backgroundColor));
        chip.setTextColor(textColor);
        chip.setCloseIconTint(android.content.res.ColorStateList.valueOf(textColor));
        chip.setEnsureMinTouchTargetSize(false);
        chip.setChipStrokeWidth(0);
        
        chip.setOnCloseIconClickListener(v -> {
            editBinding.chipGroupInterests.removeView(chip);
        });
        
        editBinding.chipGroupInterests.addView(chip);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void updateAvatar(String imageUriString) {
        try {
            Log.d(TAG, "开始更新头像: " + imageUriString);
            Uri imageUri = Uri.parse(imageUriString);
            
            // 显示加载提示
            Toast.makeText(requireContext(), "正在上传头像...", Toast.LENGTH_SHORT).show();
            
            // 调用 ViewModel 上传头像
            viewModel.updateAvatar(imageUri);
            
            // 在编辑界面显示选择的头像预览
            if (imageUri != null) {
                Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .circleCrop()
                    .into(editBinding.ivAvatar);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "更新头像失败", e);
            Toast.makeText(requireContext(), "更新头像失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        String name = editBinding.etName.getText().toString().trim();
        String username = editBinding.etUsername.getText().toString().trim();
        String phone = editBinding.etPhone.getText().toString().trim();
        String bio = editBinding.etBio.getText().toString().trim();

        // 验证输入
        if (name.isEmpty() || username.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "请填写必要的信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 收集兴趣标签
        List<String> interests = new ArrayList<>();
        for (int i = 0; i < editBinding.chipGroupInterests.getChildCount(); i++) {
            Chip chip = (Chip) editBinding.chipGroupInterests.getChildAt(i);
            interests.add(chip.getText().toString());
        }

        // 显示加载提示
        Toast.makeText(requireContext(), "正在保存...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "正在保存用户资料...");

        // 更新用户资料（包括兴趣标签）
        viewModel.updateProfile(name, username, phone, bio, interests, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                // 在UI线程中处理结果
                Log.d(TAG, "保存用户资料成功，在UI线程更新: " + user);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show();
                    switchToDisplayMode();
                    // 确保UI更新
                    updateDisplayUI(user);
                });
            }

            @Override
            public void onError(String error) {
                // 在UI线程中处理错误
                Log.e(TAG, "保存用户资料失败: " + error);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "保存失败: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLogoutConfirmDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("退出登录")
            .setMessage("确定要退出登录吗？")
            .setPositiveButton("确定", (dialog, which) -> logout())
            .setNegativeButton("取消", null)
            .show();
    }

    private void showAddInterestDialog() {
        // 创建对话框布局
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_interest, null);
        EditText etInterest = dialogView.findViewById(R.id.et_interest);
        
        // 创建并显示对话框
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("添加兴趣标签")
            .setView(dialogView)
            .setPositiveButton("添加", (dialog, which) -> {
                String interest = etInterest.getText().toString().trim();
                if (!interest.isEmpty()) {
                    addEditInterestChip(interest);
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void logout() {
        // 只清除登录相关的数据，而不是所有数据
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);  // 将登录状态设为false
        editor.putString(KEY_TOKEN, null);          // 清除token
        editor.apply();
        
        Log.d(TAG, "用户已登出，登录状态已清除");

        // 跳转到登录页面
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void handleCancel() {
        try {
            // 切换回显示模式
            switchToDisplayMode();
            // 重新加载用户资料以恢复原始数据
            viewModel.loadUserProfile();
            Log.d(TAG, "Successfully cancelled edit mode");
        } catch (Exception e) {
            Log.e(TAG, "Error handling cancel", e);
            Toast.makeText(requireContext(), "取消编辑失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        displayBinding = null;
        editBinding = null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 处理返回按钮点击
            if (isEditMode) {
                handleCancel();
                return true;
            } else {
                requireActivity().onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToLogin() {
        // 跳转到登录页面，但不清除任何数据
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
    }

    private void navigateToMyDebunkList() {
        try {
            Intent intent = new Intent(requireContext(), MyDebunkArticleListActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to my debunk list", e);
            Toast.makeText(requireContext(), "无法打开我的辟谣列表", Toast.LENGTH_SHORT).show();
        }
    }
} 