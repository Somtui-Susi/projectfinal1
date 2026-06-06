package com.example.project.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GroqAPI {

    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer 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"
    })
    @POST("chat/completions")
    Call<Object> generateQuiz(
            @Body Object body
    );
}