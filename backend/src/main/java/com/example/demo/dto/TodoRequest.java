package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TodoRequest {

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度须在 1~200 个字符之间")
    private String title;

    private boolean completed = false;

    @Min(value = 1, message = "优先级最小值为 1")
    @Max(value = 3, message = "优先级最大值为 3")
    private int priority = 2;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
