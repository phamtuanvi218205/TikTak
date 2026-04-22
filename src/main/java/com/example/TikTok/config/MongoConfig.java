package com.example.TikTok.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.example.TikTok.repository")
public class MongoConfig {

    @Bean
    public MongoClient mongoClient() {

        String connectionString = "mongodb+srv://chatdb:chatdb123456@chatdb.ryiyft8.mongodb.net/tiktok_chat?retryWrites=true&w=majority&appName=chatdb";

        System.out.println("🚀 ĐANG ÉP KẾT NỐI LÊN CLOUD ATLAS TẠI: chatdb.ryiyft8.mongodb.net");
        return MongoClients.create(connectionString);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "tiktok_chat");
    }
}