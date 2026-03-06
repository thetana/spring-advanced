package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WeatherClientTest {

    private WeatherClient createClientWithMockRestTemplate(RestTemplate restTemplate) {
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.build()).thenReturn(restTemplate);
        return new WeatherClient(builder);
    }

    private String today() {
        return LocalDate.now()
                .format(DateTimeFormatter.ofPattern("MM-dd"));
    }

    @Test
    @DisplayName("상태코드가 OK가 아니면 ServerException 발생")
    void getTodayWeather_statusNotOk() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        WeatherClient client = createClientWithMockRestTemplate(restTemplate);

        when(restTemplate.getForEntity(any(), eq(WeatherDto[].class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(ServerException.class, client::getTodayWeather);
    }

    @Test
    @DisplayName("상태코드는 OK지만 body가 null이면 예외 발생")
    void getTodayWeather_bodyNull() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        WeatherClient client = createClientWithMockRestTemplate(restTemplate);

        when(restTemplate.getForEntity(any(), eq(WeatherDto[].class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThrows(ServerException.class, client::getTodayWeather);
    }

    @Test
    @DisplayName("상태코드는 OK지만 body가 빈 배열이면 예외 발생")
    void getTodayWeather_bodyEmpty() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        WeatherClient client = createClientWithMockRestTemplate(restTemplate);

        when(restTemplate.getForEntity(any(), eq(WeatherDto[].class)))
                .thenReturn(new ResponseEntity<>(new WeatherDto[0], HttpStatus.OK));

        assertThrows(ServerException.class, client::getTodayWeather);
    }

    @Test
    @DisplayName("오늘 날짜에 해당하는 날씨가 존재하면 정상 반환")
    void getTodayWeather_success() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        WeatherClient client = createClientWithMockRestTemplate(restTemplate);

        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("MM-dd"));

        WeatherDto dto = new WeatherDto(today, "맑음");

        when(restTemplate.getForEntity(any(), eq(WeatherDto[].class)))
                .thenReturn(new ResponseEntity<>(new WeatherDto[]{dto}, HttpStatus.OK));

        String result = client.getTodayWeather();

        assertEquals("맑음", result);
    }

    @Test
    @DisplayName("데이터는 있으나 오늘 날짜와 일치하지 않으면 예외 발생")
    void getTodayWeather_todayNotFound() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        WeatherClient client = createClientWithMockRestTemplate(restTemplate);

        WeatherDto dto = new WeatherDto("01-01", "눈");

        when(restTemplate.getForEntity(any(), eq(WeatherDto[].class)))
                .thenReturn(new ResponseEntity<>(new WeatherDto[]{dto}, HttpStatus.OK));

        assertThrows(ServerException.class, client::getTodayWeather);
    }
}