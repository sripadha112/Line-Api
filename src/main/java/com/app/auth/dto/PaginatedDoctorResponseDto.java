package com.app.auth.dto;

import java.util.List;

public class PaginatedDoctorResponseDto {
    private List<DoctorSearchResponseDto> doctors;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;

    // Constructors
    public PaginatedDoctorResponseDto() {}

    public PaginatedDoctorResponseDto(List<DoctorSearchResponseDto> doctors, int currentPage, 
                                       int totalPages, long totalElements, int pageSize,
                                       boolean hasNext, boolean hasPrevious, 
                                       boolean isFirst, boolean isLast) {
        this.doctors = doctors;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
        this.isFirst = isFirst;
        this.isLast = isLast;
    }

    // Getters and Setters
    public List<DoctorSearchResponseDto> getDoctors() {
        return doctors;
    }

    public void setDoctors(List<DoctorSearchResponseDto> doctors) {
        this.doctors = doctors;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }
}
