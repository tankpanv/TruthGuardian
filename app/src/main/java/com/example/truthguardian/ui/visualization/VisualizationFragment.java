package com.example.truthguardian.ui.visualization;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.truthguardian.R;
import com.example.truthguardian.api.AIService;
import com.example.truthguardian.api.ApiClient;
import com.example.truthguardian.model.VisualizationResponse;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisualizationFragment extends Fragment {
    private PieChart topicDistributionChart;
    private BarChart credibilityScoresChart;
    private LineChart sentimentTimelineChart;
    private WebView spreadPathWebView;
    private WebView geoDistributionWebView;
    private AIService aiService;
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "access_token";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_visualization, container, false);
        
        initializeCharts(root);
        loadVisualizationData();
        
        return root;
    }

    private void initializeCharts(View root) {
        // Initialize pie chart
        topicDistributionChart = root.findViewById(R.id.topic_distribution_chart);
        setupPieChart(topicDistributionChart);

        // Initialize bar chart
        credibilityScoresChart = root.findViewById(R.id.credibility_scores_chart);
        setupBarChart(credibilityScoresChart);

        // Initialize line chart
        sentimentTimelineChart = root.findViewById(R.id.sentiment_timeline_chart);
        setupLineChart(sentimentTimelineChart);

        // Initialize WebViews
        spreadPathWebView = root.findViewById(R.id.spread_path_web_view);
        geoDistributionWebView = root.findViewById(R.id.geo_distribution_web_view);
        
        // 配置WebView
        setupWebView(spreadPathWebView);
        setupWebView(geoDistributionWebView);

        aiService = ApiClient.getClient().create(AIService.class);
    }

    private void setupPieChart(PieChart chart) {
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleRadius(61f);
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(12f);
        chart.getLegend().setEnabled(true);
    }

    private void setupBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setFitBars(true);
        chart.setPinchZoom(false);
        chart.setDrawValueAboveBar(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
    }

    private void setupLineChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true);
    }

    private void setupWebView(WebView webView) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        
        // 启用调试
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        
        // 设置WebChromeClient来处理JavaScript控制台消息
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("WebView", String.format("JavaScript: %s -- From line %s of %s",
                    consoleMessage.message(),
                    consoleMessage.lineNumber(),
                    consoleMessage.sourceId()));
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d("VisualizationFragment", "WebView开始加载: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("VisualizationFragment", "WebView加载完成: " + url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e("VisualizationFragment", "WebView加载错误: " + description + " URL: " + failingUrl);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.e("VisualizationFragment", "SSL错误: " + error.toString());
                handler.proceed(); // 处理SSL证书错误
            }
        });
    }

    private void loadVisualizationData() {
        String token = getStoredToken();
        if (token == null) {
            Log.e("VisualizationFragment", "Token is null, cannot load visualization data");
            showError("未登录或登录已过期，请重新登录");
            return;
        }

        showLoading(true);
        Log.d("VisualizationFragment", "Loading visualization data with token: " + token.substring(0, Math.min(token.length(), 20)) + "...");

        Call<VisualizationResponse> call = aiService.getVisualizationData(token);
        call.enqueue(new Callback<VisualizationResponse>() {
            @Override
            public void onResponse(Call<VisualizationResponse> call, Response<VisualizationResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("VisualizationFragment", "Successfully received visualization data");
                    updateCharts(response.body());
                    updateWebViews(response.body());
                } else {
                    Log.e("VisualizationFragment", "Failed to get visualization data: " + response.code() + " " + response.message());
                    showError("获取数据失败：" + response.code());
                }
            }

            @Override
            public void onFailure(Call<VisualizationResponse> call, Throwable t) {
                showLoading(false);
                Log.e("VisualizationFragment", "Error loading visualization data", t);
                showError("加载数据失败：" + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            if (show) {
                // TODO: 显示加载进度条
            } else {
                // TODO: 隐藏加载进度条
            }
        });
    }

    private void showError(String message) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        });
    }

    private void updateCharts(final VisualizationResponse data) {
        // 在后台线程处理数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 更新主题分布饼图
                final ArrayList<PieEntry> topicEntries = new ArrayList<>();
                List<VisualizationResponse.TopicDistribution> topics = data.getData().getTextAnalysis().getTopicDistribution();
                for (VisualizationResponse.TopicDistribution topic : topics) {
                    topicEntries.add(new PieEntry(topic.getPercentage(), topic.getTopic()));
                }

                // 更新可信度得分柱状图
                final ArrayList<BarEntry> credibilityEntries = new ArrayList<>();
                List<VisualizationResponse.CredibilityScore> scores = data.getData().getTextAnalysis().getCredibilityScores();
                for (int i = 0; i < scores.size(); i++) {
                    VisualizationResponse.CredibilityScore score = scores.get(i);
                    credibilityEntries.add(new BarEntry(i, score.getCount()));
                }

                // 更新情感分析时间线
                final ArrayList<Entry> sentimentEntries = new ArrayList<>();
                VisualizationResponse.Timeline timeline = data.getData().getSentimentAnalysis().getTimeline();
                List<Integer> positive = timeline.getPositive();
                List<Integer> negative = timeline.getNegative();
                List<Integer> neutral = timeline.getNeutral();
                
                for (int i = 0; i < positive.size(); i++) {
                        // 计算综合情感得分：(正面 - 负面) / (正面 + 中性 + 负面)
                    float positiveCount = positive.get(i);
                    float negativeCount = negative.get(i);
                    float neutralCount = neutral.get(i);
                        float total = positiveCount + neutralCount + negativeCount;
                        float sentimentScore = total > 0 ? (positiveCount - negativeCount) / total : 0;
                    sentimentEntries.add(new Entry(i, sentimentScore));
                    }

                    // 在主线程更新UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            // 更新饼图
                            PieDataSet topicDataSet = new PieDataSet(topicEntries, "主题分布");
                            topicDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                            PieData topicData = new PieData(topicDataSet);
                            topicData.setValueTextSize(12f);
                            topicDistributionChart.setData(topicData);
                                    topicDistributionChart.invalidate();

                            // 更新柱状图
                            BarDataSet credibilityDataSet = new BarDataSet(credibilityEntries, "可信度得分");
                            credibilityDataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
                            BarData credibilityData = new BarData(credibilityDataSet);
                            credibilityData.setBarWidth(0.9f);
                            credibilityScoresChart.setData(credibilityData);
                                    credibilityScoresChart.invalidate();

                            // 更新折线图
                            LineDataSet sentimentDataSet = new LineDataSet(sentimentEntries, "情感趋势");
                            sentimentDataSet.setColor(ColorTemplate.MATERIAL_COLORS[2]);
                            sentimentDataSet.setCircleColor(ColorTemplate.MATERIAL_COLORS[2]);
                            sentimentDataSet.setLineWidth(2f);
                            sentimentDataSet.setCircleRadius(4f);
                            sentimentDataSet.setDrawCircleHole(false);
                            LineData sentimentData = new LineData(sentimentDataSet);
                            sentimentTimelineChart.setData(sentimentData);
                                    sentimentTimelineChart.invalidate();

                            // 更新WebView内容
                            updateWebViews(data);
                        }
                    });
                }
            }
        }).start();
    }

    private void updateWebViews(VisualizationResponse data) {
        if (data == null || data.getData() == null) {
            Log.e("VisualizationFragment", "数据为空");
            return;
        }

        // 更新传播路径图
        VisualizationResponse.SpreadPath spreadPath = data.getData().getSpreadPath();
        if (spreadPath != null) {
            String spreadPathHtml = generateSpreadPathHtml(spreadPath);
            Log.d("VisualizationFragment", "传播路径HTML: " + spreadPathHtml);
            spreadPathWebView.loadDataWithBaseURL("file:///android_asset/", spreadPathHtml, "text/html", "UTF-8", null);
        } else {
            Log.e("VisualizationFragment", "传播路径数据为空");
        }

        // 更新地理分布图
        VisualizationResponse.GeoDistribution geoDistribution = data.getData().getGeoDistribution();
        if (geoDistribution != null) {
            Log.d("VisualizationFragment", "地理分布数据: regions=" + geoDistribution.getRegions().size() + 
                                         ", heatmap=" + (geoDistribution.getHeatmapData() != null ? geoDistribution.getHeatmapData().size() : 0));
            
            String geoDistributionHtml = generateGeoDistributionHtml(geoDistribution);
            Log.d("VisualizationFragment", "地理分布HTML: " + geoDistributionHtml);
            
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                    geoDistributionWebView.loadDataWithBaseURL("file:///android_asset/", geoDistributionHtml, "text/html", "UTF-8", null);
                }
            });
        } else {
            Log.e("VisualizationFragment", "地理分布数据为空");
        }
    }

    private String generateSpreadPathHtml(VisualizationResponse.SpreadPath spreadPath) {
        if (spreadPath.getNodes() == null || spreadPath.getLinks() == null) {
            Log.e("VisualizationFragment", "传播路径节点或链接为空");
            return "";
        }

        StringBuilder nodesJson = new StringBuilder("[");
        boolean firstNode = true;
        for (VisualizationResponse.Node node : spreadPath.getNodes()) {
            if (!firstNode) {
                nodesJson.append(",");
            }
            firstNode = false;
            nodesJson.append("{\n")
                    .append("    \"id\": \"").append(node.getId()).append("\",\n")
                    .append("    \"name\": \"").append(node.getName()).append("\",\n")
                    .append("    \"type\": \"").append(node.getType()).append("\",\n")
                    .append("    \"symbolSize\": 50,\n")  // 添加节点大小
                    .append("    \"category\": \"").append(node.getType()).append("\"\n")  // 添加节点类型分类
                    .append("}");
        }
        nodesJson.append("]");

        StringBuilder linksJson = new StringBuilder("[");
        boolean firstLink = true;
        for (VisualizationResponse.Link link : spreadPath.getLinks()) {
            if (!firstLink) {
                linksJson.append(",");
            }
            firstLink = false;
            linksJson.append("{\n")
                    .append("    \"source\": \"").append(link.getSource()).append("\",\n")
                    .append("    \"target\": \"").append(link.getTarget()).append("\",\n")
                    .append("    \"value\": ").append(link.getValue()).append("\n")
                    .append("}");
        }
        linksJson.append("]");

        Log.d("VisualizationFragment", "Nodes: " + nodesJson.toString());
        Log.d("VisualizationFragment", "Links: " + linksJson.toString());

        String html = "<!DOCTYPE html><html><head>\n" +
               "<meta charset='UTF-8'>\n" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
               "<script src='https://cdn.jsdelivr.net/npm/echarts@5/dist/echarts.min.js'></script>\n" +
               "</head><body>\n" +
               "<div id='main' style='width: 100%; height: 400px;'></div>\n" +
               "<script>\n" +
               "var nodes = " + nodesJson.toString() + ";\n" +
               "var links = " + linksJson.toString() + ";\n" +
               "var chart = echarts.init(document.getElementById('main'));\n" +
               "var option = {\n" +
               "    title: { \n" +
               "        text: '谣言传播路径',\n" +
               "        left: 'center',\n" +
               "        top: 10,\n" +
               "        textStyle: {\n" +
               "            fontSize: 16\n" +
               "        }\n" +
               "    },\n" +
               "    tooltip: { \n" +
               "        trigger: 'item',\n" +
               "        formatter: function(params) {\n" +
               "            if (params.dataType === 'node') {\n" +
               "                return '节点: ' + params.data.name + '<br/>类型: ' + params.data.type;\n" +
               "            } else {\n" +
               "                return '传播强度: ' + params.data.value;\n" +
               "            }\n" +
               "        }\n" +
               "    },\n" +
               "    legend: {\n" +
               "        data: ['origin', 'platform', 'group'],\n" +
               "        top: 40,\n" +
               "        textStyle: {\n" +
               "            fontSize: 12\n" +
               "        },\n" +
               "        formatter: function(name) {\n" +
               "            var translations = {\n" +
               "                'origin': '源头',\n" +
               "                'platform': '平台',\n" +
               "                'group': '群组'\n" +
               "            };\n" +
               "            return translations[name] || name;\n" +
               "        }\n" +
               "    },\n" +
               "    animationDurationUpdate: 1500,\n" +
               "    animationEasingUpdate: 'quinticInOut',\n" +
               "    series: [{\n" +
               "        type: 'graph',\n" +
               "        layout: 'force',\n" +
               "        force: { \n" +
               "            repulsion: 1500,\n" +
               "            edgeLength: 120,\n" +
               "            gravity: 0.2,\n" +
               "            layoutAnimation: true\n" +
               "        },\n" +
               "        roam: true,\n" +
               "        draggable: true,\n" +
               "        label: { \n" +
               "            show: true,\n" +
               "            position: 'right',\n" +
               "            formatter: '{b}',\n" +
               "            fontSize: 12,\n" +
               "            backgroundColor: 'rgba(255, 255, 255, 0.8)',\n" +
               "            padding: [4, 8],\n" +
               "            borderRadius: 4\n" +
               "        },\n" +
               "        edgeSymbol: ['circle', 'arrow'],\n" +
               "        edgeSymbolSize: [4, 10],\n" +
               "        edgeLabel: {\n" +
               "            show: true,\n" +
               "            formatter: function(params) {\n" +
               "                return params.data.value;\n" +
               "            },\n" +
               "            fontSize: 12,\n" +
               "            backgroundColor: 'rgba(255, 255, 255, 0.8)',\n" +
               "            padding: [2, 4],\n" +
               "            borderRadius: 2\n" +
               "        },\n" +
               "        data: nodes,\n" +
               "        links: links,\n" +
               "        lineStyle: {\n" +
               "            opacity: 0.9,\n" +
               "            width: 2,\n" +
               "            curveness: 0.2\n" +
               "        },\n" +
               "        categories: [\n" +
               "            { \n" +
               "                name: 'origin',\n" +
               "                itemStyle: { color: '#c23531' },\n" +
               "                label: { color: '#c23531' }\n" +
               "            },\n" +
               "            { \n" +
               "                name: 'platform',\n" +
               "                itemStyle: { color: '#2f4554' },\n" +
               "                label: { color: '#2f4554' }\n" +
               "            },\n" +
               "            { \n" +
               "                name: 'group',\n" +
               "                itemStyle: { color: '#61a0a8' },\n" +
               "                label: { color: '#61a0a8' }\n" +
               "            }\n" +
               "        ]\n" +
               "    }]\n" +
               "};\n" +
               "chart.setOption(option);\n" +
               "window.addEventListener('resize', function() { chart.resize(); });\n" +
               "</script>\n" +
               "</body></html>";

        Log.d("VisualizationFragment", "生成的传播路径HTML: " + html);
        return html;
    }

    private String generateGeoDistributionHtml(VisualizationResponse.GeoDistribution geoDistribution) {
        if (geoDistribution.getRegions() == null) {
            Log.e("VisualizationFragment", "地理分布区域数据为空");
            return "";
        }

        StringBuilder categoriesJson = new StringBuilder("[");
        StringBuilder valuesJson = new StringBuilder("[");
        StringBuilder intensityJson = new StringBuilder("[");
        boolean first = true;

        // 按谣言数量排序
        List<VisualizationResponse.Region> sortedRegions = new ArrayList<>(geoDistribution.getRegions());
        Collections.sort(sortedRegions, (a, b) -> b.getRumorCount() - a.getRumorCount());

        for (VisualizationResponse.Region region : sortedRegions) {
            if (!first) {
                categoriesJson.append(",\n    ");
                valuesJson.append(",\n    ");
                intensityJson.append(",\n    ");
            }
            first = false;
            categoriesJson.append("\"").append(region.getName()).append("\"");
            valuesJson.append("{\n")
                    .append("        value: ").append(region.getRumorCount()).append(",\n")
                    .append("        itemStyle: {\n")
                    .append("            color: '").append(getColorByValue(region.getRumorCount(), sortedRegions)).append("'\n")
                    .append("        }\n")
                    .append("    }");
            intensityJson.append("{\n")
                    .append("        value: ").append(String.format(Locale.US, "%.2f", region.getSpreadIntensity())).append(",\n")
                    .append("        symbolSize: ").append(10 + region.getSpreadIntensity() * 10).append("\n")
                    .append("    }");
        }
        categoriesJson.append("]");
        valuesJson.append("]");
        intensityJson.append("]");

        Log.d("VisualizationFragment", "Categories: " + categoriesJson.toString());
        Log.d("VisualizationFragment", "Values: " + valuesJson.toString());
        Log.d("VisualizationFragment", "Intensity: " + intensityJson.toString());

        String html = "<!DOCTYPE html><html><head>\n" +
               "<meta charset='UTF-8'>\n" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
               "<script src='https://cdn.jsdelivr.net/npm/echarts@5/dist/echarts.min.js'></script>\n" +
               "</head><body>\n" +
               "<div id='main' style='width: 100%; height: 400px;'></div>\n" +
               "<script>\n" +
               "var categories = " + categoriesJson.toString() + ";\n" +
               "var values = " + valuesJson.toString() + ";\n" +
               "var intensity = " + intensityJson.toString() + ";\n" +
               "var chart = echarts.init(document.getElementById('main'));\n" +
               "var option = {\n" +
               "    title: { \n" +
               "        text: '谣言地理分布',\n" +
               "        left: 'center',\n" +
               "        top: 10,\n" +
               "        textStyle: {\n" +
               "            fontSize: 16\n" +
               "        }\n" +
               "    },\n" +
               "    tooltip: {\n" +
               "        trigger: 'axis',\n" +
               "        axisPointer: { type: 'shadow' },\n" +
               "        formatter: function(params) {\n" +
               "            var region = params[0].axisValue;\n" +
               "            var rumorCount = params[0].data.value;\n" +
               "            var intensity = params[1].data.value;\n" +
               "            return region + '<br/>' +\n" +
               "                   '谣言数量: ' + rumorCount + '<br/>' +\n" +
               "                   '传播强度: ' + intensity;\n" +
               "        }\n" +
               "    },\n" +
               "    legend: {\n" +
               "        data: ['谣言数量', '传播强度'],\n" +
               "        top: 40,\n" +
               "        textStyle: {\n" +
               "            fontSize: 12\n" +
               "        }\n" +
               "    },\n" +
               "    grid: {\n" +
               "        left: '10%',\n" +
               "        right: '10%',\n" +
               "        bottom: '20%',\n" +
               "        top: '25%',\n" +
               "        containLabel: true\n" +
               "    },\n" +
               "    xAxis: [{\n" +
               "        type: 'category',\n" +
               "        data: categories,\n" +
               "        axisLabel: { \n" +
               "            interval: 0, \n" +
               "            rotate: 45,\n" +
               "            textStyle: {\n" +
               "                fontSize: 12\n" +
               "            },\n" +
               "            margin: 15\n" +
               "        },\n" +
               "        axisTick: {\n" +
               "            alignWithLabel: true\n" +
               "        }\n" +
               "    }],\n" +
               "    yAxis: [\n" +
               "        {\n" +
               "            type: 'value',\n" +
               "            name: '谣言数量',\n" +
               "            position: 'left',\n" +
               "            nameTextStyle: {\n" +
               "                fontSize: 12,\n" +
               "                padding: [0, 0, 0, 40]\n" +
               "            },\n" +
               "            axisLabel: {\n" +
               "                fontSize: 12\n" +
               "            },\n" +
               "            splitLine: {\n" +
               "                show: false\n" +
               "            }\n" +
               "        },\n" +
               "        {\n" +
               "            type: 'value',\n" +
               "            name: '传播强度',\n" +
               "            position: 'right',\n" +
               "            min: 0,\n" +
               "            max: 1,\n" +
               "            interval: 0.2,\n" +
               "            nameTextStyle: {\n" +
               "                fontSize: 12,\n" +
               "                padding: [0, 40, 0, 0]\n" +
               "            },\n" +
               "            axisLabel: {\n" +
               "                fontSize: 12,\n" +
               "                formatter: '{value}'\n" +
               "            },\n" +
               "            splitLine: {\n" +
               "                show: false\n" +
               "            }\n" +
               "        }\n" +
               "    ],\n" +
               "    series: [\n" +
               "        {\n" +
               "            name: '谣言数量',\n" +
               "            type: 'bar',\n" +
               "            data: values,\n" +
               "            barMaxWidth: 30,\n" +
               "            barGap: '30%',\n" +
               "            label: {\n" +
               "                show: true,\n" +
               "                position: 'top',\n" +
               "                fontSize: 12,\n" +
               "                color: '#666'\n" +
               "            },\n" +
               "            emphasis: {\n" +
               "                itemStyle: {\n" +
               "                    shadowBlur: 10,\n" +
               "                    shadowOffsetX: 0,\n" +
               "                    shadowColor: 'rgba(0, 0, 0, 0.5)'\n" +
               "                }\n" +
               "            }\n" +
               "        },\n" +
               "        {\n" +
               "            name: '传播强度',\n" +
               "            type: 'line',\n" +
               "            yAxisIndex: 1,\n" +
               "            data: intensity,\n" +
               "            itemStyle: {\n" +
               "                color: '#91cc75'\n" +
               "            },\n" +
               "            label: {\n" +
               "                show: true,\n" +
               "                position: 'top',\n" +
               "                fontSize: 12,\n" +
               "                color: '#91cc75',\n" +
               "                formatter: function(params) {\n" +
               "                    return params.data.value;\n" +
               "                }\n" +
               "            },\n" +
               "            lineStyle: {\n" +
               "                width: 3,\n" +
               "                shadowColor: 'rgba(0,0,0,0.3)',\n" +
               "                shadowBlur: 10\n" +
               "            },\n" +
               "            symbol: 'circle',\n" +
               "            symbolSize: 8,\n" +
               "            smooth: true,\n" +
               "            emphasis: {\n" +
               "                scale: true\n" +
               "            }\n" +
               "        }\n" +
               "    ],\n" +
               "    animation: true,\n" +
               "    animationDuration: 1000,\n" +
               "    animationEasing: 'cubicOut'\n" +
               "};\n" +
               "chart.setOption(option);\n" +
               "window.addEventListener('resize', function() { chart.resize(); });\n" +
               "</script>\n" +
               "</body></html>";

        Log.d("VisualizationFragment", "生成的地理分布HTML: " + html);
        return html;
    }

    private String getColorByValue(int value, List<VisualizationResponse.Region> regions) {
        // 找到最大值
        int maxValue = regions.get(0).getRumorCount();
        // 计算当前值的比例
        double ratio = value / (double) maxValue;
        
        // 使用渐变色
        if (ratio >= 0.8) {
            return "#c23531";  // 深红色
        } else if (ratio >= 0.6) {
            return "#e98f6f";  // 浅红色
        } else if (ratio >= 0.4) {
            return "#91cc75";  // 绿色
        } else if (ratio >= 0.2) {
            return "#61a0a8";  // 蓝绿色
        } else {
            return "#2f4554";  // 深蓝色
        }
    }

    private String getStoredToken() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(KEY_TOKEN, null);
        
        if (token == null) {
            Log.e("VisualizationFragment", "No token found in SharedPreferences");
            return null;
        }

        // 确保token格式正确
        if (!token.startsWith("Bearer ")) {
            token = "Bearer " + token;
            Log.d("VisualizationFragment", "Added Bearer prefix to token");
        }

        return token;
    }
} 