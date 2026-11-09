package com.example.mylibrary;

public class Book {
    private String title;
    private String author;
    private int pages;
    private int read;
    public Book() {
        // Required for Firebase Firestore deserialization
    }

    public Book(String title, String author, int pages, int read) {
        this.title = title;
        this.author = author;
        this.pages = pages;
        this.read = read;
    }

    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public int getPages() {
        return pages;
    }
    public int getRead() {
        return read;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setPages(int pages) {
        this.pages = pages;
    }
    public void setRead(int read) {
        this.read = read;
    }
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", pages=" + pages +
                ", read=" + read +
                '}';
    }
}
