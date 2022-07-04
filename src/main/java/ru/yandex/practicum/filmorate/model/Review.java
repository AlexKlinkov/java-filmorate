package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Review { // класс отзовов на фильмы
    @JsonProperty("id")
    private Integer reviewId;
    @NotBlank
    private String content;
    @NotNull
    private Boolean isPositive;
    @NotNull
    private Long userId;
    @NotNull
    private Long filmId;
    private int useful;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("review_id", reviewId);
        values.put("content", content);
        values.put("is_positive", isPositive);
        values.put("user_id", userId);
        values.put("film_id", filmId);
        values.put("useful", useful);
        return values;
    }
}
