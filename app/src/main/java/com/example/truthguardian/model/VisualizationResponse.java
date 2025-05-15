package com.example.truthguardian.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VisualizationResponse {
    private int code;
    private String message;
    private Data data;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public static class Data {
        @SerializedName("geo_distribution")
        private GeoDistribution geoDistribution;
        
        @SerializedName("sentiment_analysis")
        private SentimentAnalysis sentimentAnalysis;
        
        @SerializedName("spread_path")
        private SpreadPath spreadPath;
        
        @SerializedName("text_analysis")
        private TextAnalysis textAnalysis;
        
        @SerializedName("user_behavior")
        private UserBehavior userBehavior;

        public GeoDistribution getGeoDistribution() {
            return geoDistribution;
        }

        public SentimentAnalysis getSentimentAnalysis() {
            return sentimentAnalysis;
        }

        public SpreadPath getSpreadPath() {
            return spreadPath;
        }

        public TextAnalysis getTextAnalysis() {
            return textAnalysis;
        }

        public UserBehavior getUserBehavior() {
            return userBehavior;
        }
    }

    public static class GeoDistribution {
        @SerializedName("heatmap_data")
        private List<HeatmapPoint> heatmapData;
        private List<Region> regions;

        public List<HeatmapPoint> getHeatmapData() {
            return heatmapData;
        }

        public List<Region> getRegions() {
            return regions;
        }
    }

    public static class HeatmapPoint {
        private double intensity;
        private double lat;
        private double lng;

        public double getIntensity() {
            return intensity;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }

    public static class Region {
        private List<Double> coordinates;
        private String name;
        
        @SerializedName("rumor_count")
        private int rumorCount;
        
        @SerializedName("spread_intensity")
        private double spreadIntensity;

        public List<Double> getCoordinates() {
            return coordinates;
        }

        public String getName() {
            return name;
        }

        public int getRumorCount() {
            return rumorCount;
        }

        public double getSpreadIntensity() {
            return spreadIntensity;
        }
    }

    public static class SentimentAnalysis {
        @SerializedName("platform_sentiment")
        private PlatformSentiment platformSentiment;
        private Timeline timeline;

        public PlatformSentiment getPlatformSentiment() {
            return platformSentiment;
        }

        public Timeline getTimeline() {
            return timeline;
        }
    }

    public static class PlatformSentiment {
        private SentimentData news;
        private SentimentData wechat;
        private SentimentData weibo;

        public SentimentData getNews() {
            return news;
        }

        public SentimentData getWechat() {
            return wechat;
        }

        public SentimentData getWeibo() {
            return weibo;
        }
    }

    public static class SentimentData {
        private int negative;
        private int neutral;
        private int positive;

        public int getNegative() {
            return negative;
        }

        public int getNeutral() {
            return neutral;
        }

        public int getPositive() {
            return positive;
        }
    }

    public static class Timeline {
        private List<String> dates;
        private List<Integer> negative;
        private List<Integer> neutral;
        private List<Integer> positive;

        public List<String> getDates() {
            return dates;
        }

        public List<Integer> getNegative() {
            return negative;
        }

        public List<Integer> getNeutral() {
            return neutral;
        }

        public List<Integer> getPositive() {
            return positive;
        }
    }

    public static class SpreadPath {
        private List<Link> links;
        private List<Node> nodes;

        public List<Link> getLinks() {
            return links;
        }

        public List<Node> getNodes() {
            return nodes;
        }
    }

    public static class Link {
        private String source;
        private String target;
        private int value;

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public int getValue() {
            return value;
        }
    }

    public static class Node {
        private String id;
        private String name;
        private String type;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }

    public static class TextAnalysis {
        @SerializedName("credibility_scores")
        private List<CredibilityScore> credibilityScores;
        
        @SerializedName("keyword_cloud")
        private List<KeywordWeight> keywordCloud;
        
        @SerializedName("topic_distribution")
        private List<TopicDistribution> topicDistribution;

        public List<CredibilityScore> getCredibilityScores() {
            return credibilityScores;
        }

        public List<KeywordWeight> getKeywordCloud() {
            return keywordCloud;
        }

        public List<TopicDistribution> getTopicDistribution() {
            return topicDistribution;
        }
    }

    public static class CredibilityScore {
        private String category;
        private int count;

        public String getCategory() {
            return category;
        }

        public int getCount() {
            return count;
        }
    }

    public static class KeywordWeight {
        private int weight;
        private String word;

        public int getWeight() {
            return weight;
        }

        public String getWord() {
            return word;
        }
    }

    public static class TopicDistribution {
        private int percentage;
        private String topic;

        public int getPercentage() {
            return percentage;
        }

        public String getTopic() {
            return topic;
        }
    }

    public static class UserBehavior {
        @SerializedName("interaction_stats")
        private InteractionStats interactionStats;
        
        @SerializedName("user_distribution")
        private UserDistribution userDistribution;

        public InteractionStats getInteractionStats() {
            return interactionStats;
        }

        public UserDistribution getUserDistribution() {
            return userDistribution;
        }
    }

    public static class InteractionStats {
        private List<Integer> comments;
        private List<String> dates;
        private List<Integer> reports;
        private List<Integer> shares;

        public List<Integer> getComments() {
            return comments;
        }

        public List<String> getDates() {
            return dates;
        }

        public List<Integer> getReports() {
            return reports;
        }

        public List<Integer> getShares() {
            return shares;
        }
    }

    public static class UserDistribution {
        @SerializedName("age_groups")
        private AgeGroups ageGroups;
        private List<RegionValue> regions;

        public AgeGroups getAgeGroups() {
            return ageGroups;
        }

        public List<RegionValue> getRegions() {
            return regions;
        }
    }

    public static class AgeGroups {
        @SerializedName("18-24")
        private int age18To24;
        
        @SerializedName("25-34")
        private int age25To34;
        
        @SerializedName("35-44")
        private int age35To44;
        
        @SerializedName("45-54")
        private int age45To54;
        
        @SerializedName("55+")
        private int age55Plus;

        public int getAge18To24() {
            return age18To24;
        }

        public int getAge25To34() {
            return age25To34;
        }

        public int getAge35To44() {
            return age35To44;
        }

        public int getAge45To54() {
            return age45To54;
        }

        public int getAge55Plus() {
            return age55Plus;
        }
    }

    public static class RegionValue {
        private String name;
        private int value;

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
} 