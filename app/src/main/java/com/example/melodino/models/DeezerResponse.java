package com.example.melodino.models;

import java.util.List;

public class DeezerResponse<T> {
    private List<T> data;

    public List<T> getData() {
        return data;
    }
}
