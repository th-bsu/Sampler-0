package se.simple.api.composite.product;

public class ReviewSummary {

    private final int reviewId;
    
    // TH: represents authorship of review.
    private final String author;
    
    // TH: represents status of review (i.e. active, complete, n/a).
    private final String status;
    
    // TH: represents ...
    private final String content;

    public ReviewSummary() {
        this.reviewId = 0;
        this.author = null;
        this.status = null;
        this.content = null;
    }

    public ReviewSummary(int reviewId, String author, String status, String content) {
        this.reviewId = reviewId;
        this.author = author;
        this.status = status;
        this.content = content;
    }

    public int getReviewId() {
        return reviewId;
    }

    public String getAuthor() {
        return author;
    }

    public String getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }
}
