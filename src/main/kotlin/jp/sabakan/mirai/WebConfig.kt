package jp.sabakan.mirai

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class WebConfig {
    /**
     * RestTemplateのBean定義
     *
     * @return RestTemplateのインスタンス
     */
    @Bean
    fun RestTemplate(): RestTemplate = RestTemplate()
}